package com.example.chatapp.mainActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.UserHandle
import android.view.Menu
import android.view.MenuItem
import com.example.chatapp.R
import com.example.chatapp.model.User
import com.example.chatapp.recyclerviewAdapter.UserListAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var mMenu: Menu? = null
    private lateinit var userList: ArrayList<User>
    private lateinit var userAdapter: UserListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        initToolBar()
        userList = ArrayList()
        userAdapter = UserListAdapter(this, userList)


    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        mMenu = menu
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logOut){
          //logic for log out
            return true
        }

        return true
    }

    private fun initToolBar(){
        setSupportActionBar(main_activity_toolbar)
    }
}