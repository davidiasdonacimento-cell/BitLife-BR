package com.bitlifebr.game.life;

/** Evento de vida com três decisões reais e identificador ligado à idade/personagem. */
public final class LifeEvent {
    public final String lifeId;
    public final int ageAtCreation;
    public final String id;
    public final String title;
    public final String description;
    public final String choiceAId;
    public final String choiceALabel;
    public final String choiceBId;
    public final String choiceBLabel;
    public final String choiceCId;
    public final String choiceCLabel;

    public LifeEvent(String id, String title, String description,
                     String choiceAId, String choiceALabel,
                     String choiceBId, String choiceBLabel) {
        this(null, -1, id, title, description,
                choiceAId, choiceALabel, choiceBId, choiceBLabel,
                "rest", "Tirar um tempo para descansar");
    }

    public LifeEvent(String lifeId, int ageAtCreation, String id, String title,
                     String description, String choiceAId, String choiceALabel,
                     String choiceBId, String choiceBLabel) {
        this(lifeId, ageAtCreation, id, title, description,
                choiceAId, choiceALabel, choiceBId, choiceBLabel,
                "rest", "Tirar um tempo para descansar");
    }

    public LifeEvent(String lifeId, int ageAtCreation, String id, String title,
                     String description, String choiceAId, String choiceALabel,
                     String choiceBId, String choiceBLabel,
                     String choiceCId, String choiceCLabel) {
        this.lifeId = lifeId;
        this.ageAtCreation = ageAtCreation;
        this.id = id;
        this.title = title;
        this.description = description;
        this.choiceAId = choiceAId;
        this.choiceALabel = choiceALabel;
        this.choiceBId = choiceBId;
        this.choiceBLabel = choiceBLabel;
        this.choiceCId = choiceCId;
        this.choiceCLabel = choiceCLabel;
    }

    public boolean accepts(String choiceId) {
        return choiceAId.equals(choiceId) || choiceBId.equals(choiceId)
                || choiceCId.equals(choiceId);
    }
}