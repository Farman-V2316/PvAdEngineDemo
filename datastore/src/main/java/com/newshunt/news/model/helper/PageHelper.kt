package com.newshunt.news.model.helper

import com.newshunt.news.model.usecase.DefaultHomePageUsecase
import com.newshunt.news.model.usecase.toMediator2

object PageHelper {

	@JvmStatic
	fun insertDefaultPages() {
		DefaultHomePageUsecase().toMediator2().execute(Any())
	}
}