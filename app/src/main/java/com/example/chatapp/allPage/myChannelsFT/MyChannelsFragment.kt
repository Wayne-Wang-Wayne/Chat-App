package com.example.chatapp.allPage.myChannelsFT

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.chatapp.R
import com.example.chatapp.allPublicChannelsFT.PublicChannelViewModel
import com.example.chatapp.recyclerviewAdapter.MyChannelAdapter
import com.example.chatapp.recyclerviewAdapter.PublicChannelsAdapter

class MyChannelsFragment : Fragment() {

    private lateinit var mContext: Context
    private lateinit var myChannelViewModel: MyChannelViewModel
    private lateinit var myChannelAdapter: MyChannelAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_channels, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    companion object {
        fun newInstance() = MyChannelsFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myChannelViewModel = ViewModelProvider(this).get(MyChannelViewModel::class.java)

    }

}