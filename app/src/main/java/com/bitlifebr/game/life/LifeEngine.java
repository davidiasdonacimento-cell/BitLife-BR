package com.bitlifebr.game.life;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Motor determinístico: cada idade recebe um acontecimento próprio e único. */
public final class LifeEngine {
    private LifeEngine() { }

    private static final String[][] TITLES = {
        {"Uma visita especial em casa", "Uma grande descoberta", "Um dia de brincadeiras", "Um momento com a família"},
        {"Uma nova amizade na vizinhança", "Uma descoberta no parquinho", "Uma atividade em família", "Um passeio inesquecível"},
        {"Uma curiosidade que virou aprendizado", "Uma festa diferente", "Um novo hábito em casa", "Um desafio divertido"},
        {"Uma apresentação na escola", "Um projeto de criatividade", "Uma atividade em equipe", "Um livro que chamou atenção"},
        {"Um projeto para a feira de ciências", "Uma amizade importante", "Uma atividade extracurricular", "Uma conquista nos estudos"},
        {"Uma responsabilidade nova", "Uma experiência fora da rotina", "Uma habilidade em desenvolvimento", "Um convite para participar"},
        {"Um novo desafio escolar", "Uma escolha para o tempo livre", "Uma oportunidade de aprender", "Uma atividade em grupo"},
        {"Um trabalho em equipe", "Uma meta pessoal", "Uma atividade cultural", "Uma decisão sobre estudos"},
        {"Uma oportunidade de voluntariado", "Uma nova matéria favorita", "Um projeto de longo prazo", "Uma amizade que se fortalece"},
        {"Uma escolha para o futuro", "Uma atividade que exige dedicação", "Um projeto com colegas", "Uma nova responsabilidade"},
        {"Uma decisão sobre os próximos estudos", "Uma experiência de autonomia", "Um objetivo que ganha forma", "Uma oportunidade de liderança"},
        {"Uma preparação para uma nova fase", "Uma escolha de formação", "Um desafio de organização", "Uma oportunidade na comunidade"},
        {"Um primeiro passo profissional", "Uma decisão sobre formação", "Uma oportunidade de independência", "Uma nova responsabilidade financeira"},
        {"Uma oportunidade de trabalho", "Um curso para ampliar conhecimentos", "Uma mudança na rotina", "Um projeto pessoal"},
        {"Uma escolha importante de carreira", "Uma oportunidade de qualificação", "Um contato profissional", "Um plano de longo prazo"},
        {"Uma mudança de planos", "Uma meta profissional", "Uma decisão financeira", "Uma oportunidade de colaboração"},
        {"Um novo projeto de vida", "Uma responsabilidade familiar", "Uma decisão sobre moradia", "Uma oportunidade profissional"}
    };

    private static final String[][] DESCRIPTIONS = {
        {"Sua família organiza um momento especial. Como você prefere aproveitar o dia?", "Uma novidade desperta sua curiosidade. O que chama sua atenção?", "Há tempo livre para brincar e descobrir algo novo. Qual será sua escolha?", "Pessoas próximas planejam uma atividade em conjunto. Como você participa?"},
        {"Você encontra outra criança com interesses parecidos. Como começa essa amizade?", "Um espaço de brincadeiras oferece novas atividades. O que você escolhe explorar?", "Sua família propõe um passeio. Que tipo de experiência combina com você?", "Uma rotina diferente abre espaço para aprender algo. Como você aproveita?"},
        {"Uma pergunta sua vira uma pequena descoberta. Como continua explorando?", "Uma comemoração reúne pessoas importantes. Como você participa?", "Uma atividade nova passa a fazer parte da rotina. O que prefere fazer?", "Um desafio divertido aparece em casa. Como você tenta resolvê-lo?"},
        {"A escola abre espaço para mostrar o que você aprendeu. Como se prepara?", "Você pode desenvolver uma ideia criativa com materiais simples. Qual caminho escolhe?", "Um trabalho coletivo depende da participação de todos. Como contribui?", "Uma leitura desperta interesse por um assunto novo. O que faz em seguida?"},
        {"A turma prepara uma feira de ciências. Como você decide participar?", "Uma amizade ganha importância durante o ano. Como cuida desse vínculo?", "A escola oferece atividades além das aulas. Qual combina com seus interesses?", "Um assunto chama sua atenção e você pode aprofundá-lo. Como começa?"},
        {"Você recebe uma pequena responsabilidade e pode organizar sua rotina.", "Surge uma experiência diferente que exige curiosidade e iniciativa.", "Uma habilidade sua pode evoluir com prática e orientação.", "Um grupo convida você para participar de uma atividade nova."},
        {"Um desafio escolar pede planejamento e colaboração. Como você se organiza?", "Você pode usar seu tempo livre para desenvolver um interesse.", "Uma oportunidade permite aprender com pessoas diferentes.", "Uma atividade em grupo precisa de alguém para ajudar a organizar."},
        {"Um trabalho em equipe apresenta opiniões diferentes. Como você contribui?", "Você decide uma meta que gostaria de alcançar neste ano.", "Uma atividade cultural apresenta novas ideias e experiências.", "Uma escolha de estudos pode abrir caminhos para o futuro."},
        {"Uma iniciativa local procura voluntários para uma atividade comunitária.", "Uma matéria desperta interesse e pode virar um estudo mais profundo.", "Um projeto exige constância e planejamento durante vários meses.", "Uma amizade pode se fortalecer com tempo e atenção."},
        {"Você começa a pensar com mais clareza nos próximos passos.", "Uma atividade exige dedicação e equilíbrio com outras responsabilidades.", "Colegas convidam você para desenvolver um projeto em conjunto.", "Uma responsabilidade nova exige organização e diálogo."},
        {"Você precisa comparar caminhos possíveis para continuar estudando.", "Uma experiência oferece mais autonomia e responsabilidade.", "Um objetivo pessoal começa a se transformar em um plano concreto.", "Uma atividade permite exercitar liderança e cooperação."},
        {"Uma nova fase se aproxima e pede preparação.", "Você pode escolher uma formação que combine com seus interesses.", "Organizar compromissos ajuda a equilibrar prioridades.", "Uma ação comunitária oferece espaço para contribuir."},
        {"Uma oportunidade inicial permite conhecer o mundo profissional.", "Um curso pode desenvolver uma habilidade útil para seus planos.", "A rotina muda e você precisa reorganizar seus compromissos.", "Um projeto pessoal começa a exigir planejamento."},
        {"Uma proposta de trabalho ou estágio aparece para avaliação.", "Você encontra uma formação que pode ampliar suas possibilidades.", "Um contato profissional compartilha uma oportunidade interessante.", "Um plano importante precisa de etapas e prioridades."},
        {"Uma decisão de carreira pede reflexão sobre objetivos e rotina.", "Uma qualificação pode ajudar no desenvolvimento profissional.", "Uma conversa amplia sua rede de contatos.", "Um plano de longo prazo exige consistência."},
        {"Uma mudança inesperada pede adaptação e organização.", "Uma meta profissional pode receber novos passos.", "Uma escolha financeira precisa considerar prioridades.", "Uma colaboração pode abrir espaço para um projeto."},
        {"Um projeto pessoal ganha importância e pede dedicação.", "Uma questão familiar exige conversa e planejamento.", "Uma decisão sobre moradia envolve orçamento e necessidades.", "Uma oportunidade profissional merece avaliação cuidadosa."}
    };

    public static LifeEvent createAnnualEvent(LifeState life) {
        if (life == null) throw new IllegalArgumentException("Vida inválida.");
        int age = Math.max(0, Math.min(120, life.age));
        int group;
        String prefix, choiceAId, choiceALabel, choiceBId, choiceBLabel;
        if (age < 6) {
            group=0; prefix="family"; choiceAId="family_time"; choiceALabel="Passar mais tempo em família";
            choiceBId="creative_play"; choiceBLabel="Explorar brincadeiras e atividades";
        } else if (age < 12) {
            group=Math.min(4,1+age-6); prefix="school"; choiceAId="focus_study";
            choiceALabel="Dedicar mais tempo aos estudos"; choiceBId="join_arts"; choiceBLabel="Participar de atividades e artes";
        } else if (age < 18) {
            group=Math.min(11,6+age-12); prefix="teen"; choiceAId="sports_group";
            choiceALabel="Participar de uma atividade esportiva"; choiceBId="study_group"; choiceBLabel="Entrar em um grupo de estudos";
        } else if (age < 60) {
            group=12+Math.floorMod(age-18,4); prefix="adult"; choiceAId="professional_course";
            choiceALabel="Fazer um curso de qualificação"; choiceBId="social_network"; choiceBLabel="Conhecer pessoas e ampliar contatos";
        } else {
            group=16; prefix="senior"; choiceAId="community"; choiceALabel="Participar de uma atividade comunitária";
            choiceBId="personal_hobby"; choiceBLabel="Dedicar tempo a um hobby";
        }
        int slot=Math.floorMod((life.id==null?0:life.id.hashCode())+age*31,4);
        String title=TITLES[group][slot]+" — aos "+age+" anos";
        String description=DESCRIPTIONS[group][slot]+" Você está com "+age+" anos. Escolha como deseja conduzir este momento.";
        return new LifeEvent(life.id,age,prefix+"_"+age+"_"+slot,title,description,
                choiceAId,choiceALabel,choiceBId,choiceBLabel);
    }

    public static LifeState applyChoice(LifeState current,LifeEvent event,String choiceId){
        if(current==null||event==null)throw new IllegalArgumentException("Vida ou evento inválido.");
        if(event.ageAtCreation>=0&&(event.ageAtCreation!=current.age
                ||(event.lifeId!=null&&!event.lifeId.equals(current.id))))
            throw new IllegalArgumentException("Este evento já foi usado ou pertence a outra vida. Atualize a tela.");
        boolean first=event.choiceAId.equals(choiceId),second=event.choiceBId.equals(choiceId);
        if(!first&&!second)throw new IllegalArgumentException("Escolha não pertence ao evento.");
        if(current.age>=120)throw new IllegalArgumentException("Esta vida já chegou ao limite de idade.");
        LifeState next=current.copy();next.age=current.age+1;next.birthDate=advanceBirthDate(current.birthDate);
        if("family_time".equals(choiceId)){next.happiness+=3;next.health+=1;}
        else if("creative_play".equals(choiceId)){next.happiness+=2;next.charisma+=2;}
        else if("focus_study".equals(choiceId)){next.intelligence+=4;next.discipline+=2;next.happiness-=1;}
        else if("join_arts".equals(choiceId)){next.happiness+=3;next.charisma+=2;next.popularity+=1;}
        else if("sports_group".equals(choiceId)){next.health+=2;next.happiness+=3;next.charisma+=1;}
        else if("study_group".equals(choiceId)){next.intelligence+=4;next.discipline+=2;next.charisma+=1;}
        else if("professional_course".equals(choiceId)){next.intelligence+=3;next.discipline+=2;next.happiness+=1;}
        else if("social_network".equals(choiceId)){next.charisma+=3;next.popularity+=2;next.happiness+=1;}
        else if("community".equals(choiceId)){next.happiness+=3;next.popularity+=1;}
        else if("personal_hobby".equals(choiceId)){next.happiness+=2;next.intelligence+=1;}
        next.happiness=clamp(next.happiness);next.health=clamp(next.health);next.intelligence=clamp(next.intelligence);
        next.charisma=clamp(next.charisma);next.discipline=clamp(next.discipline);next.popularity=clamp(next.popularity);
        String selected=first?event.choiceALabel:event.choiceBLabel;
        next.history.add("Aos "+next.age+" anos: "+event.title+" — "+selected+".");
        while(next.history.size()>500)next.history.remove(0);
        return next;
    }

    private static String advanceBirthDate(String value){
        if(value==null||value.trim().isEmpty())return value;
        SimpleDateFormat f=new SimpleDateFormat("dd/MM/yyyy",Locale.ROOT);f.setLenient(false);
        try{Date d=f.parse(value);if(d==null)return value;Calendar c=Calendar.getInstance();c.setTime(d);c.add(Calendar.YEAR,1);return f.format(c.getTime());}
        catch(ParseException ignored){return value;}
    }
    private static int clamp(int value){return Math.max(0,Math.min(100,value));}
}