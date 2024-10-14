package com.fajar.writeocr.data.api

import com.fajar.writeocr.data.model.response.ScanResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface Api {
    @Multipart
    @POST("/predict")
    fun scanImage(
        @Part file: MultipartBody.Part
    ): Call<ScanResponse>
}