package com.example.chatapp.allPage.myInfoActivity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import com.example.chatapp.R
import com.example.chatapp.customStuff.SafeClickListener.Companion.setSafeOnClickListener
import com.example.chatapp.model.User
import com.example.chatapp.util.FirebaseUtil
import com.example.chatapp.util.FirebaseUtil.Companion.ALL_USER
import com.example.chatapp.util.FirebaseUtil.Companion.getPictureFromFirebase
import com.example.chatapp.util.FirebaseUtil.Companion.mFirebaseAuthInstance
import com.example.chatapp.util.FirebaseUtil.Companion.mFirebaseRTDbInstance
import com.example.chatapp.util.FirebaseUtil.Companion.uploadProfileImage
import kotlinx.android.synthetic.main.activity_my_info.*

class MyInfoActivity : AppCompatActivity() {

    companion object {
        const val requestProfilePicturesCode = 999
        const val requestProfileBackgroundCode = 998
        const val MainPicture = "mainProfilePicture"
        const val BackgroundPicture = "backgroundProfilePicture"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_info)

        //customize status bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setView()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            requestProfilePicturesCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    val imageUir = data?.data
                    uploadProfileImage(
                        MainPicture,
                        this,
                        this,
                        iv_myProfilePicture,
                        imageUir!!,
                        profilePictureLoading
                    )
                }
            }
            requestProfileBackgroundCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    val imageUir = data?.data
                    uploadProfileImage(
                        BackgroundPicture,
                        this,
                        this,
                        iv_profile_background,
                        imageUir!!,
                        profilePictureLoading
                    )
                }
            }
        }

    }

    private fun setView() {

        mFirebaseRTDbInstance.child(ALL_USER).child(mFirebaseAuthInstance.currentUser?.uid!!).get()
            .addOnSuccessListener { snapShot ->
                val userData = snapShot.getValue(User::class.java)
                tv_profileName.text = userData?.name
                tv_profileMail.text = userData?.email
            }
        getPictureFromFirebase(this, iv_myProfilePicture, iv_profile_background)
        profilePictureGroup.setSafeOnClickListener {
            //打開相簿
            if (profilePictureLoading.visibility == View.INVISIBLE) {
                val openGalleryIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(openGalleryIntent, requestProfilePicturesCode)
            }
        }

        iv_profile_background.setSafeOnClickListener {
            //打開相簿
            if (profilePictureLoading.visibility == View.INVISIBLE) {
                val openGalleryIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(openGalleryIntent, requestProfileBackgroundCode)

            }
        }
    }
}