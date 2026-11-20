package com.bitlifebr.game.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bitlifebr.game.character.CharacterState;
import com.bitlifebr.game.life.LifeFactory;
import com.bitlifebr.game.life.LifeState;

import java.util.ArrayList;
import java.util.List;

/**
 * Banco local. O modelo de vidas múltiplas é aditivo e mantém a tabela antiga
 * para que saves instalados em versões anteriores continuem recuperáveis.
 */
public final class GameDatabase extends SQLiteOpenHelper {
    private static final String DB_NAME = "bitlife_br.db";
    private static final int DB_VERSION = 2;

    public GameDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createLegacyTable(db);
        createLivesTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            createLivesTable(db);
            migrateLegacySave(db);
        }
        // Próximas versões devem acrescentar tabelas/colunas sem apagar saves.
    }

    private void createLegacyTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS character_state (" +
                "id INTEGER PRIMARY KEY CHECK(id = 1), " +
                "name TEXT NOT NULL, age INTEGER NOT NULL, " +
                "happiness INTEGER NOT NULL, health INTEGER NOT NULL, " +
                "intelligence INTEGER NOT NULL)");
    }

    private void createLivesTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS game_lives (" +
                "id TEXT PRIMARY KEY NOT NULL, " +
                "parent_life_id TEXT, " +
                "state_json TEXT NOT NULL, " +
                "is_active INTEGER NOT NULL DEFAULT 0, " +
                "created_at INTEGER NOT NULL, " +
                "updated_at INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS game_lives_parent_idx " +
                "ON game_lives(parent_life_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS game_lives_updated_idx " +
                "ON game_lives(updated_at DESC)");
    }

    private void migrateLegacySave(SQLiteDatabase db) {
        try (Cursor cursor = db.query("character_state",
                new String[]{"name", "age", "happiness", "health", "intelligence"},
                "id = ?", new String[]{"1"}, null, null, null)) {
            if (!cursor.moveToFirst()) return;
            LifeState legacy = LifeFactory.importLegacy(
                    cursor.getString(0), cursor.getInt(1), cursor.getInt(2),
                    cursor.getInt(3), cursor.getInt(4));
            ContentValues values = lifeValues(legacy, true);
            db.insertWithOnConflict("game_lives", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    private ContentValues lifeValues(LifeState life, boolean active) {
        ContentValues values = new ContentValues();
        values.put("id", life.id);
        values.put("parent_life_id", life.parentLifeId);
        values.put("state_json", life.serialize());
        values.put("is_active", active ? 1 : 0);
        values.put("created_at", life.createdAt);
        values.put("updated_at", System.currentTimeMillis());
        return values;
    }

    public void saveLife(LifeState life) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("UPDATE game_lives SET is_active = 0");
            ContentValues values = lifeValues(life, true);
            db.insertWithOnConflict("game_lives", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public LifeState loadActiveLife() {
        try (Cursor cursor = getReadableDatabase().query(
                "game_lives", new String[]{"state_json"}, "is_active = ?",
                new String[]{"1"}, null, null, "updated_at DESC", "1")) {
            if (!cursor.moveToFirst()) return null;
            return LifeState.deserialize(cursor.getString(0));
        }
    }

    public List<LifeState> loadLives() {
        ArrayList<LifeState> lives = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(
                "game_lives", new String[]{"state_json"}, null, null,
                null, null, "updated_at DESC")) {
            while (cursor.moveToNext()) {
                try {
                    lives.add(LifeState.deserialize(cursor.getString(0)));
                } catch (IllegalArgumentException ignored) {
                    // Um registro danificado não deve impedir a abertura dos outros saves.
                }
            }
        }
        return lives;
    }

    public List<LifeState> loadDescendants() {
        ArrayList<LifeState> lives = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(
                "game_lives", new String[]{"state_json"},
                "parent_life_id IS NOT NULL", null, null, null, "updated_at DESC")) {
            while (cursor.moveToNext()) {
                try {
                    lives.add(LifeState.deserialize(cursor.getString(0)));
                } catch (IllegalArgumentException ignored) {
                    // Ignora apenas o registro inválido; os demais permanecem disponíveis.
                }
            }
        }
        return lives;
    }

    public void setActiveLife(String lifeId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("UPDATE game_lives SET is_active = 0");
            db.execSQL("UPDATE game_lives SET is_active = 1, updated_at = ? WHERE id = ?",
                    new Object[]{System.currentTimeMillis(), lifeId});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /** Compatibilidade com a API antiga da primeira fundação. */
    public void save(CharacterState character) {
        if (character == null) return;
        LifeState life = LifeFactory.importLegacy(character.name, character.age,
                character.happiness, character.health, character.intelligence);
        saveLife(life);
    }

    /** Recupera um save antigo se a migração foi executada. */
    public CharacterState load() {
        LifeState life = loadActiveLife();
        if (life == null) return null;
        return new CharacterState(life.fullName(), life.age, life.happiness,
                life.health, life.intelligence);
    }
}