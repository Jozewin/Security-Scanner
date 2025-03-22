// src/main/kotlin/com/zeroday/security_scanner/MobSFApiService.kt
package com.zeroday.security_scanner

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface MobSFApiService {
    @Multipart
    @POST("api/v1/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Header("Authorization") apiKey: String
    ): Response<UploadResponse>

    @FormUrlEncoded
    @POST("api/v1/scan")
    suspend fun scanFile(
        @Field("scan_type") scanType: String,
        @Field("file_name") fileName: String,
        @Field("hash") hash: String,
        @Header("Authorization") apiKey: String
    ): Response<ScanResponse>

    @FormUrlEncoded
    @POST("api/v1/report_json")
    suspend fun getReportJson(
        @Field("hash") hash: String,
        @Field("re_scan") reScan: Int = 0,
        @Header("Authorization") apiKey: String
    ): Response<Map<String, Any>>
}

// Models

data class UploadResponse(
    val hash: String,
    val scan_type: String,
    val file_name: String
)

data class ScanResponse(
    val hash: String,
    val status: String,
    val scan_type: String? = null
)
