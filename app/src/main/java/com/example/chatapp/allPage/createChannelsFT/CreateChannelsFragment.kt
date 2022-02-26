package com.example.chatapp.allPage.createChannelsFT

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chatapp.R
import com.example.chatapp.customStuff.SafeClickListener.Companion.setSafeOnClickListener
import com.example.chatapp.util.FirebaseUtil.Companion.createChannel
import kotlinx.android.synthetic.main.fragment_create_channels.*


class CreateChannelsFragment : Fragment() {

    private lateinit var mContext: Context


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_channels, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setView()
    }

    companion object {
        fun newInstance() = CreateChannelsFragment()
    }

    private fun setView() {
        btn_create_channel.setSafeOnClickListener {
            val channelUid = etChatUid.text.toString()
            val channelName = etChatName.text.toString()
            val isPublic = cbIsPublic.isSelected
            createChannel(mContext, channelUid, channelName, isPublic)
        }
    }
}