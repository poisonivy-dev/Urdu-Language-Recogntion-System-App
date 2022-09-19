package com.example.ulrs;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkClient {
    private static Retrofit retrofit;
//    private static String BASE_URL = "https://urdu-language-recogniton-system.azurewebsites.net/" ;
    private static String BASE_URL = "https://urdu-language-recognition-sys.herokuapp.com/" +
        "";
    public static  Retrofit getRetrofit(){
        OkHttpClient okHttpClient =new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        if(retrofit == null){
            retrofit = new Retrofit.Builder().baseUrl(BASE_URL).
                    addConverterFactory (GsonConverterFactory.create()).client(okHttpClient).build();

        }
        return retrofit;
    }
}
