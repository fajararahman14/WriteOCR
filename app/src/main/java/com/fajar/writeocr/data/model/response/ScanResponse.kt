package com.fajar.writeocr.data.model.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ScanResponse(

    @SerializedName("text")
    @Expose
    var text: String
)