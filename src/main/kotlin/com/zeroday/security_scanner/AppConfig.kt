package com.zeroday.security_scanner

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class AppConfig {
    @Value("\${mobsf.url}")
    lateinit var mobsfUrl: String
    
    @Value("\${mobsf.api-key}")
    lateinit var apiKey: String
    
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}