package com.example.chatapp.allPage.mainActivity

import android.app.Application
import androidx.annotation.NonNull
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

open class BaseAndroidViewModel(@NonNull application: Application) :
    AndroidViewModel(application) {


    private val disposable = CompositeDisposable()
    val errorMessage = MutableLiveData<String>()
    val isLoading = MutableLiveData<Boolean>()
    val defaultErrorMessage = "連線異常 請確認網路狀態請稍後再試 !"
//    val service = ApiService.getInstance()


    fun <T> addDisposable(single: Single<T>, listener: DisposableSingleObserver<T>) {
        disposable.add(
            single
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(listener)
        )
    }

    override fun onCleared() {
        disposable.clear()
    }
}