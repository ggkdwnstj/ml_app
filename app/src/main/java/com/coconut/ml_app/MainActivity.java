package com.coconut.ml_app;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.coconut.ml_app.ml.Model;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    final int imageSize = 224;

    TextView text,text_conf;
    ImageView img1;
    Bitmap bitImg;
    Button btn1, btn2;
    Uri imgUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img1 = findViewById(R.id.img1);
        btn1 = findViewById(R.id.btn_sel);
        text = findViewById(R.id.text1);
        text_conf = findViewById(R.id.text_conf);
        btn2 = findViewById(R.id.btn_test);
       // Interpreter tflite = new Interpreter("model.tflite",);

        //이미지를 고르는 버튼 'btn1'을 눌렀을 경우 사진을 고르는 event listener 구현
        btn1.setOnClickListener(view -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI).
                    setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            launcher.launch(galleryIntent);
        });


        btn2.setOnClickListener(View -> {
            Bitmap bm = null;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    bm =// ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), imgUri));
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(),imgUri)).copy(Bitmap.Config.RGBA_F16, true);
                } else {
                    bm = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
                }
            }catch (IOException e) {
                    e.printStackTrace();
                }
            int dimension = Math.min(bm.getWidth(), bm.getHeight());
            bm = ThumbnailUtils.extractThumbnail(bm, dimension, dimension);
            img1.setImageBitmap(bm);
            bitImg = bm;

            bitImg = Bitmap.createScaledBitmap(bm, imageSize, imageSize, false);

            classifyImage(bitImg);
                });
           }

       /*
          이미지를 고른 상태에서 "추론 시작" 누를시 추론 실행
          추론 결과에 따라 TextView의 내용이 변경
          if문 사용, 사진을 고르지 않고 "추론 시작" 버튼을 클릭했을시 toast 메세지 띄우기
       */


    public ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> { // open the Gallery and pick up a photo.
                    if (result.getResultCode() == RESULT_OK) {
                    Log.e(TAG, "result : " + result); //result값 logcat에 출력
                    Intent intent = result.getData();
                    Log.e(TAG, "intent : " + intent); //intent값 logcat에 출력
                    Uri uri = intent.getData();
                    Log.e(TAG, "uri : " + uri); //uri값 logcat에 출력
                    img1.setImageURI(uri);
                    imgUri = uri;
        //          System.out.println(imgUri); 선택한 이미지의 uri값을 확인하는 디버깅 코드

                }
            });

/*
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {

            Bitmap image = (Bitmap) data.getExtras().get("uri");
            int dimension = Math.min(image.getWidth(), image.getHeight());
            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
            img1.setImageBitmap(image);
            bitImg = image;

            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
            classifyImage(image);

            System.out.println("TEST");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
*/

    public void classifyImage(Bitmap image){
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());
            int [] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            for(int i = 0; i < imageSize; i++){
                for(int j = 0; j < imageSize; j++){
                    int val = intValues[pixel++]; // RGB값을 지정하는 코드
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val >> 0xFF) * (1.f / 255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

//          Log.d("shape", byteBuffer.toString());  byteBuffer 값을 확인하는 디버깅 코드
//          Log.d("shape", inputFeature0.getBuffer().toString()); inputBuffer 값을 확인하는 디버깅 코드

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for(int i = 0; i < confidences.length; i++){
                if(confidences[i] > maxConfidence){
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            String[] classes = {"강아지", "고양이"};
            text.setText(classes[maxPos] + "" + "입니다.");

            String s = "";

                s = String.format("%s: %2.1f%% / %s : %2.1f%%", classes[0], confidences[0] * 100, classes[1], confidences[1] * 100);
                text_conf.setText(s);

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }



    //

    }




