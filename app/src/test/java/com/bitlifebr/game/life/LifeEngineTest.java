package com.bitlifebr.game.life;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class LifeEngineTest {
    @Test
    public void annualChoiceAdvancesAgeAndChangesAttributes() {
        LifeState life = LifeFactory.create(null, "create", "Ana", "Costa",
                "19/11/2026", "Feminino", "curioso", "BA", "Salvador",
                false, "", "", 2, new Random(7));
        LifeEvent event = LifeEngine.createAnnualEvent(life);

        LifeState next = LifeEngine.applyChoice(life, event, "creative_play");

        assertEquals(1, next.age);
        assertEquals(72, next.happiness);
        assertEquals(52, next.charisma);
        assertEquals(0, life.age);
        assertEquals(1, next.history.size());
    }

    @Test
    public void eventChangesWithLifeStage() {
        LifeState life = LifeFactory.create(null, "create", "Bruno", "Lima",
                "19/11/2008", "Masculino", "disciplinado", "BA", "Salvador",
                false, "", "", 0, new Random(1));
        life.age = 15;

        assertTrue(LifeEngine.createAnnualEvent(life).id.startsWith("teen_"));
    }

    @Test
    public void factoryCreatesRequestedFamilyAndDescendantLink() {
        LifeState life = LifeFactory.create("parent-life-id", "child", "Lara", "Santos",
                "19/11/2026", "Feminino", "criativo", "BA", "Feira de Santana",
                true, "Responsável Um", "Responsável Dois", 3, new Random(2));

        assertTrue(life.isDescendant());
        assertEquals("parent-life-id", life.parentLifeId);
        assertEquals(3, life.siblings.size());
        assertEquals("Responsável Um", life.fatherName);
        assertEquals("Responsável Dois", life.motherName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void eventRejectsChoiceFromAnotherEvent() {
        LifeState life = LifeFactory.create(null, "create", "João", "Dias",
                "19/11/2026", "Masculino", "equilibrado", "BA", "Salvador",
                false, "", "", 0, new Random(3));
        LifeEngine.applyChoice(life, LifeEngine.createAnnualEvent(life), "invalid_choice");
    }
}