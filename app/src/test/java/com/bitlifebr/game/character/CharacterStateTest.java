package com.bitlifebr.game.character;

import org.junit.Test;
import static org.junit.Assert.*;

public class CharacterStateTest {
    @Test public void nextYearIncreasesAgeAndPreservesAttributes() {
        CharacterState before = new CharacterState("Ana", 17, 80, 90, 75);
        CharacterState after = before.nextYear();
        assertEquals(18, after.age);
        assertEquals(80, after.happiness);
        assertEquals(90, after.health);
        assertEquals(75, after.intelligence);
    }

    @Test public void attributesAreClamped() {
        CharacterState state = new CharacterState("Davi", 20, 150, -5, 50);
        assertEquals(100, state.happiness);
        assertEquals(0, state.health);
    }
}
