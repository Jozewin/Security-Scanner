package com.zeroday.security_scanner

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Controller
class ScanController(private val mobSfService: MobSfService) {
    
    @GetMapping("/")
    fun homePage(): String {
        return "index"
    }
    
    @PostMapping("/scan")
    @ResponseBody
    fun scanApp(@RequestParam("file") file: MultipartFile): ResponseEntity<Map<String, Any>> {
        val result = mobSfService.scanApp(file)
        return ResponseEntity.ok(result)
    }
    
    @GetMapping("/report")
    @ResponseBody
    fun getReport(
        @RequestParam("hash") hash: String,
        @RequestParam("type") reportType: String
    ): ResponseEntity<Map<String, Any>> {
        val report = mobSfService.getReport(hash, reportType)
        return ResponseEntity.ok(report)
    }
    
    @GetMapping("/results")
    fun resultsPage(
        @RequestParam("hash") hash: String,
        @RequestParam("type") reportType: String,
        model: Model
    ): String {
        model.addAttribute("hash", hash)
        model.addAttribute("type", reportType)
        return "results"
    }
}