package com.newshunt.adengine.model.entity

/**
 * Empty ad with only beacon url.
 * The empty ad will have properties like position, next position etc and will be treated as any
 * other ad on the UI
 *
 * @author heena.arora
 */
class EmptyAd : BaseDisplayAdEntity()

/**
 * Representation of AD for SDK no-fill or network error.
 */
class NoFillOrErrorAd: BaseDisplayAdEntity()