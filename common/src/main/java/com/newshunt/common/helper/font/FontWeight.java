/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.font;

/**
 * Custom Font weight enum
 *
 * Created by atul.anand on 07/28/22.
 */
public enum FontWeight {
  NORMAL(400, 0),
  MEDIUM(500, 1),
  SEMIBOLD(600, 2),
  BOLD(700, 3),
  EXTRABOLD(800, 4),
  NOT_DEFINED(-1, -1);

  private int weight;
  private int weightEnumValue;

  FontWeight(int weight, int weightEnumValue){
    this.weight = weight;
    this.weightEnumValue = weightEnumValue;
  }

  public int getWeightEnumValue() {
    return weightEnumValue;
  }

  public static FontWeight getFontWeightForEnumValue(int weightEnumValue){
    FontWeight res = FontWeight.NORMAL;
    for(FontWeight fontWeight: FontWeight.values()){
      if(fontWeight.weightEnumValue == weightEnumValue){
        res = fontWeight;
        break;
      }
    }
    return res;
  }
}
