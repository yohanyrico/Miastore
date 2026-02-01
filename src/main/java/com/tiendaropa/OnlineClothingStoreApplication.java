package com.tiendaropa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OnlineClothingStoreApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(OnlineClothingStoreApplication.class, args);
        System.out.println("====================================");
        System.out.println("Tienda de Ropa Online - Iniciada");
        System.out.println("http://localhost:8080");
        System.out.println("====================================");
    }
}