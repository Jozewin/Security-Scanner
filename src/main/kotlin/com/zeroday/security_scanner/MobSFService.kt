package com.zeroday.security_scanner

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File

@Service
class MobSFService(private val mobSFApiService: MobSFApiService) {

    @Value("\${mobsf.api-key}")
    private lateinit var apiKey: String

    suspend fun uploadFile(file: MultipartFile): UploadResponse = withContext(Dispatchers.IO) {
        val tempFile = File.createTempFile("upload-", "-${file.originalFilename}")
        try {
            file.transferTo(tempFile)
            val requestFile = tempFile.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.originalFilename, requestFile)
            val response = mobSFApiService.uploadFile(body, apiKey)
            if (!response.isSuccessful) throw RuntimeException("Upload failed: \${response.code()} \${response.message()}")
            response.body() ?: throw RuntimeException("Upload response body is null")
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }

    suspend fun scanFile(uploadResponse: UploadResponse): ScanResponse = withContext(Dispatchers.IO) {
        val response = mobSFApiService.scanFile(uploadResponse.scan_type, uploadResponse.file_name, uploadResponse.hash, apiKey)
        if (!response.isSuccessful) throw RuntimeException("Scan failed: \${response.code()} \${response.message()}")
        response.body() ?: throw RuntimeException("Scan response body is null")
    }

    suspend fun scanFileByHash(hash: String, scanType: String, fileName: String): ScanResponse = withContext(Dispatchers.IO) {
        val response = mobSFApiService.scanFile(scanType, fileName, hash, apiKey)
        if (!response.isSuccessful) throw RuntimeException("Scan failed: \${response.code()} \${response.message()}")
        response.body() ?: throw RuntimeException("Scan response body is null")
    }

    suspend fun getReportJson(hash: String, reScan: Int = 0): Map<String, Any> = withContext(Dispatchers.IO) {
        val response = mobSFApiService.getReportJson(hash, reScan, apiKey)
        if (!response.isSuccessful) throw RuntimeException("Report fetch failed: \${response.code()} \${response.message()}")
        response.body() ?: throw RuntimeException("Report response body is null")
    }
}