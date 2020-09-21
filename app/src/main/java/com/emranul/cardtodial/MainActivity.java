package com.emranul.cardtodial;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class MainActivity extends AppCompatActivity {

    private static final String INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-8851464520979715/6567372269";

    private ImageButton cameraBtn;
    private Button dialBtn, deleteBtn;
    private EditText showText;
    private ImageView showImage;
    private RadioGroup radioGroup;
    private RadioButton radioButton;

    private InterstitialAd mInterstitialAd;

    private String value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraBtn = findViewById(R.id.captureBtn);
        dialBtn = findViewById(R.id.dial_btn);
        deleteBtn = findViewById(R.id.delete_btn);
        showText = findViewById(R.id.edit_text);
        showImage = findViewById(R.id.image_view);
        radioGroup = findViewById(R.id.radio_group);


        //Start doing ads work :
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(INTERSTITIAL_AD_UNIT_ID);
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        //End doing ads work;

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        dialBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                    if (!showText.getText().toString().isEmpty()) {
                        btnClicked();
                    } else {
                        Toast.makeText(MainActivity.this, "Please Pic The Card Photo First", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    if (!showText.getText().toString().isEmpty()) {
                        btnClicked();
                    } else {
                        Toast.makeText(MainActivity.this, "Please Pic The Card Photo First", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                value = "";
                showText.setVisibility(View.GONE);
                showImage.setVisibility(View.GONE);
                deleteBtn.setVisibility(View.GONE);
            }
        });


    }

    private void btnClicked() {
        int id = radioGroup.getCheckedRadioButtonId();
        radioButton = findViewById(id);
        String code = (String) radioButton.getText();
        Log.d("TAG", "btnClicked: \n :" + code);
    }

    //Upload Profile Image
    private void selectImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                cropImage();

            } else {
                cropImage();
            }

        } else {
            cropImage();
        }
    }

    //Crop Image Method for selectImage:
    private void cropImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CropImage.ActivityResult result = CropImage.getActivityResult(data);
        if (resultCode == RESULT_OK) {
            Uri resultUri = result.getUri();
            showImage.setImageURI(resultUri);
            showImage.setVisibility(View.VISIBLE);
            BitmapDrawable bitmapDrawable = (BitmapDrawable) showImage.getDrawable();
            Bitmap bitmap = bitmapDrawable.getBitmap();
            imageToText(bitmap);
        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            Exception error = result.getError();
            Log.d("TAG", "onActivityErrorResult: " + error);
        }
    }

    private void imageToText(Bitmap bitmap) {

        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (textRecognizer.isOperational()) {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> item = textRecognizer.detect(frame);
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < item.size(); i++) {
                TextBlock textBlock = item.valueAt(i);
                sb.append(textBlock.getValue());
            }
            value = sb.toString();
            //remove all space and line break form string:
            value = value.replaceAll("\\s+", "");
            Log.d("TAG", "imageToText: \n :" + value);
            showText.setText(value);
            showText.setVisibility(View.VISIBLE);
            deleteBtn.setVisibility(View.VISIBLE);

        } else {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }

    }
}