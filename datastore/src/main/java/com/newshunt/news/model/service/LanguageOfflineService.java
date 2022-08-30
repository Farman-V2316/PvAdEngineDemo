package com.newshunt.news.model.service;

import com.newshunt.dataentity.dhutil.model.entity.language.LanguageMultiValueResponse;

/**
 * Service to retrive offline cache data
 *
 * @author arun.babu
 */
public interface LanguageOfflineService {

  LanguageMultiValueResponse getLanguages(String edition);
}
