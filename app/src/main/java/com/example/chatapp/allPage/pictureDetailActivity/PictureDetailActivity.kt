package com.example.chatapp.allPage.pictureDetailActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.net.toUri
import com.example.chatapp.R
import com.example.chatapp.customStuff.SafeClickListener.Companion.setSafeOnClickListener
import com.example.chatapp.util.SmallUtil
import com.example.chatapp.util.SmallUtil.glideNormalUtil
import kotlinx.android.synthetic.main.activity_picture_detail.*

class PictureDetailActivity : AppCompatActivity() {

    companion object{
        const val getDetailPictureKey = "998974"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture_detail)

        setView()
    }

    private fun setView(){
        picture_detail_back_press.setSafeOnClickListener { finish() }
        val pictureUri = intent.extras?.getString(getDetailPictureKey)?.toUri()
        glideNormalUtil(this,pictureUri!!,iv_image_detail)
    }
}