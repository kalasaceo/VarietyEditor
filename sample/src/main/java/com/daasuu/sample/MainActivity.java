package com.daasuu.sample;
import android.content.Intent;
import com.daasuu.camerarecorder.AddWaterMarkOnVideoActivity;
import com.daasuu.camerarecorder.BaseCameraActivity;
import com.daasuu.camerarecorder.CameraHandler;
import com.daasuu.camerarecorder.CameraMainActivity;
import com.daasuu.camerarecorder.PortrateActivity;
import com.daasuu.camerarecorder.RemoveAudioFromVideoActivity;
import com.daasuu.camerarecorder.TrimMainActivity;
import com.daasuu.camerarecorder.TrimmerActivity;
import com.daasuu.camerarecorder.VideoProcessActivity;
import com.daasuu.camerarecorder.VoiceRecorderActivity;
import android.graphics.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 88888;
    private static final int ADD_MAIN_REQUEST_CODE = 4444;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, BaseCameraActivity.class);
        startActivityForResult(intent, ADD_MAIN_REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_MAIN_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data.hasExtra("storyvideopath")) {
                if (data.getExtras().getString("storyvideopath") != "none") {
                }
            }
        }
    }
}