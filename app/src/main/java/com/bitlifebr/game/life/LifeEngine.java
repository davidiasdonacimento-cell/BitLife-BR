package com.bitlifebr.game.life;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Motor de eventos do BitLife BR.
 * Os acontecimentos são escolhidos por vida/idade/estado, não por uma sequência fixa
 * de quatro textos. O mesmo save sempre recebe o mesmo evento até tomar uma decisão.
 */
public final class LifeEngine {
    private LifeEngine() { }

    private static final Scenario[][] SCENARIOS = {
        {
            s("Um passeio diferente", "Sua família planejou um passeio em {{city}}. Como você participa?", "family_time", "Aproveitar com a família", "creative_play", "Explorar o lugar brincando", "rest", "Ficar em um ritmo tranquilo"),
            s("Uma nova descoberta", "Um objeto curioso chamou sua atenção em casa. O que fazer?", "creative_play", "Investigar e brincar", "family_time", "Chamar alguém da família", "rest", "Descansar"),
            s("Dia de visita", "Parentes apareceram para uma visita. Como aproveitar?", "family_time", "Ficar perto dos parentes", "join_arts", "Mostrar uma atividade criativa", "rest", "Tirar um tempo tranquilo"),
            s("Uma atividade no bairro", "Há uma atividade infantil acontecendo no bairro. Qual será sua escolha?", "creative_play", "Participar da brincadeira", "family_time", "Ir acompanhado da família", "rest", "Ficar em casa"),
            s("Uma pequena comemoração", "Sua família quer comemorar uma data especial. O que você prefere?", "family_time", "Participar da comemoração", "join_arts", "Preparar algo artístico", "rest", "Ter um dia calmo"),
            s("Hora de aprender", "Você demonstrou curiosidade por algo novo. Como explorar?", "creative_play", "Experimentar na prática", "family_time", "Aprender com a família", "rest", "Observar e descansar"),
            s("Um dia ao ar livre", "O clima está agradável em {{city}}. Como passar o dia?", "sports_group", "Brincar e se movimentar", "family_time", "Fazer um passeio em família", "rest", "Descansar"),
            s("Uma história para contar", "Alguém da família quer compartilhar uma história com você.", "family_time", "Ouvir e conversar", "creative_play", "Inventar uma continuação", "rest", "Relaxar")
        },
        {
            s("Uma novidade na escola", "A escola abriu uma atividade diferente. Qual caminho seguir?", "focus_study", "Conhecer o projeto", "join_arts", "Entrar na oficina de arte", "sports_group", "Experimentar o esporte"),
            s("Uma amizade nova", "Uma criança da turma quer fazer amizade. Como agir?", "social_network", "Conversar e se aproximar", "focus_study", "Convidar para estudar", "sports_group", "Chamar para jogar"),
            s("Feira de ciências", "Sua turma vai apresentar trabalhos em uma feira. O que escolher?", "focus_study", "Pesquisar e apresentar", "join_arts", "Criar a parte visual", "family_time", "Preparar com a família"),
            s("Campeonato da escola", "A escola organizou um campeonato entre turmas.", "sports_group", "Participar da equipe", "focus_study", "Ajudar na organização", "join_arts", "Criar cartazes e torcida"),
            s("Um talento escondido", "Você percebeu que gosta de uma atividade nova.", "join_arts", "Praticar arte e criatividade", "sports_group", "Testar uma modalidade esportiva", "focus_study", "Pesquisar mais sobre o assunto"),
            s("Trabalho em grupo", "Um trabalho escolar precisa ser dividido entre os colegas.", "focus_study", "Organizar a pesquisa", "social_network", "Coordenar a equipe", "join_arts", "Preparar a apresentação"),
            s("Convite para brincar", "Colegas chamaram você para uma atividade depois da aula.", "sports_group", "Participar da brincadeira", "social_network", "Conhecer melhor o grupo", "focus_study", "Terminar as tarefas primeiro"),
            s("Uma apresentação", "Sua turma terá um momento para mostrar o que aprendeu.", "join_arts", "Preparar uma apresentação", "focus_study", "Aprofundar o conteúdo", "family_time", "Ensaiar com a família")
        },
        {
            s("Escolhas para o futuro", "Uma conversa sobre o futuro fez você pensar nos próximos passos.", "study_group", "Montar um plano de estudos", "sports_group", "Entrar em uma equipe", "social_network", "Ampliar seu círculo social"),
            s("Um convite dos colegas", "Um grupo está organizando uma atividade no fim de semana.", "social_network", "Participar e conhecer pessoas", "sports_group", "Entrar na atividade esportiva", "rest", "Reservar tempo para descansar"),
            s("Projeto importante", "Um projeto pode ajudar a desenvolver suas habilidades.", "study_group", "Assumir a pesquisa", "join_arts", "Cuidar da criação", "social_network", "Coordenar a comunicação"),
            s("Preparação para provas", "As avaliações estão chegando e você precisa organizar a rotina.", "study_group", "Criar um cronograma", "sports_group", "Equilibrar estudo e esporte", "rest", "Reduzir compromissos"),
            s("Uma oportunidade cultural", "Uma atividade cultural gratuita está recebendo inscrições.", "join_arts", "Participar da oficina", "social_network", "Fazer novas amizades", "study_group", "Pesquisar o tema antes"),
            s("Desafio esportivo", "Uma equipe local convidou você para treinar.", "sports_group", "Aceitar o desafio", "study_group", "Priorizar a escola", "social_network", "Conhecer o grupo primeiro"),
            s("Planos de independência", "Você começou a pensar em objetivos para os próximos anos.", "study_group", "Pesquisar cursos e profissões", "social_network", "Conversar com pessoas experientes", "rest", "Organizar suas prioridades"),
            s("Mudanças no grupo", "Seu grupo de amigos está passando por mudanças.", "social_network", "Conversar e fortalecer amizades", "sports_group", "Focar em uma atividade em equipe", "study_group", "Dedicar mais tempo aos estudos")
        },
        {
            s("Uma proposta de trabalho", "Uma oportunidade de renda apareceu em {{city}}. O que fazer?", "work_shift", "Aceitar o trabalho extra", "professional_course", "Investir em qualificação", "social_network", "Conversar com contatos"),
            s("Planos de carreira", "Você pode dar um passo para desenvolver sua carreira.", "professional_course", "Fazer um curso", "work_shift", "Buscar renda extra", "social_network", "Ampliar contatos profissionais"),
            s("Uma mudança em casa", "Sua rotina familiar está mudando e exige organização.", "family_time", "Priorizar a família", "work_shift", "Aumentar a renda", "rest", "Reorganizar a rotina"),
            s("Oportunidade de negócio", "Uma ideia de pequeno negócio começou a ganhar forma.", "professional_course", "Estudar gestão", "social_network", "Procurar parceiros e contatos", "work_shift", "Fazer um trabalho extra para juntar dinheiro"),
            s("Convite para um evento", "Um evento local pode abrir novas oportunidades.", "social_network", "Participar e conhecer pessoas", "professional_course", "Buscar aprendizado", "rest", "Preservar seu tempo livre"),
            s("Meta financeira", "Você definiu uma meta financeira para o próximo período.", "work_shift", "Fazer trabalho extra", "professional_course", "Melhorar suas habilidades", "family_time", "Planejar com a família"),
            s("Um projeto pessoal", "Um projeto antigo pode finalmente sair do papel.", "professional_course", "Aprender o que falta", "social_network", "Buscar apoio e contatos", "rest", "Planejar sem pressa"),
            s("Equilíbrio na rotina", "Você percebeu que precisa equilibrar trabalho e vida pessoal.", "family_time", "Reservar tempo para pessoas próximas", "rest", "Cuidar do descanso", "work_shift", "Aceitar uma renda extra")
        },
        {
            s("Um projeto comunitário", "A comunidade de {{city}} está organizando uma iniciativa.", "community", "Participar como voluntário", "personal_hobby", "Contribuir com uma habilidade", "family_time", "Convidar a família"),
            s("Um hobby esquecido", "Você encontrou tempo para retomar um antigo interesse.", "personal_hobby", "Retomar o hobby", "family_time", "Compartilhar com a família", "community", "Participar de um grupo local"),
            s("Reencontro especial", "Uma pessoa querida entrou em contato para um reencontro.", "family_time", "Organizar o encontro", "community", "Participar de uma atividade em grupo", "personal_hobby", "Reservar um tempo para si"),
            s("Experiência para compartilhar", "Pessoas mais jovens querem ouvir suas experiências.", "community", "Compartilhar conhecimentos", "personal_hobby", "Registrar suas histórias", "family_time", "Conversar com parentes"),
            s("Uma nova rotina", "Você está pensando em dar mais variedade aos seus dias.", "personal_hobby", "Começar uma atividade nova", "community", "Conhecer um grupo", "rest", "Priorizar o descanso"),
            s("Celebração em família", "Sua família está planejando uma reunião especial.", "family_time", "Participar da reunião", "personal_hobby", "Preparar algo de que gosta", "community", "Convidar pessoas da comunidade"),
            s("Uma causa local", "Uma causa importante para o bairro precisa de apoio.", "community", "Ajudar na iniciativa", "family_time", "Mobilizar a família", "personal_hobby", "Contribuir com uma habilidade"),
            s("Aprendizado contínuo", "Surgiu a chance de aprender algo que sempre despertou curiosidade.", "personal_hobby", "Começar a aprender", "community", "Entrar em um grupo de aprendizagem", "family_time", "Aprender com alguém da família")
        }
    };

    private static Scenario s(String title, String story,
            String a, String al, String b, String bl, String c, String cl) {
        return new Scenario(title, story, a, al, b, bl, c, cl);
    }

    public static LifeEvent createAnnualEvent(LifeState life) {
        if (life == null) throw new IllegalArgumentException("Vida inválida.");
        int age = Math.max(0, Math.min(120, life.age));
        int stage = age < 6 ? 0 : age < 12 ? 1 : age < 18 ? 2 : age < 60 ? 3 : 4;
        int slot = Math.floorMod(eventSeed(life) + age, SCENARIOS[stage].length);
        Scenario event = SCENARIOS[stage][slot];
        String city = life.city == null || life.city.trim().isEmpty() ? "sua cidade" : life.city;
        String description = event.story.replace("{{city}}", city)
                + " Você tem " + age + " anos, mora em " + city + " (" + life.stateCode + ").";
        if (life.happiness < 35) description += " Você também tem tentado recuperar o ânimo.";
        if (life.health < 35) description += " Seu bem-estar merece atenção nesta fase.";
        if (life.intelligence > 75) description += " Sua dedicação ao aprendizado está se destacando.";
        if (life.moneyCents > 1000000L) description += " Sua situação financeira permite considerar mais possibilidades.";
        if (!life.siblings.isEmpty()) description += " Seus irmãos também fazem parte da sua rotina familiar.";
        String prefix = new String[]{"child", "school", "teen", "adult", "senior"}[stage];
        String eventId = prefix + "_" + life.id + "_" + age + "_" + slot;
        String title = event.title + " • " + age + " anos";
        return new LifeEvent(life.id, age, eventId, title, description,
                event.aId, event.aLabel, event.bId, event.bLabel, event.cId, event.cLabel);
    }

    private static int eventSeed(LifeState life) {
        int hash = 17;
        hash = 31 * hash + (life.id == null ? 0 : life.id.hashCode());
        hash = 31 * hash + (life.city == null ? 0 : life.city.hashCode());
        hash = 31 * hash + life.siblings.size();
        hash = 31 * hash + (life.stateCode == null ? 0 : life.stateCode.hashCode());
        // A história recente influencia o próximo acontecimento, evitando
        // a mesma sequência previsível sempre que a idade avança.
        int start = Math.max(0, life.history.size() - 4);
        for (int i = start; i < life.history.size(); i++) {
            String item = life.history.get(i);
            hash = 31 * hash + (item == null ? 0 : item.hashCode());
        }
        hash = 31 * hash + (int) (life.createdAt ^ (life.createdAt >>> 32));
        return hash ^ (hash >>> 16);
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

    private static final class Scenario {
        final String title, story, aId, aLabel, bId, bLabel, cId, cLabel;
        Scenario(String title, String story, String aId, String aLabel,
                 String bId, String bLabel, String cId, String cLabel) {
            this.title = title; this.story = story;
            this.aId = aId; this.aLabel = aLabel; this.bId = bId; this.bLabel = bLabel;
            this.cId = cId; this.cLabel = cLabel;
        }
    }
}
