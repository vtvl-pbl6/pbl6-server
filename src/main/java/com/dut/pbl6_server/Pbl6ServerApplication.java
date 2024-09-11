package com.dut.pbl6_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Pbl6ServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(Pbl6ServerApplication.class, args);
    }
}
