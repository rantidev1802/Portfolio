package com.portfoliopro.controller;

import com.portfoliopro.model.*;
import com.portfoliopro.service.MarketDataService;
import com.portfoliopro.service.TechnicalAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for real-time market data, price history, technical signals, and fundamentals.
 */
@RestController
@RequestMapping("/api/market")
@CrossOrigin(origins = "*")
public class MarketDataController {

    private final MarketDataService marketDataService;
    private final TechnicalAnalysisService technicalAnalysisService;

    public MarketDataController(MarketDataService marketDataService,
                                TechnicalAnalysisService technicalAnalysisService) {
        this.marketDataService = marketDataService;
        this.technicalAnalysisService = technicalAnalysisService;
    }

    @GetMapping("/quotes")
    public ResponseEntity<List<StockQuote>> getAllQuotes() {
        return ResponseEntity.ok(marketDataService.getAllQuotes());
    }

    @GetMapping("/quote/{symbol}")
    public ResponseEntity<?> getQuote(@PathVariable String symbol) {
        StockQuote quote = marketDataService.getQuote(symbol);
        if (quote == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(quote);
    }

    @GetMapping("/history/{symbol}")
    public ResponseEntity<List<HistoricalBar>> getHistory(@PathVariable String symbol) {
        return ResponseEntity.ok(marketDataService.getHistory(symbol));
    }

    @GetMapping("/analysis/{symbol}")
    public ResponseEntity<TechnicalIndicators> getTechnicalAnalysis(@PathVariable String symbol) {
        return ResponseEntity.ok(technicalAnalysisService.computeIndicators(symbol));
    }

    @GetMapping("/fundamental/{symbol}")
    public ResponseEntity<?> getFundamental(@PathVariable String symbol) {
        FundamentalData data = marketDataService.getFundamental(symbol);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(data);
    }
}

