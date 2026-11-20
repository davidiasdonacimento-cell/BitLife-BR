package com.bitlifebr.game.life;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Motor de decisões do BitLife BR. Toda escolha altera e retorna um save novo. */
public final class LifeEngine {
    private LifeEngine() { }

    private static final String[][] TITLES = {
        {"Uma surpresa em família", "Uma descoberta inesperada", "Um dia cheio de brincadeiras", "Um momento especial em casa"},
        {"Uma novidade na escola", "Uma amizade está começando", "Um projeto chamou atenção", "Um convite para uma atividade"},
        {"Uma escolha na adolescência", "Uma oportunidade com amigos", "Um desafio pessoal", "Uma novidade nos estudos"},
        {"Uma oportunidade para o futuro", "Uma decisão importante", "Uma proposta inesperada", "Um novo capítulo profissional"},
        {"Uma nova fase da vida", "Um convite da comunidade", "Uma ideia para realizar", "Um reencontro especial"}
    };

    private static final String[][] STORIES = {
        {"Sua família preparou uma surpresa para você. Como deseja aproveitar esse momento?",
         "Você encontrou algo novo que despertou sua curiosidade. O que vai fazer?",
         "Hoje há várias possibilidades para brincar, aprender e se divertir. Qual caminho escolhe?",
         "Pessoas importantes querem passar um tempo com você. Como pretende aproveitar?"},
        {"Na escola apareceu uma oportunidade diferente. Como você vai participar?",
         "Alguém com interesses parecidos quer se aproximar. O que você faz?",
         "Um projeto está sendo organizado e você pode escolher seu papel.",
         "Uma atividade extracurricular abriu inscrições. Qual será sua decisão?"},
        {"Você está descobrindo novos interesses e precisa escolher onde investir seu tempo.",
         "Seus amigos estão organizando algo e você pode participar de várias maneiras.",
         "Um desafio pessoal pode ajudar você a desenvolver novas habilidades.",
         "Uma oportunidade de estudo apareceu e pode influenciar seus próximos passos."},
        {"Uma oportunidade surgiu e pode mudar sua rotina. Como pretende lidar com ela?",
         "Você precisa escolher onde colocar sua energia neste momento da vida.",
         "Uma proposta apareceu. Considere seus objetivos antes de decidir.",
         "Há caminhos diferentes para construir seu futuro. Qual combina com você?"},
        {"Uma nova experiência pode trazer aprendizado e boas conexões.",
         "A comunidade está organizando uma atividade e sua participação é bem-vinda.",
         "Uma ideia antiga pode finalmente sair do papel. Como deseja começar?",
         "Uma pessoa querida quer reencontrar você. Como pretende aproveitar?"}
    };

    public static LifeEvent createAnnualEvent(LifeState life) {
        if (life == null) throw new IllegalArgumentException("Vida inválida.");
        int age = Math.max(0, Math.min(120, life.age));
        int stage;
        String prefix;
        String aId, aLabel, bId, bLabel, cId, cLabel;

        if (age < 6) {
            stage = 0; prefix = "child";
            aId = "family_time"; aLabel = "Passar o dia com a família";
            bId = "creative_play"; bLabel = "Explorar e brincar";
            cId = "rest"; cLabel = "Descansar e recuperar as energias";
        } else if (age < 12) {
            stage = 1; prefix = "school";
            aId = "focus_study"; aLabel = "Dedicar tempo aos estudos";
            bId = "join_arts"; bLabel = "Participar de uma atividade artística";
            cId = "sports_group"; cLabel = "Entrar em uma atividade esportiva";
        } else if (age < 18) {
            stage = 2; prefix = "teen";
            aId = "study_group"; aLabel = "Priorizar os estudos";
            bId = "sports_group"; bLabel = "Participar de um esporte";
            cId = "social_network"; cLabel = "Conhecer pessoas e fazer amizades";
        } else if (age < 60) {
            stage = 3; prefix = "adult";
            aId = "professional_course"; aLabel = "Investir em qualificação";
            bId = "social_network"; bLabel = "Ampliar sua rede de contatos";
            cId = "work_shift"; cLabel = "Aceitar um trabalho extra remunerado";
        } else {
            stage = 4; prefix = "senior";
            aId = "community"; aLabel = "Participar de uma atividade comunitária";
            bId = "personal_hobby"; bLabel = "Dedicar tempo a um hobby";
            cId = "family_time"; cLabel = "Aproximar-se da família";
        }

        int slot = Math.floorMod((life.id == null ? 0 : life.id.hashCode()) + age * 31, 4);
        String title = TITLES[stage][slot] + " • " + age + " anos";
        String description = STORIES[stage][slot] + " Você está com " + age + " anos e mora em "
                + life.city + " (" + life.stateCode + ").";
        String eventId = prefix + "_" + life.id + "_" + age + "_" + slot;
        return new LifeEvent(life.id, age, eventId, title, description,
                aId, aLabel, bId, bLabel, cId, cLabel);
    }

    /** Atividade rápida: muda atributos/histórico, mas nunca altera a idade. */
    public static LifeState applyActivity(LifeState current, String actionId) {
        if (current == null || actionId == null) throw new IllegalArgumentException("Atividade inválida.");
        LifeState next = current.copy();
        String description;
        switch (actionId) {
            case "family_time":
                next.happiness += 3; next.charisma += 1; description = "Passou um tempo com a família"; break;
            case "creative_play":
                next.happiness += 4; next.charisma += 1; description = "Explorou uma atividade criativa"; break;
            case "rest":
                next.health += 3; next.happiness += 2; description = "Descansou e cuidou do bem-estar"; break;
            case "focus_study":
                next.intelligence += 3; next.discipline += 2; description = "Estudou e praticou novas habilidades"; break;
            case "join_arts":
                next.charisma += 2; next.happiness += 2; description = "Participou de uma atividade artística"; break;
            case "sports_group":
                next.health += 3; next.happiness += 2; description = "Praticou uma atividade esportiva"; break;
            case "study_group":
                next.intelligence += 3; next.discipline += 2; description = "Participou de um grupo de estudos"; break;
            case "social_network":
                next.charisma += 3; next.popularity += 2; next.happiness += 1; description = "Socializou e conheceu pessoas"; break;
            case "professional_course":
                next.intelligence += 3; next.discipline += 2; description = "Estudou para desenvolver a carreira"; break;
            case "work_shift":
                if (current.age < 18) throw new IllegalArgumentException("Esta atividade está disponível a partir dos 18 anos.");
                next.moneyCents = safeAdd(next.moneyCents, 7500L);
                next.discipline += 1; next.health -= 1; description = "Concluiu um trabalho extra e recebeu R$ 75,00"; break;
            case "community":
                next.happiness += 3; next.popularity += 1; description = "Participou de uma atividade comunitária"; break;
            case "personal_hobby":
                next.happiness += 2; next.intelligence += 1; description = "Dedicou tempo a um hobby"; break;
            default:
                throw new IllegalArgumentException("Atividade não reconhecida.");
        }
        clampStats(next);
        next.history.add("Aos " + next.age + " anos: " + description + ".");
        trimHistory(next);
        return next;
    }

    public static LifeState applyChoice(LifeState current, LifeEvent event, String choiceId) {
        if (current == null || event == null) throw new IllegalArgumentException("Vida ou evento inválido.");
        if (event.ageAtCreation >= 0 && (event.ageAtCreation != current.age
                || (event.lifeId != null && !event.lifeId.equals(current.id)))) {
            throw new IllegalArgumentException("Este evento já foi usado ou pertence a outra vida. Atualize a tela.");
        }
        if (!event.accepts(choiceId)) throw new IllegalArgumentException("Escolha não pertence ao evento.");
        if (current.age >= 120) throw new IllegalArgumentException("Esta vida já chegou ao limite de idade.");

        LifeState next = current.copy();
        next.age = current.age + 1;
        next.birthDate = advanceBirthDate(current.birthDate);
        next = applyChoiceEffects(next, choiceId);
        String label = event.choiceAId.equals(choiceId) ? event.choiceALabel
                : event.choiceBId.equals(choiceId) ? event.choiceBLabel : event.choiceCLabel;
        next.history.add("Aos " + next.age + " anos: " + event.title + " — " + label + ".");
        trimHistory(next);
        return next;
    }

    private static LifeState applyChoiceEffects(LifeState next, String id) {
        switch (id) {
            case "family_time": next.happiness += 3; next.health += 1; next.charisma += 1; break;
            case "creative_play": next.happiness += 2; next.charisma += 2; break;
            case "rest": next.health += 3; next.happiness += 2; break;
            case "focus_study": case "study_group":
                next.intelligence += 4; next.discipline += 2; next.happiness -= 1; break;
            case "join_arts": next.happiness += 3; next.charisma += 2; next.popularity += 1; break;
            case "sports_group": next.health += 2; next.happiness += 3; next.charisma += 1; break;
            case "professional_course": next.intelligence += 3; next.discipline += 2; next.happiness += 1; break;
            case "social_network": next.charisma += 3; next.popularity += 2; next.happiness += 1; break;
            case "work_shift":
                next.moneyCents = safeAdd(next.moneyCents, 7500L);
                next.discipline += 1; next.health -= 1; break;
            case "community": next.happiness += 3; next.popularity += 1; break;
            case "personal_hobby": next.happiness += 2; next.intelligence += 1; break;
            default: throw new IllegalArgumentException("Resultado de escolha não reconhecido.");
        }
        clampStats(next);
        return next;
    }

    private static void clampStats(LifeState life) {
        life.happiness = clamp(life.happiness); life.health = clamp(life.health);
        life.intelligence = clamp(life.intelligence); life.charisma = clamp(life.charisma);
        life.discipline = clamp(life.discipline); life.popularity = clamp(life.popularity);
    }

    private static long safeAdd(long current, long addition) {
        if (current > Long.MAX_VALUE - addition) return Long.MAX_VALUE;
        return current + addition;
    }

    private static void trimHistory(LifeState life) {
        while (life.history.size() > 500) life.history.remove(0);
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