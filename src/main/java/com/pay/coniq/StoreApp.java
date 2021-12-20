package com.pay.coniq;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class StoreApp {
    public static void main(String[] args) {
        SpringApplication.run(StoreApp.class, args);
    }


}
