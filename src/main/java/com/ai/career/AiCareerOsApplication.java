package com.ai.career;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AiCareerOsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCareerOsApplication.class, args);
    }
}
