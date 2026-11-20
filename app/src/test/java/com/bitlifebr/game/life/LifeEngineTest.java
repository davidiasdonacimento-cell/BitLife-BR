package com.bitlifebr.game.life;

import org.junit.Test;
import org.json.JSONObject;

import java.util.Random;

import static org.junit.Assert.*;

public class LifeEngineTest {
    @Test
    public void annualChoiceAdvancesAgeAndChangesAttributes() {
        LifeState life = LifeFactory.create(null, "create", "Ana", "Costa",
                "19/11/2026", "Feminino", "curioso", "BA", "Salvador",
                false, "", "", 2, new Random(7));
        LifeEvent event = LifeEngine.createAnnualEvent(life);

        LifeState next = LifeEngine.applyChoice(life, event, event.choiceBId);

        assertEquals(1, next.age);
        assertTrue(next.happiness >= 0 && next.happiness <= 100);
        assertTrue(next.charisma >= 0 && next.charisma <= 100);
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
        assertTrue(next.moneyCents >= life.moneyCents);
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

    @Test
    public void consecutiveYearsRotateThroughDifferentScenarios() {
        LifeState life = LifeFactory.create(null, "create", "Nina", "Alves",
                "19/11/2026", "Feminino", "curioso", "BA", "Salvador",
                false, "", "", 1, new Random(18));
        java.util.HashSet<String> scenarioTitles = new java.util.HashSet<>();
        for (int year = 0; year < 8; year++) {
            LifeEvent event = LifeEngine.createAnnualEvent(life);
            scenarioTitles.add(event.title.substring(0, event.title.lastIndexOf(" • ")));
            life = LifeEngine.applyChoice(life, event, event.choiceAId);
        }
        assertEquals("Oito anos seguidos devem percorrer oito acontecimentos diferentes.", 8, scenarioTitles.size());
    }

    @Test
    public void eventUsesLifeCircumstancesInItsDescription() {
        LifeState life = LifeFactory.create(null, "create", "Rafa", "Melo",
                "19/11/1990", "Masculino", "equilibrado", "BA", "Feira de Santana",
                false, "", "", 2, new Random(19));
        life.age = 30;
        life.happiness = 20;
        String description = LifeEngine.createAnnualEvent(life).description;
        assertTrue(description.contains("Feira de Santana"));
        assertTrue(description.contains("recuperar o ânimo"));
        assertTrue(description.contains("irmãos"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void eventRejectsChoiceFromAnotherEvent() {
        LifeState life = LifeFactory.create(null, "create", "João", "Dias",
                "19/11/2026", "Masculino", "equilibrado", "BA", "Salvador",
                false, "", "", 0, new Random(3));
        LifeEngine.applyChoice(life, LifeEngine.createAnnualEvent(life), "invalid_choice");
    }
    @Test
    public void expansionPackChoiceAppliesItsActualEffectValues() throws Exception {
        LifeState life = LifeFactory.create(null, "create", "Davi", "Silva",
                "19/11/2000", "Masculino", "equilibrado", "BA", "Salvador",
                false, "", "", 0, new Random(22));
        life.age = 26;
        JSONObject effects = new JSONObject();
        effects.put("happiness", 4);
        effects.put("intelligence", 3);
        effects.put("social", 2);
        LifeEvent event = new LifeEvent(life.id, life.age, "pack_test", "Evento do pacote",
                "Teste de efeitos reais", "family_time", "Escolha A",
                "rest", "Escolha B", "social_network", "Escolha C",
                effects, new JSONObject(), new JSONObject());

        LifeState next = LifeEngine.applyChoice(life, event, event.choiceAId);

        assertEquals(life.happiness + 4, next.happiness);
        assertEquals(life.intelligence + 3, next.intelligence);
        assertEquals(life.charisma + 2, next.charisma);
        assertEquals(life.popularity + 2, next.popularity);
    }

}