package com.example.chatapp.allPage.myChannelsFT

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.R
import com.example.chatapp.allPublicChannelsFT.PublicChannelViewModel
import com.example.chatapp.model.UserChannels
import com.example.chatapp.recyclerviewAdapter.MyChannelAdapter
import com.example.chatapp.recyclerviewAdapter.PublicChannelsAdapter
import com.example.chatapp.util.FirebaseUtil.Companion.listenToRTDBForUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_my_channels.*

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

        setView()
    }

    private fun setView(){
        myChannelViewModel = ViewModelProvider(this).get(MyChannelViewModel::class.java)
        myChannelAdapter = MyChannelAdapter(mContext, ArrayList())
        myChannelsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = myChannelAdapter
        }

        listenToRTDBForUser( object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var myChannelList = ArrayList<UserChannels>()
                for (postSnapShot in snapshot.children){
                    val myChannel = postSnapShot.getValue(UserChannels::class.java)
                    myChannelList.add(myChannel!!)
                }
                val sortedList = ArrayList<UserChannels>(myChannelList.sortedWith(compareByDescending { it.timeStamp }))
                myChannelAdapter.setRecyclerviewValue(sortedList)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

}