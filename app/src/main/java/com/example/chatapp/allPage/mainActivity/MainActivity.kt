package com.example.chatapp.allPage.mainActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.example.chatapp.R
import com.example.chatapp.allPage.myMainFriend.MyMainFriendsFragment
import com.example.chatapp.util.FirebaseUtil.Companion.logOut
import com.example.chatapp.util.SmallUtil
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var mMenu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initToolBar()
        changePage(BaseViewPagerFragment.newInstance())
        
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

    private fun changePage(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(main_frame_layout.id, fragment)
        supportFragmentManager.executePendingTransactions()
        supportFragmentManager.beginTransaction()
            .replace(main_frame_layout.id, fragment)
            .commitAllowingStateLoss()
    }

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        SmallUtil.quickToast(this, "請再按一次以退出App")
        Handler(Looper.getMainLooper()).postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }
}