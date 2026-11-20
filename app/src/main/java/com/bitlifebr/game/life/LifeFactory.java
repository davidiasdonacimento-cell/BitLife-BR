package com.bitlifebr.game.life;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

/** Cria uma vida inicial com família fictícia e dados próprios. */
public final class LifeFactory {
    private static final String[] NAMES = {
            "Ana", "Arthur", "Beatriz", "Bruno", "Camila", "Davi", "Eduarda",
            "Felipe", "Gabriel", "Helena", "Isabela", "João", "Lara", "Lucas",
            "Marina", "Miguel", "Nina", "Pedro", "Rafaela", "Sofia", "Thiago"
    };
    private static final String[] SURNAMES = {
            "Almeida", "Barbosa", "Costa", "Dias", "Ferreira", "Lima",
            "Oliveira", "Pereira", "Ribeiro", "Santos", "Souza"
    };

    private LifeFactory() { }

    public static LifeState create(String parentLifeId, String creationMode,
                                   String name, String surname, String birthDate,
                                   String gender, String personality, String stateCode,
                                   String city, boolean customParents, String parentOne,
                                   String parentTwo, int siblingCount, Random random) {
        Random source = random == null ? new Random() : random;
        String father = customParents && notBlank(parentOne) ? parentOne.trim()
                : pick(NAMES, source) + " " + pick(SURNAMES, source);
        String mother = customParents && notBlank(parentTwo) ? parentTwo.trim()
                : pick(NAMES, source) + " " + pick(SURNAMES, source);

        ArrayList<String> siblings = new ArrayList<>();
        for (int i = 0; i < Math.max(0, Math.min(12, siblingCount)); i++) {
            siblings.add(pick(NAMES, source) + " " + pick(SURNAMES, source));
        }

        LifeState life = new LifeState(
                UUID.randomUUID().toString(), parentLifeId, creationMode,
                name, surname, birthDate, gender, personality, stateCode, city,
                father, mother, "automatic", "automatic", System.currentTimeMillis(),
                0, 70, 85, 50, 50, 50, 50, 50, 0, true, siblings, new ArrayList<>());
        life.history.add("A história de " + life.fullName() + " começou em "
                + life.city + " (" + life.stateCode + ").");
        return life;
    }

    public static LifeState importLegacy(String name, int age, int happiness,
                                         int health, int intelligence) {
        LifeState life = new LifeState(
                UUID.randomUUID().toString(), null, "legacy", name, "",
                "", "Não especificado", "equilibrado", "BR", "Brasil",
                "Não informado", "Não informado", "automatic", "automatic",
                System.currentTimeMillis(), age, happiness, health, intelligence,
                50, 50, 50, 50, 0, true, new ArrayList<>(), new ArrayList<>());
        life.history.add("Save antigo importado sem apagar o progresso.");
        return life;
    }

    private static String pick(String[] values, Random random) {
        return values[random.nextInt(values.length)];
    }

    private static boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}