package com.newshunt.adengine.model;

import com.newshunt.adengine.model.entity.BaseAdEntity;

/**
 * To notify that ad is ready to be shown
 *
 * @author heena.arora
 */
public interface AdReadyHandler {
  void onReady(BaseAdEntity baseAdEntity);
}
