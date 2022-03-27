package com.example.chatapp.allPage.MediaActivity

import android.content.Context
import android.content.res.Configuration
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.chatapp.R
import com.example.chatapp.util.SmallUtil
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import kotlinx.android.synthetic.main.fragment_video_player.*


class VideoPlayerFragment : Fragment() {

    private lateinit var mContext: Context
    private var width: Float? = 1F
    private var height: Float? = 1F
    private var stopPosition: Int = 0
    lateinit var player: ExoPlayer

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video_player, container, false)
    }

    companion object {
        private const val videoFragmentKey = "dfnsjkdfnsd2"
        fun newInstance(url: String): VideoPlayerFragment {
            val videoPlayerFragment = VideoPlayerFragment()
            val bundle = Bundle()
            bundle.putString(videoFragmentKey, url)
            videoPlayerFragment.arguments = bundle
            return videoPlayerFragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            val url = arguments?.getString(videoFragmentKey)
            setPlayerAndController(url!!)
//            getVideoWidthHeight(url)
//            setDimension()
        } catch (e: Exception) {
            SmallUtil.simpleDialogUtilWithY(mContext, "影片載入錯誤！", "")
            videoProgressBar.visibility = View.GONE
        }

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
//        setDimension()
    }


    private fun setDimension() {
        // Adjust the size of the video
        // so it fits on the screen
        //自適應影片高度
        val videoProportion = height!! / width!!
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        val screenProportion = screenHeight.toFloat() / screenWidth.toFloat()
        val lp: ViewGroup.LayoutParams = myExoplayer.layoutParams
        if (videoProportion < screenProportion) {
            if ((screenHeight.toFloat() / videoProportion).toInt() > screenWidth) {
                lp.width = screenWidth
                lp.height = (screenWidth.toFloat() * videoProportion).toInt()
            } else {
                lp.height = screenHeight
                lp.width = (screenHeight.toFloat() / videoProportion).toInt()
            }

        } else {
            if ((screenWidth.toFloat() * videoProportion).toInt() > screenHeight) {
                lp.height = screenHeight
                lp.width = (screenHeight.toFloat() / videoProportion).toInt()
            } else {
                lp.width = screenWidth
                lp.height = (screenWidth.toFloat() * videoProportion).toInt()
            }
        }
        myExoplayer.layoutParams = lp
    }

    private fun setPlayerAndController(url: String) {
        try {
            player = ExoPlayer.Builder(mContext).build()
            myExoplayer.player = player
            val mediaItem: MediaItem = MediaItem.fromUri(url)
            player.setMediaItem(mediaItem)
            player.prepare()

        } catch (e: Exception) {
            SmallUtil.quickToast(mContext, "影片載入錯誤")
        }
    }

    private fun getVideoWidthHeight(url: String) {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(url)
        width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            ?.toFloat()
        height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            ?.toFloat()
        retriever.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }


}