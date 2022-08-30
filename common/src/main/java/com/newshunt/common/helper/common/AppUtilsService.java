/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.common;

import java.util.Map;

/**
 * Created by anshul on 05/02/18.
 * A class for providing app level functionality to lower level modules
 */

public interface AppUtilsService {

  String getShareableString(String shareUrl, String title,
                            Map<String, String> langTitles,
                            boolean displayViaDailyhunt);

}
