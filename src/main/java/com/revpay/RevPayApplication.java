package com.revpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RevPayApplication {

    public static void main(String[] args) {
        SpringApplication.run(RevPayApplication.class, args);
    }
}
