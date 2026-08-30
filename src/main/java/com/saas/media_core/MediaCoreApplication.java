package com.saas.media_core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; 

@SpringBootApplication
@EnableScheduling 
public class MediaCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediaCoreApplication.class, args);
    }
}