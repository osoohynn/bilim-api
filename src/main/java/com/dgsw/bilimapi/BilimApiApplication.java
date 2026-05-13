package com.dgsw.bilimapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BilimApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BilimApiApplication.class, args);
    }

}
