package com.newshunt.appview.common.postcreation.viewmodel

import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.newshunt.dataentity.common.asset.OEmbedResponse
import com.newshunt.dhutil.bundleOf
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.OEmbedUsecase

class OEmbedViewModel(val oembedMed: MediatorUsecase<Bundle, OEmbedResponse>): ViewModel()  {

    fun fetchOEmebedData(url: String){
        val bundle = bundleOf(Pair(OEmbedUsecase.OEMBED_URL, url))
        oembedMed.execute(bundle)
    }
}