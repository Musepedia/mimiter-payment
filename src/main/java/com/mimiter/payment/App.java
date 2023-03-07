package com.mimiter.payment;

import com.mimiter.payment.annotation.AnonymousAccess;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 核心后端模块。
 */
@RestController
@RequestMapping("/api")
@SpringBootApplication(scanBasePackages = {"com.mimiter"})
@ServletComponentScan
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @AnonymousAccess
    @GetMapping
    public String hello() {
        return "Hello MGS";
    }
}
