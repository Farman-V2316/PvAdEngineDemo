package com.newshunt.adengine.model.entity

/**
 * @author raunak.yadav
 */
enum class AdErrorType(var value: Int) {
    NO_INTERNET(101001),
    AD_LOAD_TIMEOUT(101002),
    AD_LOAD_ERROR(101003),
    AD_NO_VAST_TAG_URL(101004),
    MALFORMED_CLICK_URL(101005),
    INSTREAM_NO_SKIP(101006),
    OUTSTREAM_SKIP(101007)
}