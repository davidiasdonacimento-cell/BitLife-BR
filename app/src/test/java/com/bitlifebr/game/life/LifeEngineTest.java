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
        assertEquals(2, next.history.size());
        assertEquals("19/11/2027", next.birthDate);
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


    @Test
    public void annualEventsHaveDifferentIdsAndTextForEachAge() {
        LifeState life = LifeFactory.create(null, "create", "Bia", "Souza",
                "19/11/2026", "Feminino", "curioso", "BA", "Salvador",
                false, "", "", 0, new Random(8));
        LifeEvent first = LifeEngine.createAnnualEvent(life);
        life.age = 1;
        LifeEvent second = LifeEngine.createAnnualEvent(life);
        assertNotEquals(first.id, second.id);
        assertNotEquals(first.title, second.title);
        assertNotEquals(first.description, second.description);
    }

    @Test(expected = IllegalArgumentException.class)
    public void oldAnnualEventCannotAdvanceTheSameLifeTwice() {
        LifeState life = LifeFactory.create(null, "create", "Caio", "Lima",
                "19/11/2026", "Masculino", "equilibrado", "BA", "Salvador",
                false, "", "", 0, new Random(9));
        LifeEvent event = LifeEngine.createAnnualEvent(life);
        LifeState afterFirstChoice = LifeEngine.applyChoice(life, event, "family_time");
        LifeEngine.applyChoice(afterFirstChoice, event, "family_time");
    }


    @Test
    public void thirdAnnualChoiceAdvancesAgeAndChangesAttributes() {
        LifeState life = LifeFactory.create(null, "create", "Caio", "Mendes",
                "19/11/2000", "Masculino", "equilibrado", "BA", "Salvador",
                false, "", "", 0, new Random(10));
        life.age = 26;
        LifeEvent event = LifeEngine.createAnnualEvent(life);

        LifeState next = LifeEngine.applyChoice(life, event, event.choiceCId);

        assertEquals(27, next.age);
        assertEquals(7500L, next.moneyCents);
        assertEquals(26, life.age);
        assertTrue(next.history.get(next.history.size() - 1).contains(event.choiceCLabel));
    }

    @Test
    public void quickActivityChangesStatsWithoutAdvancingAge() {
        LifeState life = LifeFactory.create(null, "create", "Lia", "Souza",
                "19/11/2010", "Feminino", "criativo", "BA", "Salvador",
                false, "", "", 0, new Random(11));
        life.age = 15;

        LifeState next = LifeEngine.applyActivity(life, "sports_group");

        assertEquals(15, next.age);
        assertTrue(next.health > life.health);
        assertTrue(next.history.size() > life.history.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void eventRejectsChoiceFromAnotherEvent() {
        LifeState life = LifeFactory.create(null, "create", "João", "Dias",
                "19/11/2026", "Masculino", "equilibrado", "BA", "Salvador",
                false, "", "", 0, new Random(3));
        LifeEngine.applyChoice(life, LifeEngine.createAnnualEvent(life), "invalid_choice");
    }
}