package com.example.chatapp.allPage.joinChannelsFT

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.example.chatapp.R
import com.example.chatapp.allPage.createChannelsFT.CreateChannelFTViewModel
import com.example.chatapp.customStuff.SafeClickListener.Companion.setSafeOnClickListener
import kotlinx.android.synthetic.main.fragment_join_channels.*

class JoinChannelsFragment : Fragment() {

    private lateinit var mContext: Context
    private lateinit var mJoinChannelFTViewModel: JoinChannelViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_join_channels, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    companion object {
        fun newInstance() = JoinChannelsFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mJoinChannelFTViewModel =
            ViewModelProvider(this).get(JoinChannelViewModel::class.java)
        setView()
    }

    private fun setView() {
        etJoinUid.addTextChangedListener { mJoinChannelFTViewModel.uidEditTextChanged(etJoinUid.text.toString()) }
        btn_join_channel.setSafeOnClickListener {
            val onJoinSuccess = object : OnJoinSuccess {
                override fun onJoinSuccess() {
                    etJoinUid.setText("")
                }
            }
            mJoinChannelFTViewModel.joinChannel(mContext, etJoinUid.text.toString(), onJoinSuccess)
        }
    }
}

interface OnJoinSuccess {
    fun onJoinSuccess()
}