package com.example.chatapp.allPublicChannelsFT

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.R
import com.example.chatapp.customStuff.SafeClickListener.Companion.setSafeOnClickListener
import com.example.chatapp.model.PublicChannels
import com.example.chatapp.recyclerviewAdapter.PublicChannelsAdapter
import kotlinx.android.synthetic.main.fragment_all_public_channels.*


class AllPublicChannelsFragment : Fragment() {

    private lateinit var mContext: Context
    private lateinit var publicChannelViewModel: PublicChannelViewModel
    private lateinit var publicChannelsAdapter: PublicChannelsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all_public_channels, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    companion object {
        fun newInstance() = AllPublicChannelsFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        publicChannelViewModel = ViewModelProvider(this).get(PublicChannelViewModel::class.java)
        publicChannelsAdapter =
            PublicChannelsAdapter(mContext, ArrayList<PublicChannels>())
        publicChannelsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = publicChannelsAdapter
        }
        retry_layout.setSafeOnClickListener {
            publicChannelViewModel.getPublicChannelsInfo()
        }
    }

    override fun onResume() {
        super.onResume()

        publicChannelViewModel.getPublicChannelsInfo()
        observeViewModel()
    }

    private fun observeViewModel() {

        publicChannelViewModel.finalPublicChanelLiveData.observe(viewLifecycleOwner, Observer {
            if(it.size==0){
                tvNoPublicChannelToJoin.visibility = View.VISIBLE
            }else{
                publicChannelsAdapter.setRecyclerviewValue(it)
                tvNoPublicChannelToJoin.visibility = View.INVISIBLE
            }

        })

        publicChannelViewModel.isLoading.observe(viewLifecycleOwner, Observer {
            if (it) {
                public_channel_progressbar.visibility = View.VISIBLE
            } else {
                public_channel_progressbar.visibility = View.INVISIBLE
            }
        })

        publicChannelViewModel.isError.observe(viewLifecycleOwner, Observer {
            if (it) {
                retry_layout.visibility = View.VISIBLE
            } else {
                retry_layout.visibility = View.GONE
            }
        })
    }
}