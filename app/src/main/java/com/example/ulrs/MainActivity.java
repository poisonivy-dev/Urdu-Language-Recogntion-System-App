package com.example.ulrs;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.google.gson.Gson;

import java.io.File;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {
    Button button_capture;
    TextView textView2;
//  Image Cropper Activity Launcher
    ActivityResultLauncher<CropImageContractOptions> mGetContent;
    private static final int REQUEST_CAMERA_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Objects.requireNonNull(getSupportActionBar()).hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //fetching ids from view
        textView2 = findViewById(R.id.textView2);
        button_capture = findViewById(R.id.button_capture);

        //ask for camera permissions
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.CAMERA
            }, REQUEST_CAMERA_CODE);
        }

        //Launch the Selection panel and Edit-image View
        button_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = null;
                CropImageContractOptions options = getCropOptions(uri);
                mGetContent.launch(options);
            }
        });

        // Fetch the image file and send it to the API
        mGetContent = registerForActivityResult(new CropImageContract(), new ActivityResultCallback<CropImageView.CropResult>() {
            @Override
            public void onActivityResult(CropImageView.CropResult result) {
                if (result.isSuccessful()) {
                    //Create File
                    final File f = new File(Objects.requireNonNull(result.getUriFilePath(getApplicationContext(), true)));

                    // Prepare Data to send to API using Retrofit2
                    RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), f);
                    MultipartBody.Part parts = MultipartBody.Part.createFormData("file", f.getName(), requestBody);
                    RequestBody output_text = RequestBody.create(MediaType.parse("multipart/form-data"),
                            "this is a new image");

                    //Making Retrofit instance
                    Retrofit retrofit = NetworkClient.getRetrofit();
                    UploadApis uploadApis = retrofit.create(UploadApis.class);

                    //Send request to the API
                    Call<ApiResponse> call = uploadApis.uploadImage(parts, output_text);
                    // Set up progress before call
                    final ProgressDialog progressDialog;
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("Extracting Text ...");
                    progressDialog.setTitle("Please Wait");
                    progressDialog.setIndeterminate(false);
                    progressDialog.setCancelable(false);
                    // show it
                    progressDialog.show();
                    call.enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            String jsonRes;
                            if(response.body() == null){
                                jsonRes = "\"  \"";
                            }
                            else{
                                jsonRes = new Gson().toJson(response.body().getOutputText());
                            }
                            //:TODO intent creation and forwarding text
                            Intent send = new Intent(getApplicationContext(), OutputActivity.class);
                            send.putExtra("output_text",jsonRes);
                            startActivity(send);
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            Log.d("REST22", t.toString());
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // an error occurred
                    Log.e("cropImageErr", "onActivityResult...Error CropImage: " + result.getError());
                }
            }
        });
    }

    //Options to make available on Image Cropper View
    public CropImageContractOptions getCropOptions (Uri imageUri){
        return new CropImageContractOptions(imageUri, new CropImageOptions())
                .setActivityTitle("Edit Image")
                .setGuidelines(CropImageView.Guidelines.ON)
                .setBorderCornerColor(R.color.black)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setActivityMenuIconColor(ContextCompat.getColor(getApplicationContext(), R.color.white))
                .setAllowRotation(true)
                .setAutoZoomEnabled(false)
                .setAllowFlipping(true)
                .setOutputCompressFormat(Bitmap.CompressFormat.PNG)
                .setAllowCounterRotation(true)
                .setCropMenuCropButtonTitle("Continue");
    }
}