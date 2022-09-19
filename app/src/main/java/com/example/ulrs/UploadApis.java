package com.example.ulrs;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UploadApis {
    @Multipart
    @POST("predict")
    Call<ApiResponse> uploadImage(@Part MultipartBody.Part part, @Part("output_text") RequestBody requestBody );

}
