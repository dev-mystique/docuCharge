package com.example.mssqll;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MssqllApplication {
    public static void main(String[] args) {
        SpringApplication.run(MssqllApplication.class, args);
    }
}
