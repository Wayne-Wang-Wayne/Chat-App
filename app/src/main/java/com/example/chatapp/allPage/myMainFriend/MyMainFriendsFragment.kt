package com.example.chatapp.allPage.myMainFriend

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.R
import com.example.chatapp.model.User
import com.example.chatapp.recyclerviewAdapter.UserListAdapter
import com.example.chatapp.util.FirebaseUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_my_main_friends.*

class MyMainFriendsFragment : Fragment() {


    private lateinit var userList: ArrayList<User>
    private lateinit var userAdapter: UserListAdapter
    private lateinit var mContext:Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_main_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setView()

    }

    private fun setView(){
        userList = ArrayList()
        userAdapter = UserListAdapter(mContext, userList)

        userRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userAdapter
        }

        FirebaseUtil.listenToRTDBForUser("user", object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                userList.clear()
                for (postSnapShot in snapshot.children) {
                    val currentUser = postSnapShot.getValue(User::class.java)
                    if (FirebaseUtil.mFirebaseAuthInstance.currentUser?.uid != currentUser?.userUID) {
                        userList.add(currentUser!!)
                    }
                }
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }



}