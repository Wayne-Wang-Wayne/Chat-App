package com.example.chatapp.util


import android.content.Context
import android.media.Image
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import com.example.chatapp.model.OpenGraphResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.lockAndWaitNanos

import org.jsoup.Jsoup

class PreviewLinkUtil {

    private val AGENT = "Mozilla"
    private val REFERRER = "http://www.google.com"
    private val TIMEOUT = 10000
    private val DOC_SELECT_QUERY = "meta[property^=og:]"
    private val OPEN_GRAPH_KEY = "content"
    private val PROPERTY = "property"
    private val OG_IMAGE = "og:image"
    private val OG_DESCRIPTION = "og:description"
    private val OG_URL = "og:url"
    private val OG_TITLE = "og:title"
    private val OG_SITE_NAME = "og:site_name"
    private val OG_TYPE = "og:type"

    fun getLinkPreviewData(
        context: Context,
        passInUrl: String,
        iv_urlImage: ImageView,
        tv_urlTitle: TextView,
        tv_urlDescription: TextView
    ) {

        var url = passInUrl

        if (!url.contains("http")) {
            url = "http://$url"
        }

        val openGraphResult = OpenGraphResult()

        CoroutineScope(IO).launch {
            try {

                val response = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .userAgent(AGENT)
                    .referrer(REFERRER)
                    .timeout(TIMEOUT)
                    .followRedirects(true)
                    .execute()

                val doc = response.parse()

                val ogTags = doc.select(DOC_SELECT_QUERY)
                when {
                    ogTags.size > 0 ->
                        ogTags.forEachIndexed { index, _ ->
                            val tag = ogTags[index]
                            val text = tag.attr(PROPERTY)

                            when (text) {
                                OG_IMAGE -> {
                                    openGraphResult!!.image = (tag.attr(OPEN_GRAPH_KEY))
                                }
                                OG_DESCRIPTION -> {
                                    openGraphResult!!.description = (tag.attr(OPEN_GRAPH_KEY))
                                }
                                OG_URL -> {
                                    openGraphResult!!.url = (tag.attr(OPEN_GRAPH_KEY))
                                }
                                OG_TITLE -> {
                                    openGraphResult!!.title = (tag.attr(OPEN_GRAPH_KEY))
                                }
                                OG_SITE_NAME -> {
                                    openGraphResult!!.siteName = (tag.attr(OPEN_GRAPH_KEY))
                                }
                                OG_TYPE -> {
                                    openGraphResult!!.type = (tag.attr(OPEN_GRAPH_KEY))
                                }
                            }
                        }
                }
                val uriString = openGraphResult.image?.toUri()!!

                launch(Main) {
                    // 回到Main Thread Set View
                    if (iv_urlImage.parent != null && tv_urlTitle.parent != null && tv_urlDescription.parent != null) {
                        SmallUtil.glideNoCutPicture(
                            context,
                            uriString,
                            iv_urlImage
                        )
                        tv_urlTitle.text = openGraphResult.title
                        tv_urlDescription.text = openGraphResult.description

                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                launch(Main) {
                    //listener.onError(e.localizedMessage)
                }
            }
        }
    }


}