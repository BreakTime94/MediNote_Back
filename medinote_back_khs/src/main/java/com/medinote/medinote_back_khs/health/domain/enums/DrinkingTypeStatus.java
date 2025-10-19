package com.medinote.medinote_back_khs.health.domain.enums;

public enum DrinkingTypeStatus {
  SOJU("소주"),
  BEER("맥주"),
  WINE("와인"),
  WHISKY("위스키"),
  MAKGEOLLI("막걸리"),
  COCKTAIL("칵테일"),
  ETC("기타");

  private final String label;

  DrinkingTypeStatus(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
