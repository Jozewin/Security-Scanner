// src/main/kotlin/com/zeroday/security_scanner/controller/ScanController.kt
package com.zeroday.security_scanner

import kotlinx.coroutines.runBlocking
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile


@RestController
@RequestMapping("/api/scan")
class ScanController(private val mobSFService: MobSFService) {

    @PostMapping(
        value = ["/upload"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun uploadFile(@RequestPart("file") file: MultipartFile): UploadResponse = runBlocking {
        mobSFService.uploadFile(file)
    }


    @PostMapping("/start")
    fun startScan(@RequestBody uploadResponse: UploadResponse): ScanResponse = runBlocking {
        mobSFService.scanFile(uploadResponse)
    }

    @PostMapping("/start-by-hash")
    fun startScanByHash(
        @RequestParam hash: String,
        @RequestParam scanType: String,
        @RequestParam fileName: String
    ): ScanResponse = runBlocking {
        mobSFService.scanFileByHash(hash, scanType, fileName)
    }

    @PostMapping("/report-json")
    fun getReportJson(
        @RequestParam hash: String,
        @RequestParam(required = false, defaultValue = "0") reScan: Int
    ): Map<String, Any> = runBlocking {
        mobSFService.getReportJson(hash, reScan)
    }
}