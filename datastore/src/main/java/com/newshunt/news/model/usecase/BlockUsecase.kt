package com.newshunt.news.model.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.newshunt.news.model.sqlite.SocialDB

class BlockUsecase: MediatorUsecase<Any, Int> {

	private val _data = MediatorLiveData<Result0<Int>>()
	private val _status = MutableLiveData<Boolean>()

	override fun execute(t: Any): Boolean {
		_data.addSource(SocialDB.instance().followEntityDao().getBlockCount()) {
			_data.value = Result0.success(it)
		}
		return true
	}

	override fun data(): LiveData<Result0<Int>> {
		return _data
	}

	override fun status(): LiveData<Boolean> {
		return _status
	}

	override fun dispose() {

	}
}

fun <T> transform (f: LiveData<Result0<T>>) = Transformations.map(f) {
	it.getOrNull()
}