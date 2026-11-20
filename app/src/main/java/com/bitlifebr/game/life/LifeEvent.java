package com.bitlifebr.game.life;

/** Evento anual individual, vinculado à vida e à idade em que foi apresentado. */
public final class LifeEvent {
    public final String lifeId;
    public final int ageAtCreation;
    public final String id,title,description,choiceAId,choiceALabel,choiceBId,choiceBLabel;
    public LifeEvent(String id,String title,String description,String choiceAId,String choiceALabel,String choiceBId,String choiceBLabel){
        this(null,-1,id,title,description,choiceAId,choiceALabel,choiceBId,choiceBLabel);
    }
    public LifeEvent(String lifeId,int ageAtCreation,String id,String title,String description,
                     String choiceAId,String choiceALabel,String choiceBId,String choiceBLabel){
        this.lifeId=lifeId;this.ageAtCreation=ageAtCreation;this.id=id;this.title=title;this.description=description;
        this.choiceAId=choiceAId;this.choiceALabel=choiceALabel;this.choiceBId=choiceBId;this.choiceBLabel=choiceBLabel;
    }
}