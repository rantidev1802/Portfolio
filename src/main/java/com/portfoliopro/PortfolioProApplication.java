package com.portfoliopro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PortfolioPro Main Application Entry Point.
 */
@SpringBootApplication
public class PortfolioProApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortfolioProApplication.class, args);
        System.out.println("==========================================================");
        System.out.println("  PortfolioPro: Stock Trading & Portfolio Management     ");
        System.out.println("  Server running on http://localhost:8080               ");
        System.out.println("==========================================================");
    }
}

