package com.example.chatapp.allPage.MediaActivity

import android.app.DownloadManager
import android.content.res.Configuration
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.example.chatapp.R
import com.example.chatapp.allPage.MediaActivity.pagerAdapter.PageFragmentAdapter
import com.example.chatapp.customStuff.SafeClickListener.Companion.setSafeOnClickListener
import com.example.chatapp.recyclerviewAdapter.ChatRecyclerviewAdapter
import com.example.chatapp.recyclerviewAdapter.ChatRecyclerviewAdapter.Companion.OPEN_MEDIA_INDEX_KEY
import com.example.chatapp.util.SmallUtil
import kotlinx.android.synthetic.main.activity_media.*
import java.io.File

class MyMediaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media)

        initViewPager()

        media_detail_back_press.setSafeOnClickListener { finish() }
    }

    val mFragments = ArrayList<Fragment>()

    private fun initViewPager() {

        val bundle = intent.extras
        val position = bundle?.getInt(OPEN_MEDIA_INDEX_KEY)
        val uriList = bundle?.getStringArrayList(ChatRecyclerviewAdapter.OPEN_MEDIA_LIST_KEY)
        for (uri in uriList!!) {
            if (uri.substring(0, 6) == "video_") {
                mFragments.add(VideoPlayerFragment.newInstance(uri.substring(6)))
            }
            if (uri.substring(0, 6) == "image_") {
                mFragments.add(ImageDisplayFragment.newInstance(uri.substring(6)))
            }
        }
        val mPageFragmentAdapter = PageFragmentAdapter(supportFragmentManager, mFragments)
        fragmentViewPager.adapter = mPageFragmentAdapter
        fragmentViewPager.offscreenPageLimit = 0
        fragmentViewPager.currentItem = position!!
        media_title_text.text = "${position + 1} / ${mFragments.size}"
        btn_downLoad_media.setSafeOnClickListener {
            //todo 下載音樂或影片邏輯
        }
        fragmentViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                media_title_text.text = "${position + 1} / ${mFragments.size}"
                btn_downLoad_media.setSafeOnClickListener {
                    //todo 下載音樂或影片邏輯
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        println("IN onConfigurationChanged()")

        val newOrientation = newConfig.orientation
        if (newOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            supportActionBar?.hide()
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            media_activity_toolbar.visibility = View.GONE
        } else {
            supportActionBar?.show()
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            media_activity_toolbar.visibility = View.VISIBLE
        }
    }

    private fun saveToGallery(pictureUri: Uri) {
        try {
            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(pictureUri)
            val timeStamp = SmallUtil.getCurrentTimeStamp().toString()
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle("chat_app_downloaded_image$timeStamp")
                .setMimeType("image/jpeg") // Your file type. You can use this code to download other file types also.
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_PICTURES,
                    File.separator + "chat_app_downloaded_image$timeStamp" + ".jpg"
                )
            dm.enqueue(request)
            Toast.makeText(this, "開始下載圖片！", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "圖片下載失敗！", Toast.LENGTH_SHORT).show()
        }
    }
}