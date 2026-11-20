package com.bitlifebr.game.character;

public final class CharacterState {
    public final String name;
    public final int age;
    public final int happiness;
    public final int health;
    public final int intelligence;

    public CharacterState(String name, int age, int happiness, int health, int intelligence) {
        this.name = name;
        this.age = age;
        this.happiness = clamp(happiness);
        this.health = clamp(health);
        this.intelligence = clamp(intelligence);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    public CharacterState nextYear() {
        return new CharacterState(name, age + 1, happiness, health, intelligence);
    }
}
