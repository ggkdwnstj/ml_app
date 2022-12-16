package com.coconut.ml_app;

import static android.os.ext.SdkExtensions.getExtensionVersion;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import org.tensorflow.lite.Interpreter; //importing tensorflow module (animal classification)

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.io.FileInputStream;

public class MainActivity extends AppCompatActivity {
//    Button bnt1 = new Button();
//    Interpreter tflite =
private final String TAG = this.getClass().getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView text1 = findViewById(R.id.text1);
        Button btn1 = findViewById(R.id.btn_sel);
        Button btn2 = findViewById(R.id.btn_start);
        ImageView img1 = findViewById(R.id.img1);

        //이미지를 고르는 버튼 'btn1'을 눌렀을 경우 사진을 고르는 event listener 구현
        btn1.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            launcher.launch(intent);
        });
    }

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {

                ImageView img1 = findViewById(R.id.img1);
                //imageView의 id를 검색할 수 있도록 findViewById로 선언

                if (result.getResultCode() == RESULT_OK) {
                    Log.e(TAG,"result : " + result);
                    Intent intent = result.getData();
                    Log.e(TAG, "intent : " + intent);
                    Uri uri = intent.getData();
                    Log.e(TAG, "uri : " + uri);
//                        imageview.setImageURI(uri);
                    Glide.with(MainActivity.this)
                            .load(uri)
                            .into(img1);
                }
            });

    //  Interpreter tflite = getTflite("model.tflite");
}

