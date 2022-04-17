package com.example.chatapp.allPage.chatActivity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.chatapp.R
import com.example.chatapp.allPage.MediaActivity.MyMediaActivity
import com.example.chatapp.allPage.myInfoActivity.MyInfoActivity.Companion.BackgroundPicture
import com.example.chatapp.allPage.myInfoActivity.MyInfoActivity.Companion.MainPicture
import com.example.chatapp.allPage.splash.SplashActivity.Companion.allUserBackgroundUrl
import com.example.chatapp.allPage.splash.SplashActivity.Companion.allUserProfileUrl
import com.example.chatapp.customStuff.SafeClickListener.Companion.setSafeOnClickListener
import com.example.chatapp.model.ChannelMessage
import com.example.chatapp.recyclerviewAdapter.ChatRecyclerviewAdapter
import com.example.chatapp.util.IntentUtil
import com.example.chatapp.util.SmallUtil
import kotlinx.android.synthetic.main.layout_profile_dialog.*


class ProfileDialogFragment : DialogFragment() {

    lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window
            ?.attributes?.windowAnimations = R.style.DialogAnimation
    }

    companion object {
        fun getInstance(userUid: String, userName: String): ProfileDialogFragment {
            val profileDialogFragment = ProfileDialogFragment()
            val args = Bundle()
            args.putString("userUid", userUid)
            args.putString("userName", userName)
            profileDialogFragment.arguments = args
            return profileDialogFragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_profile_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userUid = arguments?.getString("userUid")
        val userName = arguments?.getString("userName")
        if (allUserProfileUrl[userUid] != null) {
            SmallUtil.glideProfileUtil(
                context = mContext,
                width = 600,
                type = MainPicture,
                uri = allUserProfileUrl[userUid]!!,
                imageView = iv_profileDialogMainPicture
            )
            iv_profileDialogMainPicture.setSafeOnClickListener {
                goToMediaActivity(allUserProfileUrl[userUid].toString())
            }
        }

        if (allUserBackgroundUrl[userUid] != null) {
            SmallUtil.glideProfileUtil(
                context = mContext,
                type = BackgroundPicture,
                uri = allUserBackgroundUrl[userUid]!!,
                imageView = iv_profileDialogBackgroundPicture
            )
            iv_profileDialogBackgroundPicture.setSafeOnClickListener {
                goToMediaActivity(allUserBackgroundUrl[userUid].toString())
            }
        }

        tv_userNameInProfileDialog.text = userName

    }

    private fun goToMediaActivity(pictureUrl: String) {
        val bundle = Bundle()
        val urlList = ArrayList<String>()
        urlList.add("image_$pictureUrl")
        bundle.putInt(ChatRecyclerviewAdapter.OPEN_MEDIA_INDEX_KEY, 0)
        bundle.putStringArrayList(
            ChatRecyclerviewAdapter.OPEN_MEDIA_LIST_KEY,
            urlList
        )
        IntentUtil.intentToAnyClass(mContext, bundle, MyMediaActivity::class.java)
    }


}