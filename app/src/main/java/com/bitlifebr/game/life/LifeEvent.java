package com.bitlifebr.game.life;

/** Evento anual com duas escolhas e resultados calculados pelo motor do jogo. */
public final class LifeEvent {
    public final String id;
    public final String title;
    public final String description;
    public final String choiceAId;
    public final String choiceALabel;
    public final String choiceBId;
    public final String choiceBLabel;

    public LifeEvent(String id, String title, String description,
                     String choiceAId, String choiceALabel,
                     String choiceBId, String choiceBLabel) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.choiceAId = choiceAId;
        this.choiceALabel = choiceALabel;
        this.choiceBId = choiceBId;
        this.choiceBLabel = choiceBLabel;
    }
}