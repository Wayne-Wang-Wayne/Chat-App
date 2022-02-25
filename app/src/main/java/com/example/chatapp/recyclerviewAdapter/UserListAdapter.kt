package com.example.chatapp.recyclerviewAdapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.chatActivity.ChatActivity
import com.example.chatapp.customStuff.SafeClickListener.Companion.setSafeOnClickListener
import com.example.chatapp.model.User
import com.example.chatapp.util.IntentUtil.intentToAnyClass

class UserListAdapter(val context: Context, private val userList: ArrayList<User>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.user_layout, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentUser = userList[position]
        (holder as UserViewHolder)
        holder.tvName.text = currentUser.name
        holder.itemView.setSafeOnClickListener {
            val mBundle = Bundle()
            mBundle.apply {
                putString("friendName", currentUser.name)
                putString("friendUid", currentUser.userUID)
            }
            intentToAnyClass(context = context, bundle = mBundle, cls = ChatActivity::class.java)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
    }
}