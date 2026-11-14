package com.portfoliopro.controller;

import com.portfoliopro.model.TradeExecutionResponse;
import com.portfoliopro.model.TradeOrder;
import com.portfoliopro.service.TradingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for trade execution and transaction audit history.
 */
@RestController
@RequestMapping("/api/trade")
@CrossOrigin(origins = "*")
public class TradeController {

    private final TradingService tradingService;

    public TradeController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    @PostMapping("/execute")
    public ResponseEntity<TradeExecutionResponse> executeTrade(@RequestBody Map<String, Object> payload) {
        String symbol = (String) payload.get("symbol");
        String orderType = (String) payload.get("orderType");
        Number qtyNum = (Number) payload.get("quantity");

        if (symbol == null || orderType == null || qtyNum == null) {
            return ResponseEntity.badRequest().body(
                    new TradeExecutionResponse(false, "symbol, orderType, and quantity are required.", null, tradingService.getCashBalance())
            );
        }

        int quantity = qtyNum.intValue();
        TradeExecutionResponse response = tradingService.executeOrder(symbol, orderType, quantity);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<TradeOrder>> getTransactionHistory() {
        return ResponseEntity.ok(tradingService.getTransactionHistory());
    }
}

