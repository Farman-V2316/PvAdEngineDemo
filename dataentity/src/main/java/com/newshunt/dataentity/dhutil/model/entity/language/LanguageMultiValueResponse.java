/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.dhutil.model.entity.language;

import com.newshunt.dataentity.common.model.entity.language.Language;
import com.newshunt.dataentity.common.model.entity.model.MultiValueResponse;
import com.newshunt.dataentity.common.model.entity.BaseDataResponse;

/**
 * Encapsulates the response for list of languages.
 *
 * @author datta.vitore
 */

public class LanguageMultiValueResponse extends BaseDataResponse {
  private static final long serialVersionUID = -3536335410475834289L;

  private MultiValueResponse<Language> data;

  public LanguageMultiValueResponse() {
  }

  public LanguageMultiValueResponse(MultiValueResponse<Language> data) {
    this.data = data;
  }

  public MultiValueResponse<Language> getData() {
    return data;
  }

  public void setData(MultiValueResponse<Language> data) {
    this.data = data;
  }

}
