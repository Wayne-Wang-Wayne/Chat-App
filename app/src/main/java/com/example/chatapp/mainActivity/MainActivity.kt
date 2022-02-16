package com.example.chatapp.mainActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.UserHandle
import com.example.chatapp.R
import com.example.chatapp.model.User
import com.example.chatapp.recyclerviewAdapter.UserListAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var userList: ArrayList<User>
    private lateinit var userAdapter: UserListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        userList = ArrayList()
        userAdapter = UserListAdapter(this, userList)

    }
}