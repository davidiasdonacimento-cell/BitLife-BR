package com.bitlifebr.game.life;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Regras determinísticas da primeira etapa jogável. Toda escolha altera o save. */
public final class LifeEngine {
    private LifeEngine() { }

    public static LifeEvent createAnnualEvent(LifeState life) {
        if (life.age < 6) {
            return new LifeEvent("family_" + life.age, "Um ano em família",
                    "Sua rotina muda conforme você cresce. Como sua família vai aproveitar este período?",
                    "family_time", "Passar mais tempo em família",
                    "creative_play", "Explorar brincadeiras e atividades");
        }
        if (life.age < 12) {
            return new LifeEvent("school_" + life.age, "Novidades na escola",
                    "A escola oferece novas oportunidades de aprendizado e convivência.",
                    "focus_study", "Dedicar mais tempo aos estudos",
                    "join_arts", "Participar de atividades e artes");
        }
        if (life.age < 18) {
            return new LifeEvent("teen_" + life.age, "Uma escolha na adolescência",
                    "Você pode desenvolver habilidades acadêmicas ou participar de atividades com outras pessoas.",
                    "sports_group", "Participar de uma atividade esportiva",
                    "study_group", "Entrar em um grupo de estudos");
        }
        if (life.age < 60) {
            return new LifeEvent("adult_" + life.age, "Uma oportunidade para o futuro",
                    "Surge a oportunidade de investir no aprendizado ou ampliar sua rede de contatos.",
                    "professional_course", "Fazer um curso de qualificação",
                    "social_network", "Conhecer pessoas e ampliar contatos");
        }
        return new LifeEvent("senior_" + life.age, "Um novo capítulo",
                "Você pode fortalecer sua convivência social ou dedicar tempo a um interesse pessoal.",
                "community", "Participar de uma atividade comunitária",
                "personal_hobby", "Dedicar tempo a um hobby");
    }

    public static LifeState applyChoice(LifeState current, LifeEvent event, String choiceId) {
        if (current == null || event == null) throw new IllegalArgumentException("Vida ou evento inválido.");
        boolean first = event.choiceAId.equals(choiceId);
        boolean second = event.choiceBId.equals(choiceId);
        if (!first && !second) throw new IllegalArgumentException("Escolha não pertence ao evento.");

        LifeState next = current.copy();
        next.age = Math.min(120, current.age + 1);
        next.birthDate = advanceBirthDate(current.birthDate);

        if ("family_time".equals(choiceId)) {
            next.happiness += 3;
            next.health += 1;
        } else if ("creative_play".equals(choiceId)) {
            next.happiness += 2;
            next.charisma += 2;
        } else if ("focus_study".equals(choiceId)) {
            next.intelligence += 4;
            next.discipline += 2;
            next.happiness -= 1;
        } else if ("join_arts".equals(choiceId)) {
            next.happiness += 3;
            next.charisma += 2;
            next.popularity += 1;
        } else if ("sports_group".equals(choiceId)) {
            next.health += 2;
            next.happiness += 3;
            next.charisma += 1;
        } else if ("study_group".equals(choiceId)) {
            next.intelligence += 4;
            next.discipline += 2;
            next.charisma += 1;
        } else if ("professional_course".equals(choiceId)) {
            next.intelligence += 3;
            next.discipline += 2;
            next.happiness += 1;
        } else if ("social_network".equals(choiceId)) {
            next.charisma += 3;
            next.popularity += 2;
            next.happiness += 1;
        } else if ("community".equals(choiceId)) {
            next.happiness += 3;
            next.popularity += 1;
        } else if ("personal_hobby".equals(choiceId)) {
            next.happiness += 2;
            next.intelligence += 1;
        }

        next.happiness = clamp(next.happiness);
        next.health = clamp(next.health);
        next.intelligence = clamp(next.intelligence);
        next.charisma = clamp(next.charisma);
        next.discipline = clamp(next.discipline);
        next.popularity = clamp(next.popularity);
        String selected = first ? event.choiceALabel : event.choiceBLabel;
        next.history.add("Aos " + next.age + " anos: " + event.title + " — " + selected + ".");
        while (next.history.size() > 500) next.history.remove(0);
        return next;
    }

    private static String advanceBirthDate(String value) {
        if (value == null || value.trim().isEmpty()) return value;
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT);
        format.setLenient(false);
        try {
            Date date = format.parse(value);
            if (date == null) return value;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.YEAR, 1);
            return format.format(calendar.getTime());
        } catch (ParseException ignored) {
            return value;
        }
    }

    private static int clamp(int value) { return Math.max(0, Math.min(100, value)); }
}