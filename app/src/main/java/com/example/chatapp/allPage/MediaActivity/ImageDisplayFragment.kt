package com.example.chatapp.allPage.MediaActivity

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.example.chatapp.R
import com.example.chatapp.allPage.MediaActivity.pagerAdapter.PageFragmentAdapter
import com.example.chatapp.allPage.pictureDetailActivity.PictureDetailActivity
import com.example.chatapp.customStuff.SafeClickListener.Companion.setSafeOnClickListener
import com.example.chatapp.util.SmallUtil
import kotlinx.android.synthetic.main.fragment_image_display.*


class ImageDisplayFragment : Fragment() {

    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_display, container, false)
    }

    companion object {

        private const val imageFragmentKey = "dfnsjkdfnsd2"

        fun newInstance(url: String): ImageDisplayFragment {
            val videoPlayerFragment = ImageDisplayFragment()
            val bundle = Bundle()
            bundle.putString(imageFragmentKey, url)
            videoPlayerFragment.arguments = bundle
            return videoPlayerFragment
        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pictureUri = arguments?.getString(imageFragmentKey)
        SmallUtil.glideNoCutPicture(mContext, pictureUri?.toUri()!!, iv_image_display)
    }
}