package com.bitlifebr.game.core;

import com.bitlifebr.game.character.CharacterState;

import java.util.Random;

public final class CharacterFactory {
    private static final String[] NAMES = {
            "Alex", "Ana", "Beatriz", "Bruno", "Camila", "Davi",
            "Gabriel", "Helena", "Isabela", "Lucas", "Marina", "Rafael"
    };

    private CharacterFactory() { }

    public static CharacterState create(Random random) {
        String name = NAMES[random.nextInt(NAMES.length)];
        return new CharacterState(name, 0, 70, 85, 50);
    }
}
