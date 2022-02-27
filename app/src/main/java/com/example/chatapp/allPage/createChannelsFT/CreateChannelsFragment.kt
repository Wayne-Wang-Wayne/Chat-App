package com.example.chatapp.allPage.createChannelsFT

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.chatapp.R
import com.example.chatapp.customStuff.SafeClickListener.Companion.setSafeOnClickListener
import com.example.chatapp.util.SmallUtil
import kotlinx.android.synthetic.main.fragment_create_channels.*


class CreateChannelsFragment : Fragment() {

    private lateinit var mContext: Context
    private lateinit var mCreateChannelFTViewModel: CreateChannelFTViewModel


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

        mCreateChannelFTViewModel =
            ViewModelProvider(this).get(CreateChannelFTViewModel::class.java)

        setView()
        observeViewModel()
    }

    companion object {
        fun newInstance() = CreateChannelsFragment()
    }

    private fun setView() {

        etChatUid.addTextChangedListener { mCreateChannelFTViewModel.uidEditTextChanged(etChatUid.text.toString()) }
        etChatName.addTextChangedListener { mCreateChannelFTViewModel.nameEditTextChanged(etChatName.text.toString()) }

        btn_create_channel.setSafeOnClickListener {
            val channelUid = etChatUid.text.toString()
            val channelName = etChatName.text.toString()
            val isPublic = cbIsPublic.isChecked
            mCreateChannelFTViewModel.createChannelProcess(
                mContext,
                channelUid,
                channelName,
                isPublic
            )
        }
    }

    private fun observeViewModel() {
        mCreateChannelFTViewModel.ifCreateSuccessfully.observe(viewLifecycleOwner, Observer {
            if (it) {
                SmallUtil.simpleDialogUtil(mContext,"成功","創建頻道成功，趕快告訴朋友你的頻道ID！\n房間Uid：${etChatUid.text}")
                etChatUid.setText("")
                etChatName.setText("")
            }
        })

        mCreateChannelFTViewModel.ifCreateFail.observe(viewLifecycleOwner, Observer {
            if(it){
                SmallUtil.quickToast(mContext,"創建失敗！請重試或聯繫瑋瑋！")
            }
        })
    }


}

