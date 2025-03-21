package com.zeroday.security_scanner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SecurityScannerApplication

fun main(args: Array<String>) {
	runApplication<SecurityScannerApplication>(*args)
}
