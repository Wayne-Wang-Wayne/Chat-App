package com.example.chatapp.allPage.mainActivity

import android.app.Application
import android.content.Context
import androidx.annotation.NonNull
import androidx.lifecycle.MutableLiveData
import com.example.chatapp.R
import io.reactivex.Single
import io.reactivex.observers.DisposableSingleObserver
import org.xmlpull.v1.XmlPullParser

class BaseViewPagerViewModel(@NonNull application: Application) : BaseAndroidViewModel(application) {
    private val TAG: String = javaClass.simpleName
    val newsTabs = MutableLiveData<List<BaseTabsModel>>()

    fun fetchNewsTabs(){
        isLoading.value = true
        addDisposable(getTabs(getApplication()),object :DisposableSingleObserver<List<BaseTabsModel>>(){
            override fun onSuccess(t: List<BaseTabsModel>) {
                newsTabs.value = t
                isLoading.value = false
            }
            override fun onError(e: Throwable) {
                errorMessage.value = defaultErrorMessage
                isLoading.value = false
            }



        })
    }

     private fun getTabs(context: Context): Single<List<BaseTabsModel>> {
         return Single.create {
             val newsTabsModels = ArrayList<BaseTabsModel>()
             val res = context.resources
             val xrp = res.getXml(R.xml.news_tabs)
             try {
                 var eventType = xrp.eventType
                 while (eventType != XmlPullParser.END_DOCUMENT) {
                     if (eventType == XmlPullParser.START_TAG) {
                         if (xrp.name == "item") {
                             val newsTabsModel = BaseTabsModel()
                             newsTabsModel.id = xrp.getAttributeValue(null, "id")
                             newsTabsModel.title = xrp.getAttributeValue(null, "title")
                             newsTabsModels.add(newsTabsModel)
                         }

                     }
                     eventType = xrp.next()
                 }
                 xrp.close()
             } catch (e: Exception) {
             }
             it.onSuccess(newsTabsModels)

         }
     }
}

