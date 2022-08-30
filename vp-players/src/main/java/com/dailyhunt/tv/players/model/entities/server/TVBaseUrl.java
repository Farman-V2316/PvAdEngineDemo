package com.dailyhunt.tv.players.model.entities.server;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.Serializable;

/**
 * @author rohit
 */
public class TVBaseUrl implements Serializable {
  private static final long serialVersionUID = 1806100607755821273L;

  private String apiUrl;

  public String getApiUrl() {
    return apiUrl;
  }

  public void setApiUrl(String apiUrl) {
    this.apiUrl = apiUrl;
  }

}
