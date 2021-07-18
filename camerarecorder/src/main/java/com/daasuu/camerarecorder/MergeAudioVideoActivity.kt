package com.daasuu.camerarecorder

import android.content.Intent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.jaiselrahman.filepicker.model.MediaFile
import com.daasuu.camerarecorder.BaseActivity
import com.daasuu.camerarecorder.R
import com.simform.videooperations.*
import com.timqi.sectorprogressview.SectorProgressView
import kotlinx.android.synthetic.main.activity_add_text_on_video.*
import kotlinx.android.synthetic.main.activity_merge_audio_video.btnMerge
import kotlinx.android.synthetic.main.activity_merge_audio_video.btnMp3Path
import kotlinx.android.synthetic.main.activity_merge_audio_video.btnVideoPath
import kotlinx.android.synthetic.main.activity_merge_audio_video.mProgressView
import kotlinx.android.synthetic.main.activity_merge_audio_video.tvInputPathAudio
import kotlinx.android.synthetic.main.activity_merge_audio_video.tvInputPathVideo
import kotlinx.android.synthetic.main.activity_merge_audio_video.tvOutputPath

class MergeAudioVideoActivity : BaseActivity(R.layout.activity_merge_audio_video, R.string.merge_video_and_audio) {
    private var isInputVideoSelected: Boolean = false
    private var isInputAudioSelected: Boolean = false
    private var spv: SectorProgressView? = null
    private var thistime="1";
    private var p_comp: TextView? = null
    override fun initialization() {
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        val intent: Intent = intent
        var video_path = intent.getStringExtra("PassedVideoPath")
        var music_path = intent.getStringExtra("PassedMusicTime")
        thistime = intent.getStringExtra("PassedVideoTime")
        if (video_path != null) {
            isInputVideoSelected = true
            isInputAudioSelected = true
            tvInputPathVideo.text = video_path
            tvInputPathAudio.text = music_path
            processStart()
            mergeProcess()
        }
        spv = findViewById<View>(R.id.spv) as SectorProgressView
        p_comp = findViewById<View>(R.id.progress_comp) as TextView
        btnVideoPath.setOnClickListener(this)
        btnMp3Path.setOnClickListener(this)
        btnMerge.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnVideoPath -> {
                Common.selectFile(this, maxSelection = 1, isImageSelection = false, isAudioSelection = false)
            }
            R.id.btnMp3Path -> {
                Common.selectFile(this, maxSelection = 1, isImageSelection = false, isAudioSelection = true)
            }
            R.id.btnMerge -> {
                when {
                    !isInputVideoSelected -> {
                        Toast.makeText(this, getString(R.string.input_video_validate_message), Toast.LENGTH_SHORT).show()
                    }
                    !isInputAudioSelected -> {
                        Toast.makeText(this, getString(R.string.please_select_input_audio), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        processStart()
                        mergeProcess()
                    }
                }
            }
        }
    }

    private fun mergeProcess() {
        val outputPath = Common.getFilePath(this, Common.VIDEO)
        val query = ffmpegQueryExtension.mergeAudioVideo(tvInputPathVideo.text.toString(), tvInputPathAudio.text.toString(), outputPath)

        CallBackOfQuery().callQuery(this, query, object : FFmpegCallBack {
            override fun process(logMessage: LogMessage) {
                tvOutputPath.text = logMessage.text
            }
            override fun statisticsProcess(statistics: Statistics) {
                super.statisticsProcess(statistics)
                var comp=(statistics.time.toFloat()/1000)/(thistime.toInt()/1000)
                spv?.setPercent(comp*100)
                p_comp?.text=(((statistics.time.toFloat()/1000)/(thistime.toFloat()/1000))*100).toInt().toString()+"%"
            }
            override fun success() {
                tvOutputPath.text = String.format(getString(R.string.output_path), outputPath)
                processStop()

            }

            override fun cancel() {
                processStop()
            }

            override fun failed() {
                processStop()
            }

        })
    }

    override fun selectedFiles(mediaFiles: List<MediaFile>?, requestCode: Int) {
        when (requestCode) {
            Common.VIDEO_FILE_REQUEST_CODE -> {
                if (mediaFiles != null && mediaFiles.isNotEmpty()) {
                    tvInputPathVideo.text = mediaFiles[0].path
                    isInputVideoSelected = true
                } else {
                    Toast.makeText(this, getString(R.string.video_not_selected_toast_message), Toast.LENGTH_SHORT).show()
                }
            }
            Common.AUDIO_FILE_REQUEST_CODE -> {
                if (mediaFiles != null && mediaFiles.isNotEmpty()) {
                    tvInputPathAudio.text = mediaFiles[0].path
                    isInputAudioSelected = true
                } else {
                    Toast.makeText(this, getString(R.string.video_not_selected_toast_message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun processStop() {
        runOnUiThread {
            btnVideoPath.isEnabled = true
            btnMp3Path.isEnabled = true
            btnMerge.isEnabled = true
            mProgressView.visibility = View.GONE
        }
    }

    private fun processStart() {
        btnVideoPath.isEnabled = false
        btnMp3Path.isEnabled = false
        btnMerge.isEnabled = false
        mProgressView.visibility = View.VISIBLE
    }
}