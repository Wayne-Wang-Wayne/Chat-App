package com.example.chatapp.allPage.pictureDetailActivity

import android.app.DownloadManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.chatapp.R
import com.example.chatapp.customStuff.SafeClickListener.Companion.setSafeOnClickListener
import com.example.chatapp.util.SmallUtil
import com.example.chatapp.util.SmallUtil.glideNoCutPicture
import com.example.chatapp.util.SmallUtil.glideNormalUtil
import kotlinx.android.synthetic.main.activity_picture_detail.*
import java.io.File


class PictureDetailActivity : AppCompatActivity() {

    companion object {
        const val getDetailPictureKey = "998974"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture_detail)

        setView()
    }

    private fun setView() {
        picture_detail_back_press.setSafeOnClickListener { finish() }
        val pictureUri = intent.extras?.getString(getDetailPictureKey)?.toUri()
        glideNoCutPicture(this, pictureUri!!, iv_image_detail)
        btn_downLoad_image.setSafeOnClickListener {
            saveToGallery(pictureUri)
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