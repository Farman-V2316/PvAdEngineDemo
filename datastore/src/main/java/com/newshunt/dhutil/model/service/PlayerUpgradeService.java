package com.newshunt.dhutil.model.service;

import com.newshunt.dataentity.dhutil.model.entity.players.PlayerUpgradeInfo;

import io.reactivex.Single;

public interface PlayerUpgradeService {
  Single<PlayerUpgradeInfo> getPlayerUpgradeResponse();
  Single<PlayerUpgradeInfo> getPlayerInfoLocal();
}
