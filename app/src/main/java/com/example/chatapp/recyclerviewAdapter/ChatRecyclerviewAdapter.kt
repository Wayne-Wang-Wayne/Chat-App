package com.example.chatapp.recyclerviewAdapter


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.chatapp.R
import com.example.chatapp.allPage.MediaActivity.MyMediaActivity
import com.example.chatapp.allPage.splash.SplashActivity.Companion.allUserProfileUrl
import com.example.chatapp.customStuff.SafeClickListener.Companion.setSafeOnClickListener
import com.example.chatapp.model.ChannelMessage
import com.example.chatapp.util.AudioPlayHelper
import com.example.chatapp.util.AudioProgressInterface
import com.example.chatapp.util.FirebaseUtil.Companion.mFirebaseAuthInstance
import com.example.chatapp.util.IntentUtil.intentToAnyClass
import com.example.chatapp.util.SmallUtil
import com.example.chatapp.util.SmallUtil.glideFormOnlineVideo
import com.example.chatapp.util.SmallUtil.glideNormalUtil

class ChatRecyclerviewAdapter(
    private val mContext: Context,
    private val messageList: ArrayList<ChannelMessage>,
    private val mActivity: Activity
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val colorSettingList = ArrayList<String>()
    val MESSAGE_RECEIVED = 1
    val MESSAGE_SENT = 2

    companion object {
        val OPEN_MEDIA_INDEX_KEY = "987sdmads4"
        val OPEN_MEDIA_LIST_KEY = "2sdo948"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            MESSAGE_SENT -> {
                SentViewHolder(
                    LayoutInflater.from(mContext).inflate(R.layout.message_sent_box, parent, false)
                )
            }
            MESSAGE_RECEIVED -> {
                ReceivedViewHolder(
                    LayoutInflater.from(mContext)
                        .inflate(R.layout.message_received_box, parent, false)
                )
            }
            else -> {
                //will not go into this line
                SentViewHolder(
                    LayoutInflater.from(mContext).inflate(R.layout.message_sent_box, parent, false)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val currentMessage = messageList[position]

        if (holder.javaClass == SentViewHolder::class.java) {

            //do the stuff for sent view holder
            holder as SentViewHolder

            //set send time
            holder.tvSentTime.text =
                "${modifyDate(currentMessage.messageDate!!)} ${modifyTime(currentMessage.messageTime!!)}"

            //set sent message
            if (currentMessage.imageUri != "") {
                //如果是圖片
                holder.ivSentImage.visibility = View.VISIBLE
                holder.tvSentMessage.visibility = View.GONE
                holder.sent_link_preview_recyclerview.visibility = View.GONE
                holder.play_icon.visibility = View.GONE
                holder.sent_play_voice_animation.visibility = View.GONE
                glideNormalUtil(mContext, currentMessage.imageUri?.toUri()!!, holder.ivSentImage)
                holder.ivSentImage.setSafeOnClickListener {
                    goToMediaActivity(currentMessage)
                }
            } else if (currentMessage.message != "" || (currentMessage.message == "" && currentMessage.imageUri == "" && currentMessage.videoUri == "" && currentMessage.voiceUri == "")) {
                //如果是文字訊息
                holder.tvSentMessage.visibility = View.VISIBLE
                holder.ivSentImage.visibility = View.GONE
                holder.tvSentMessage.text = currentMessage.message
                holder.play_icon.visibility = View.GONE
                holder.sent_play_voice_animation.visibility = View.GONE

                //處理連結預覽圖
                val spans = holder.tvSentMessage.urls
                if (spans.isNotEmpty()) {
                    holder.sent_link_preview_recyclerview.visibility = View.VISIBLE
                    val previewLinkList = ArrayList<String>()
                    for (span in spans) {
                        previewLinkList.add(span.url)
                    }
                    val linkPreviewAdapter = LinkPreviewAdapter(mContext, previewLinkList)
                    holder.sent_link_preview_recyclerview.apply {
                        layoutManager = LinearLayoutManager(context)
                        adapter = linkPreviewAdapter
                    }
                } else {
                    holder.sent_link_preview_recyclerview.visibility = View.GONE
                }
            } else if (currentMessage.videoUri != "") {
                //如果是影片
                holder.ivSentImage.visibility = View.VISIBLE
                holder.tvSentMessage.visibility = View.GONE
                holder.sent_link_preview_recyclerview.visibility = View.GONE
                holder.play_icon.visibility = View.VISIBLE
                holder.sent_play_voice_animation.visibility = View.GONE
                glideFormOnlineVideo(mContext, currentMessage.videoUri!!, holder.ivSentImage)
                holder.ivSentImage.setSafeOnClickListener {
                    goToMediaActivity(currentMessage)
                }
            } else if (currentMessage.voiceUri != "") {
                //如果是語音訊息
                holder.ivSentImage.visibility = View.GONE
                holder.tvSentMessage.visibility = View.GONE
                holder.sent_link_preview_recyclerview.visibility = View.GONE
                holder.play_icon.visibility = View.GONE
                holder.sent_play_voice_animation.visibility = View.VISIBLE
                holder.sent_play_voice_animation.layoutParams =
                    RelativeLayout.LayoutParams(500, 200)
                holder.sent_play_voice_animation.cancelAnimation()


                val audioProgressInterface = object : AudioProgressInterface {
                    override fun onAudioPlaying(proportion: Float) {
                        holder.sent_audio_progress_view.layoutParams =
                            RelativeLayout.LayoutParams((proportion * 500).toInt(), 200)
                        if (!holder.sent_play_voice_animation.isAnimating) {
                            holder.sent_play_voice_animation.playAnimation()
                        }
                        if (proportion == 1F) {
                            holder.sent_audio_progress_view.layoutParams =
                                RelativeLayout.LayoutParams(0, 200)
                            if (holder.sent_play_voice_animation.isAnimating) {
                                holder.sent_play_voice_animation.cancelAnimation()
                            }
                        }
                    }
                }
                holder.sent_play_voice_animation.setSafeOnClickListener {
                    val audioPlayHelper = AudioPlayHelper(
                        mActivity,
                        mContext,
                        currentMessage.voiceUri!!,
                        audioProgressInterface
                    )
                    audioPlayHelper.startPlaying()
                }

            }
        }
        if (holder.javaClass == ReceivedViewHolder::class.java) {

            //do the stuff for received view holder
            holder as ReceivedViewHolder
            setReceiveBoxColor(currentMessage.senderName!!, holder.tvReceivedMessageParent)

            //set profile picture
            if (allUserProfileUrl[currentMessage.sentUserUID] != null) {
                SmallUtil.glideProfileUtil(
                    mContext,
                    200,
                    allUserProfileUrl[currentMessage.sentUserUID]!!,
                    holder.iv_myProfilePictureInReceiveBox
                )
            } else {
                holder.iv_myProfilePictureInReceiveBox.setImageResource(R.drawable.default_user_image)
            }

            //set receiver name and also hide duplicate name
            holder.tvReceiveName.text = currentMessage.senderName
            if (position > 0 && messageList[position].senderName == messageList[position - 1].senderName) {
                holder.tvReceiveName.visibility = View.GONE
                holder.profilePictureGroupInReceiveBox.visibility = View.GONE
            } else {
                holder.tvReceiveName.visibility = View.VISIBLE
                holder.profilePictureGroupInReceiveBox.visibility = View.VISIBLE
            }

            //set receive time
            holder.tvReceiveTime.text =
                "${modifyDate(currentMessage.messageDate!!)} ${modifyTime(currentMessage.messageTime!!)}"

            //set received message
            if (currentMessage.imageUri != "") {
                //如果是圖片
                holder.ivReceivedImage.visibility = View.VISIBLE
                holder.tvReceivedMessage.visibility = View.GONE
                holder.received_link_preview_recyclerview.visibility = View.GONE
                holder.play_icon.visibility = View.GONE
                holder.received_play_voice_animation.visibility = View.GONE
                glideNormalUtil(
                    mContext,
                    currentMessage.imageUri?.toUri()!!,
                    holder.ivReceivedImage
                )
                holder.ivReceivedImage.setSafeOnClickListener {
                    goToMediaActivity(currentMessage)
                }
            } else if (currentMessage.message != "" || (currentMessage.message == "" && currentMessage.imageUri == "" && currentMessage.videoUri == "" && currentMessage.voiceUri == "")) {
                //如果是文字訊息
                holder.tvReceivedMessage.visibility = View.VISIBLE
                holder.ivReceivedImage.visibility = View.GONE
                holder.tvReceivedMessage.text = currentMessage.message
                holder.play_icon.visibility = View.GONE
                holder.received_play_voice_animation.visibility = View.GONE

                //處理連結預覽圖
                val spans = holder.tvReceivedMessage.urls
                if (spans.isNotEmpty()) {
                    holder.received_link_preview_recyclerview.visibility = View.VISIBLE
                    val previewLinkList = ArrayList<String>()
                    for (span in spans) {
                        previewLinkList.add(span.url)
                    }
                    val linkPreviewAdapter = LinkPreviewAdapter(mContext, previewLinkList)
                    holder.received_link_preview_recyclerview.apply {
                        layoutManager = LinearLayoutManager(context)
                        adapter = linkPreviewAdapter
                    }
                } else {
                    holder.received_link_preview_recyclerview.visibility = View.GONE
                }
            } else if (currentMessage.videoUri != "") {
                //如果是影片
                holder.ivReceivedImage.visibility = View.VISIBLE
                holder.tvReceivedMessage.visibility = View.GONE
                holder.received_link_preview_recyclerview.visibility = View.GONE
                holder.play_icon.visibility = View.VISIBLE
                holder.received_play_voice_animation.visibility = View.GONE
                glideFormOnlineVideo(mContext, currentMessage.videoUri!!, holder.ivReceivedImage)

                holder.ivReceivedImage.setSafeOnClickListener {
                    goToMediaActivity(currentMessage)
                }
            } else if (currentMessage.voiceUri != "") {
                //如果是語音訊息
                holder.ivReceivedImage.visibility = View.GONE
                holder.tvReceivedMessage.visibility = View.GONE
                holder.received_link_preview_recyclerview.visibility = View.GONE
                holder.play_icon.visibility = View.GONE
                holder.received_play_voice_animation.visibility = View.VISIBLE
                holder.received_play_voice_animation.layoutParams =
                    RelativeLayout.LayoutParams(500, 200)
                holder.received_play_voice_animation.cancelAnimation()

                val audioProgressInterface = object : AudioProgressInterface {
                    override fun onAudioPlaying(proportion: Float) {
                        holder.received_audio_progress_view.layoutParams =
                            RelativeLayout.LayoutParams((proportion * 500).toInt(), 200)
                        if (!holder.received_play_voice_animation.isAnimating) {
                            holder.received_play_voice_animation.playAnimation()
                        }
                        if (proportion == 1F) {
                            holder.received_audio_progress_view.layoutParams =
                                RelativeLayout.LayoutParams(0, 200)
                            if (holder.received_play_voice_animation.isAnimating) {
                                holder.received_play_voice_animation.cancelAnimation()
                            }
                        }
                    }
                }
                holder.received_play_voice_animation.setSafeOnClickListener {
                    val audioPlayHelper = AudioPlayHelper(
                        mActivity,
                        mContext,
                        currentMessage.voiceUri!!,
                        audioProgressInterface
                    )
                    audioPlayHelper.startPlaying()
                }

            }
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return if (mFirebaseAuthInstance.currentUser?.uid.equals(currentMessage.sentUserUID)) {
            MESSAGE_SENT
        } else {
            MESSAGE_RECEIVED
        }
    }

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSentMessage: TextView = itemView.findViewById<TextView>(R.id.tvSentMessage)
        val tvSentTime: TextView = itemView.findViewById<TextView>(R.id.tvSentTime)
        val ivSentImage: ImageView = itemView.findViewById(R.id.ivSentImage)
        val sent_link_preview_recyclerview: RecyclerView =
            itemView.findViewById(R.id.sent_link_preview_recyclerview)
        val play_icon: ImageView = itemView.findViewById(R.id.play_icon)
        val sent_play_voice_animation: LottieAnimationView =
            itemView.findViewById(R.id.sent_play_voice_animation)
        val sent_audio_progress_view: View = itemView.findViewById(R.id.sent_audio_progress_view)
    }

    class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvReceiveName: TextView = itemView.findViewById<TextView>(R.id.tvReceiveName)
        val tvReceivedMessage: TextView = itemView.findViewById<TextView>(R.id.tvReceivedMessage)
        val tvReceiveTime: TextView = itemView.findViewById<TextView>(R.id.tvReceiveTime)
        val tvReceivedMessageParent: RelativeLayout =
            itemView.findViewById(R.id.tvReceivedMessageParent)
        val profilePictureGroupInReceiveBox: CardView =
            itemView.findViewById(R.id.profilePictureGroupInReceiveBox)
        val iv_myProfilePictureInReceiveBox: ImageView =
            itemView.findViewById(R.id.iv_myProfilePictureInReceiveBox)
        val ivReceivedImage: ImageView = itemView.findViewById(R.id.ivReceivedImage)
        val received_link_preview_recyclerview: RecyclerView =
            itemView.findViewById(R.id.received_link_preview_recyclerview)
        val play_icon: ImageView = itemView.findViewById(R.id.play_icon)
        val received_play_voice_animation: LottieAnimationView =
            itemView.findViewById(R.id.received_play_voice_animation)
        val received_audio_progress_view: View =
            itemView.findViewById(R.id.received_audio_progress_view)
    }

    private fun modifyDate(messageDate: String): String {
        val lastIndexDate = messageDate.lastIndexOf("-")
        return lastIndexDate.let { messageDate.substring(0, it) }
    }

    private fun modifyTime(messageTime: String): String {
        val lastIndexTime = messageTime.lastIndexOf(":")
        return lastIndexTime.let { messageTime.substring(0, it) }
    }

    private fun setReceiveBoxColor(senderName: String, relativeLayout: RelativeLayout) {
        var isNameAlreadyAdd = false
        var positionOfName = 0
        for (item in colorSettingList) {
            if (item == senderName) {
                isNameAlreadyAdd = true
                break
            }
            positionOfName++
        }
        if (isNameAlreadyAdd) {
            determineBgColor(positionOfName % 6, relativeLayout)
        } else {
            colorSettingList.add(senderName)
            determineBgColor(positionOfName % 6, relativeLayout)
        }
    }

    private fun determineBgColor(index: Int, relativeLayout: RelativeLayout) {
        when (index) {
            0 -> relativeLayout.background =
                ContextCompat.getDrawable(mContext, R.drawable.received_message_background1)
            1 -> relativeLayout.background =
                ContextCompat.getDrawable(mContext, R.drawable.received_message_background2)
            2 -> relativeLayout.background =
                ContextCompat.getDrawable(mContext, R.drawable.received_message_background3)
            3 -> relativeLayout.background =
                ContextCompat.getDrawable(mContext, R.drawable.received_message_background4)
            4 -> relativeLayout.background =
                ContextCompat.getDrawable(mContext, R.drawable.received_message_background5)
            5 -> relativeLayout.background =
                ContextCompat.getDrawable(mContext, R.drawable.received_message_background6)
        }
    }

    private fun extractPositionAndUriList(currentMessage: ChannelMessage): ArrayList<Any> {
        val urlList = ArrayList<String>()
        var i = 0
        var index = 0
        for (message in messageList) {

            if (message.videoUri != "") {
                urlList.add("video_${message.videoUri}")
                if (message.videoUri == currentMessage.videoUri) {
                    index = i
                }
                i++
            }
            if (message.imageUri != "") {
                urlList.add("image_${message.imageUri}")
                if (message.imageUri == currentMessage.imageUri) {
                    index = i
                }
                i++
            }

        }
        val fullList = ArrayList<Any>()
        fullList.add(index)
        fullList.add(urlList)
        return fullList
    }

    private fun goToMediaActivity(currentMessage: ChannelMessage) {
        val fullUriList = extractPositionAndUriList(currentMessage)
        val bundle = Bundle()
        bundle.putInt(OPEN_MEDIA_INDEX_KEY, fullUriList[0] as Int)
        bundle.putStringArrayList(
            OPEN_MEDIA_LIST_KEY,
            fullUriList[1] as ArrayList<String>
        )
        intentToAnyClass(mContext, bundle, MyMediaActivity::class.java)

    }
}