package com.daasuu.camerarecorder
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.jaiselrahman.filepicker.model.MediaFile
import com.simform.videooperations.*
import com.simform.videooperations.Common.getFileFromAssets
import com.timqi.sectorprogressview.SectorProgressView
import kotlinx.android.synthetic.main.activity_add_text_on_video.*
import java.io.File
import java.util.concurrent.CompletableFuture.runAsync

class AddTextOnVideoActivity : BaseActivity(R.layout.activity_add_text_on_video, R.string.add_text_on_video) {
    private var isInputVideoSelected = false
    private var VideoOutputPath="none"
    private var p_comp: TextView? = null
    private var spv: SectorProgressView? = null
    private var thistime="1";
    override fun initialization() {
        btnVideoPath.setOnClickListener(this)
        btnAdd.setOnClickListener(this)
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        spv = findViewById<View>(R.id.spv) as SectorProgressView
        p_comp = findViewById<View>(R.id.progress_comp) as TextView
        val intent: Intent = intent
        var thisdata = intent.getStringExtra("LocalVideoPath")
        thistime = intent.getStringExtra("LocalVideoTime")
        if (thisdata != null) {
            isInputVideoSelected = true
            tvInputPathVideo.text = thisdata
            var xx=tvInputPathVideo.text.toString()
            findViewById<View>(R.id.btnVideoPath).visibility = View.GONE
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
                Common.selectFile(this, maxSelection = 1, isImageSelection = false, isAudioSelection = false)
            }
            R.id.btnAdd -> {
                when {
                    !isInputVideoSelected -> {
                        Toast.makeText(this, getString(R.string.input_video_validate_message), Toast.LENGTH_SHORT).show()
                    }
                    TextUtils.isEmpty(edtText.text.toString()) -> {
                        Toast.makeText(this, getString(R.string.please_add_text_validation), Toast.LENGTH_SHORT).show()
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
                        addTextProcess()
                    }
                }
            }
        }
    }
    private fun addTextProcess() {
        val outputPath = Common.getFilePath(this, Common.VIDEO)
        val xPos = width?.let {
            (edtXPos.text.toString().toFloat().times(it)).div(100)
        }
        val yPos = height?.let {
            (edtYPos.text.toString().toFloat().times(it)).div(100)
        }
       val fontPath = getFileFromAssets(this, "Palanquin-Medium.ttf").absolutePath

        val query = ffmpegQueryExtension.addTextOnVideo(tvInputPathVideo.text.toString(), edtText.text.toString(), xPos, yPos, fontPath = fontPath, isTextBackgroundDisplay = true, fontSize = 79, fontcolor = "black", output = outputPath)
        CallBackOfQuery().callQuery(this, query, object : FFmpegCallBack {
            override fun process(logMessage: LogMessage) {
            }
            override fun statisticsProcess(statistics: Statistics) {
                super.statisticsProcess(statistics)
                var comp=(statistics.time.toFloat()/1000)/(thistime.toInt()/1000)
                spv?.setPercent(comp*100)
                p_comp?.text=(((statistics.time.toFloat()/1000)/(thistime.toFloat()/1000))*100).toInt().toString()+"%"
            }
            override fun success() {
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
            btnAdd.isEnabled = true
            mProgressView.visibility = View.GONE
        }
    }

    private fun processStart() {
        btnVideoPath.isEnabled = false
        btnAdd.isEnabled = false
        mProgressView.visibility = View.VISIBLE
    }


    override fun finish() {
        val data = Intent()
        data.putExtra("returnKey1", VideoOutputPath.toString())
        setResult(RESULT_OK, data)
        super.finish()
    }
    @SuppressLint("NewApi")
    override fun selectedFiles(mediaFiles: List<MediaFile>?, requestCode: Int) {
        when (requestCode) {
            Common.VIDEO_FILE_REQUEST_CODE -> {
                if (mediaFiles != null && mediaFiles.isNotEmpty()) {
                    tvInputPathVideo.text = mediaFiles[0].path
                    var xx=tvInputPathVideo.text.toString()
                    Toast.makeText(this, "new passedarg$xx", Toast.LENGTH_SHORT).show()
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
        }
    }
}