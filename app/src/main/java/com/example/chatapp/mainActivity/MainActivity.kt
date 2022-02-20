package com.example.chatapp.mainActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.R
import com.example.chatapp.model.User
import com.example.chatapp.recyclerviewAdapter.UserListAdapter
import com.example.chatapp.util.FirebaseUtil.Companion.listenToRTDBForUser
import com.example.chatapp.util.FirebaseUtil.Companion.logOut
import com.example.chatapp.util.FirebaseUtil.Companion.mFirebaseAuthInstance
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
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

        userRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userAdapter
        }

        listenToRTDBForUser("user", object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                userList.clear()
                for (postSnapShot in snapshot.children) {
                    val currentUser = postSnapShot.getValue(User::class.java)
                    if(mFirebaseAuthInstance.currentUser?.uid != currentUser?.uid){
                        userList.add(currentUser!!)
                    }
                }
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        mMenu = menu
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logOut) {
            //logic for log out
            logOut(this)
            return true
        }

        return true
    }

    private fun initToolBar() {
        setSupportActionBar(main_activity_toolbar)
    }
}