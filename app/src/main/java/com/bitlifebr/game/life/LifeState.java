package com.bitlifebr.game.life;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

/** Estado persistente de uma vida. Cada registro é independente dos demais. */
public final class LifeState {
    public final String id;
    public final String parentLifeId;
    public final String creationMode;
    public final String name;
    public final String surname;
    public String birthDate;
    public final String gender;
    public final String personality;
    public final String stateCode;
    public final String city;
    public final String fatherName;
    public final String motherName;
    public final String appearanceMode;
    public final String clothingMode;
    public final long createdAt;
    public int age;
    public int happiness;
    public int health;
    public int intelligence;
    public int appearance;
    public int charisma;
    public int discipline;
    public int popularity;
    public long moneyCents;
    public boolean alive;
    public final ArrayList<String> siblings;
    public final ArrayList<String> history;

    public LifeState(String id, String parentLifeId, String creationMode, String name,
                     String surname, String birthDate, String gender, String personality,
                     String stateCode, String city, String fatherName, String motherName,
                     String appearanceMode, String clothingMode, long createdAt,
                     int age, int happiness, int health, int intelligence, int appearance,
                     int charisma, int discipline, int popularity, long moneyCents,
                     boolean alive, ArrayList<String> siblings, ArrayList<String> history) {
        this.id = valueOr(id, UUID.randomUUID().toString());
        this.parentLifeId = emptyToNull(parentLifeId);
        this.creationMode = valueOr(creationMode, "create");
        this.name = valueOr(name, "Personagem");
        this.surname = valueOr(surname, "");
        this.birthDate = valueOr(birthDate, "");
        this.gender = valueOr(gender, "Não especificado");
        this.personality = valueOr(personality, "equilibrado");
        this.stateCode = valueOr(stateCode, "BA").toUpperCase();
        this.city = valueOr(city, "Salvador");
        this.fatherName = valueOr(fatherName, "Responsável 1");
        this.motherName = valueOr(motherName, "Responsável 2");
        this.appearanceMode = valueOr(appearanceMode, "automatic");
        this.clothingMode = valueOr(clothingMode, "automatic");
        this.createdAt = createdAt > 0 ? createdAt : System.currentTimeMillis();
        this.age = Math.max(0, Math.min(120, age));
        this.happiness = clamp(happiness);
        this.health = clamp(health);
        this.intelligence = clamp(intelligence);
        this.appearance = clamp(appearance);
        this.charisma = clamp(charisma);
        this.discipline = clamp(discipline);
        this.popularity = clamp(popularity);
        this.moneyCents = Math.max(0L, moneyCents);
        this.alive = alive;
        this.siblings = siblings == null ? new ArrayList<>() : new ArrayList<>(siblings);
        this.history = history == null ? new ArrayList<>() : new ArrayList<>(history);
    }

    public String fullName() { return (name + " " + surname).trim(); }
    public boolean isDescendant() { return parentLifeId != null && !parentLifeId.isEmpty(); }

    public LifeState copy() {
        return new LifeState(id, parentLifeId, creationMode, name, surname, birthDate, gender,
                personality, stateCode, city, fatherName, motherName, appearanceMode,
                clothingMode, createdAt, age, happiness, health, intelligence, appearance,
                charisma, discipline, popularity, moneyCents, alive, siblings, history);
    }

    public String serialize() {
        try {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("parentLifeId", parentLifeId);
            json.put("creationMode", creationMode);
            json.put("name", name);
            json.put("surname", surname);
            json.put("birthDate", birthDate);
            json.put("gender", gender);
            json.put("personality", personality);
            json.put("stateCode", stateCode);
            json.put("city", city);
            json.put("fatherName", fatherName);
            json.put("motherName", motherName);
            json.put("appearanceMode", appearanceMode);
            json.put("clothingMode", clothingMode);
            json.put("createdAt", createdAt);
            json.put("age", age);
            json.put("happiness", happiness);
            json.put("health", health);
            json.put("intelligence", intelligence);
            json.put("appearance", appearance);
            json.put("charisma", charisma);
            json.put("discipline", discipline);
            json.put("popularity", popularity);
            json.put("moneyCents", moneyCents);
            json.put("alive", alive);
            json.put("siblings", new JSONArray(siblings));
            json.put("history", new JSONArray(history));
            return json.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("Não foi possível serializar a vida.", e);
        }
    }

    public static LifeState deserialize(String source) {
        try {
            JSONObject json = new JSONObject(source);
            return new LifeState(
                    json.optString("id", UUID.randomUUID().toString()),
                    json.optString("parentLifeId", null),
                    json.optString("creationMode", "create"),
                    json.optString("name", "Personagem"),
                    json.optString("surname", ""),
                    json.optString("birthDate", ""),
                    json.optString("gender", "Não especificado"),
                    json.optString("personality", "equilibrado"),
                    json.optString("stateCode", "BA"),
                    json.optString("city", "Salvador"),
                    json.optString("fatherName", "Responsável 1"),
                    json.optString("motherName", "Responsável 2"),
                    json.optString("appearanceMode", "automatic"),
                    json.optString("clothingMode", "automatic"),
                    json.optLong("createdAt", System.currentTimeMillis()),
                    json.optInt("age", 0),
                    json.optInt("happiness", 70),
                    json.optInt("health", 85),
                    json.optInt("intelligence", 50),
                    json.optInt("appearance", 50),
                    json.optInt("charisma", 50),
                    json.optInt("discipline", 50),
                    json.optInt("popularity", 50),
                    json.optLong("moneyCents", 0),
                    json.optBoolean("alive", true),
                    readArray(json.optJSONArray("siblings")),
                    readArray(json.optJSONArray("history")));
        } catch (JSONException e) {
            throw new IllegalArgumentException("Save de vida inválido.", e);
        }
    }

    private static ArrayList<String> readArray(JSONArray array) {
        ArrayList<String> values = new ArrayList<>();
        if (array == null) return values;
        for (int i = 0; i < array.length(); i++) {
            String value = array.optString(i, "");
            if (!value.isEmpty()) values.add(value);
        }
        return values;
    }

    private static int clamp(int value) { return Math.max(0, Math.min(100, value)); }
    private static String valueOr(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
    private static String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}