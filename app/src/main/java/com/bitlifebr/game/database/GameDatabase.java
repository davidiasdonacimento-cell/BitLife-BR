package com.bitlifebr.game.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bitlifebr.game.character.CharacterState;

public final class GameDatabase extends SQLiteOpenHelper {
    private static final String DB_NAME = "bitlife_br.db";
    private static final int DB_VERSION = 1;

    public GameDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE character_state (" +
                "id INTEGER PRIMARY KEY CHECK(id = 1), " +
                "name TEXT NOT NULL, age INTEGER NOT NULL, " +
                "happiness INTEGER NOT NULL, health INTEGER NOT NULL, " +
                "intelligence INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Preserve existing saves. Future schema changes must use additive migrations.
    }

    public void save(CharacterState character) {
        ContentValues values = new ContentValues();
        values.put("id", 1);
        values.put("name", character.name);
        values.put("age", character.age);
        values.put("happiness", character.happiness);
        values.put("health", character.health);
        values.put("intelligence", character.intelligence);
        getWritableDatabase().insertWithOnConflict(
                "character_state", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public CharacterState load() {
        try (Cursor cursor = getReadableDatabase().query(
                "character_state",
                new String[]{"name", "age", "happiness", "health", "intelligence"},
                "id = ?", new String[]{"1"}, null, null, null)) {
            if (!cursor.moveToFirst()) return null;
            return new CharacterState(
                    cursor.getString(0), cursor.getInt(1), cursor.getInt(2),
                    cursor.getInt(3), cursor.getInt(4));
        }
    }
}
