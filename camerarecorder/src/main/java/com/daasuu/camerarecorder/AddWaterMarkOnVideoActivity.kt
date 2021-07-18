package com.daasuu.camerarecorder

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.jaiselrahman.filepicker.model.MediaFile
import com.daasuu.camerarecorder.BaseActivity
import com.daasuu.camerarecorder.R
import com.simform.videooperations.CallBackOfQuery
import com.simform.videooperations.Common
import com.simform.videooperations.Common.VIDEO
import com.simform.videooperations.Common.getFilePath
import com.simform.videooperations.Common.selectFile
import com.simform.videooperations.FFmpegCallBack
import com.simform.videooperations.LogMessage
import kotlinx.android.synthetic.main.activity_add_text_on_video.*
import java.util.concurrent.CompletableFuture.runAsync
import kotlinx.android.synthetic.main.activity_add_water_mark_on_video.btnAdd
import kotlinx.android.synthetic.main.activity_add_water_mark_on_video.btnImagePath
import kotlinx.android.synthetic.main.activity_add_water_mark_on_video.btnVideoPath
import kotlinx.android.synthetic.main.activity_add_water_mark_on_video.edtXPos
import kotlinx.android.synthetic.main.activity_add_water_mark_on_video.edtYPos
import kotlinx.android.synthetic.main.activity_add_water_mark_on_video.mProgressView
import kotlinx.android.synthetic.main.activity_add_water_mark_on_video.tvInputPathImage
import kotlinx.android.synthetic.main.activity_add_water_mark_on_video.tvInputPathVideo
import kotlinx.android.synthetic.main.activity_add_water_mark_on_video.tvOutputPath

class AddWaterMarkOnVideoActivity : BaseActivity(R.layout.activity_add_water_mark_on_video, R.string.add_water_mark_on_video) {
    private var isInputVideoSelected = false
    private var isWaterMarkImageSelected = false
    private var VideoOutputPath="none"
    override fun initialization() {
        btnVideoPath.setOnClickListener(this)
        btnImagePath.setOnClickListener(this)
        btnAdd.setOnClickListener(this)
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        val intent: Intent = intent
        var thisdata = intent.getStringExtra("LocalVideoPath")
        var stickerthisdata = intent.getStringExtra("StickerPath")
        if (thisdata != null) {
            isInputVideoSelected = true
            isWaterMarkImageSelected =true
            tvInputPathVideo.text = thisdata
            tvInputPathImage.text = stickerthisdata
            var xx=tvInputPathVideo.text.toString()
            //findViewById<View>(R.id.btnVideoPath).visibility = View.GONE
            Toast.makeText(this, "xx passedarg$xx", Toast.LENGTH_SHORT).show()
            if (mediaFiles != null) {
                runAsync {
                    retriever = MediaMetadataRetriever()
                    retriever?.setDataSource(tvInputPathVideo.text.toString())
                    val bit = retriever?.frameAtTime
                    width = bit?.width
                    height = bit?.height
                }
            }
        }
    }
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnVideoPath -> {
                selectFile(this, maxSelection = 1, isImageSelection = false, isAudioSelection = false)
            }
            R.id.btnImagePath -> {
                selectFile(this, maxSelection = 1, isImageSelection = true, isAudioSelection = false)
            }
            R.id.btnAdd -> {

                when {
                    !isInputVideoSelected -> {
                        Toast.makeText(this, getString(R.string.input_video_validate_message), Toast.LENGTH_SHORT).show()
                    }
                    !isWaterMarkImageSelected -> {
                        Toast.makeText(this, getString(R.string.input_image_validate_message), Toast.LENGTH_SHORT).show()
                    }
                    TextUtils.isEmpty(edtXPos.text.toString()) -> {
                        Toast.makeText(this, getString(R.string.x_position_validation), Toast.LENGTH_SHORT).show()
                    }
                    edtXPos.text.toString().toFloat() > 100 || edtXPos.text.toString().toFloat() <= 0 -> {
                        Toast.makeText(this, getString(R.string.x_validation_invalid), Toast.LENGTH_SHORT).show()
                    }
                    TextUtils.isEmpty(edtYPos.text.toString()) -> {
                        Toast.makeText(this, getString(R.string.y_position_validation), Toast.LENGTH_SHORT).show()
                    }
                    edtYPos.text.toString().toFloat() > 100 || edtYPos.text.toString().toFloat() <= 0 -> {
                        Toast.makeText(this, getString(R.string.y_validation_invalid), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        processStart()
                        addWaterMarkProcess()
                    }
                }
            }
        }
    }

    @SuppressLint("NewApi")
    override fun selectedFiles(mediaFiles: List<MediaFile>?, fileRequestCode: Int) {
        when (fileRequestCode) {
            Common.VIDEO_FILE_REQUEST_CODE -> {
                if (mediaFiles != null && mediaFiles.isNotEmpty()) {
                    tvInputPathVideo.text = mediaFiles[0].path
                    isInputVideoSelected = true
                    runAsync {
                        retriever = MediaMetadataRetriever()
                        retriever?.setDataSource(tvInputPathVideo.text.toString())
                        val bit = retriever?.frameAtTime
                        width = bit?.width
                        height = bit?.height
                    }
                } else {
                    Toast.makeText(this, getString(R.string.video_not_selected_toast_message), Toast.LENGTH_SHORT).show()
                }
            }
            Common.IMAGE_FILE_REQUEST_CODE -> {
                if (mediaFiles != null && mediaFiles.isNotEmpty()) {
                    tvInputPathImage.text = mediaFiles[0].path
                    isWaterMarkImageSelected = true
                } else {
                    Toast.makeText(this, getString(R.string.image_not_selected_toast_message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addWaterMarkProcess() {
        val outputPath = getFilePath(this, VIDEO)
        val xPos = width?.let {
            (edtXPos.text.toString().toFloat().times(it)).div(100)
        }
        val yPos = height?.let {
            (edtYPos.text.toString().toFloat().times(it)).div(100)
        }
        val query = ffmpegQueryExtension.addVideoWaterMark(tvInputPathVideo.text.toString(), tvInputPathImage.text.toString(), xPos, yPos, outputPath)
        CallBackOfQuery().callQuery(this, query, object : FFmpegCallBack {
            override fun process(logMessage: LogMessage) {
                tvOutputPath.text = logMessage.text
            }

            override fun success() {
                tvOutputPath.text = String.format(getString(R.string.output_path), outputPath)
                processStop()
                VideoOutputPath=outputPath;
                finish()
            }

            override fun cancel() {
                processStop()
            }

            override fun failed() {
                processStop()
            }
        })
    }

    private fun processStop() {
        runOnUiThread {
            btnVideoPath.isEnabled = true
            btnImagePath.isEnabled = true
            btnAdd.isEnabled = true
            mProgressView.visibility = View.GONE
        }
    }
    override fun finish() {
        val data = Intent()
        data.putExtra("returnKey2", VideoOutputPath.toString())
        setResult(RESULT_OK, data)
        super.finish()
    }
    private fun processStart() {
        btnVideoPath.isEnabled = false
        btnImagePath.isEnabled = false
        btnAdd.isEnabled = false
        mProgressView.visibility = View.VISIBLE
    }
}