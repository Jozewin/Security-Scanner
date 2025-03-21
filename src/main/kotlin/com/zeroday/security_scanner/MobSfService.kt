package com.zeroday.security_scanner

import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import org.springframework.util.MultiValueMap
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.ByteArrayHttpMessageConverter
import org.springframework.http.converter.ResourceHttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter

@Service
class MobSfService(
    private val restTemplate: RestTemplate,
    private val appConfig: AppConfig
) {
    init {
        // Configure RestTemplate with appropriate message converters
        val messageConverters = ArrayList(restTemplate.messageConverters)
        messageConverters.add(ByteArrayHttpMessageConverter())
        messageConverters.add(StringHttpMessageConverter())
        messageConverters.add(ResourceHttpMessageConverter())
        messageConverters.add(FormHttpMessageConverter())
        messageConverters.add(MappingJackson2HttpMessageConverter())
        restTemplate.messageConverters = messageConverters
        
        // Configure timeout
        val requestFactory = SimpleClientHttpRequestFactory()
        requestFactory.setConnectTimeout(30000)
        requestFactory.setReadTimeout(30000)
        restTemplate.requestFactory = requestFactory
    }
    
    fun scanApp(file: MultipartFile): Map<String, Any> {
        // Upload the file
        val uploadResult = uploadFile(file)
        val hash = uploadResult["hash"] as String
        
        // Scan the uploaded file
        return scanFile(hash, determineFileType(file))
    }
    
    private fun uploadFile(file: MultipartFile): Map<String, Any> {
        try {
            // Create a custom ByteArrayResource that properly implements getFilename()
            val fileResource = object : ByteArrayResource(file.bytes) {
                override fun getFilename(): String {
                    return file.originalFilename ?: "app"
                }
                
                override fun getDescription(): String {
                    return "File resource for ${filename}"
                }
                
                override fun contentLength(): Long {
                    return file.size
                }
            }
            
            // Create a MultiValueMap for the multipart request
            val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
            
            // Add the file part with the correct name "file"
            // Add the file directly to the body
            body.add("file", fileResource)
            
            // Set up headers with multipart form data content type
            val headers = HttpHeaders()
            headers.contentType = MediaType.MULTIPART_FORM_DATA
            headers.set("Authorization", appConfig.apiKey)
            
            // Create the request entity with the body and headers
            val requestEntity = HttpEntity(body, headers)
            
            // Log the request details for debugging
            println("Sending request to: ${appConfig.mobsfUrl}/api/v1/upload")
            println("Authorization: ${appConfig.apiKey}")
            println("File name: ${file.originalFilename}")
            println("File size: ${file.size} bytes")
            
            // Make the request to the MobSF API
            val response = restTemplate.exchange(
                "${appConfig.mobsfUrl}/api/v1/upload",
                HttpMethod.POST,
                requestEntity,
                Map::class.java
            )
            
            // Return the response body
            return response.body as Map<String, Any>
        } catch (e: Exception) {
            println("Error uploading file: ${e.message}")
            e.printStackTrace()
            throw RuntimeException("Failed to upload file to MobSF: ${e.message}", e)
        }
    }
    
    private fun scanFile(hash: String, fileType: String): Map<String, Any> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.set("Authorization", appConfig.apiKey)
        
        val body = LinkedMultiValueMap<String, String>()
        body.add("hash", hash)
        body.add("scan_type", fileType)
        
        val requestEntity = HttpEntity(body, headers)
        
        val response = restTemplate.exchange(
            "${appConfig.mobsfUrl}/api/v1/scan",
            HttpMethod.POST,
            requestEntity,
            Map::class.java
        )
        
        return response.body as Map<String, Any>
    }
    
    fun getReport(hash: String, reportType: String): Map<String, Any> {
        val headers = HttpHeaders()
        headers.set("Authorization", appConfig.apiKey)
        
        val requestEntity = HttpEntity<Void>(headers)
        
        val response = restTemplate.exchange(
            "${appConfig.mobsfUrl}/api/v1/report_json?hash=$hash&type=$reportType",
            HttpMethod.GET,
            requestEntity,
            Map::class.java
        )
        
        return response.body as Map<String, Any>
    }
    
    private fun determineFileType(file: MultipartFile): String {
        return when {
            file.originalFilename?.endsWith(".apk") == true -> "apk"
            file.originalFilename?.endsWith(".ipa") == true -> "ipa"
            file.originalFilename?.endsWith(".appx") == true -> "appx"
            file.originalFilename?.endsWith(".zip") == true -> "zip"
            else -> "apk" // Default to APK
        }
    }
}