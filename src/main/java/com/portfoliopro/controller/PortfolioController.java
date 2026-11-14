package com.portfoliopro.controller;

import com.portfoliopro.model.*;
import com.portfoliopro.service.PortfolioService;
import com.portfoliopro.service.RiskManagementService;
import com.portfoliopro.service.TradingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for portfolio valuation, asset allocation, risk metrics, and account management.
 */
@RestController
@RequestMapping("/api/portfolio")
@CrossOrigin(origins = "*")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final RiskManagementService riskManagementService;
    private final TradingService tradingService;

    public PortfolioController(PortfolioService portfolioService,
                               RiskManagementService riskManagementService,
                               TradingService tradingService) {
        this.portfolioService = portfolioService;
        this.riskManagementService = riskManagementService;
        this.tradingService = tradingService;
    }

    @GetMapping("/summary")
    public ResponseEntity<PortfolioSummary> getSummary() {
        return ResponseEntity.ok(portfolioService.getPortfolioSummary());
    }

    @GetMapping("/allocations")
    public ResponseEntity<List<AssetAllocation>> getAllocations() {
        return ResponseEntity.ok(portfolioService.getAssetAllocations());
    }

    @GetMapping("/risk")
    public ResponseEntity<RiskMetrics> getRiskMetrics() {
        return ResponseEntity.ok(riskManagementService.calculateRiskMetrics());
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetPortfolio() {
        tradingService.resetPortfolio();
        return ResponseEntity.ok(Map.of("success", true, "message", "Portfolio successfully reset to initial $100,000 cash balance with sample assets."));
    }

    @PostMapping("/deposit")
    public ResponseEntity<Map<String, Object>> depositCash(@RequestBody Map<String, Object> payload) {
        Number amountNum = (Number) payload.get("amount");
        if (amountNum == null || amountNum.doubleValue() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Deposit amount must be positive."));
        }
        double amount = amountNum.doubleValue();
        tradingService.depositCash(amount);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Successfully deposited $" + String.format("%.2f", amount) + " virtual cash.",
                "newBalance", tradingService.getCashBalance()
        ));
    }
}

