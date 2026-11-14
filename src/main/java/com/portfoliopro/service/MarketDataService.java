package com.portfoliopro.service;

import com.portfoliopro.model.FundamentalData;
import com.portfoliopro.model.HistoricalBar;
import com.portfoliopro.model.StockQuote;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * High-performance Market Data Service simulating real-time prices, order books,
 * historical candlestick bars, and fundamental asset metadata.
 */
@Service
public class MarketDataService {

    private final Map<String, StockQuote> quoteMap = new ConcurrentHashMap<>();
    private final Map<String, FundamentalData> fundamentalMap = new ConcurrentHashMap<>();
    private final Map<String, List<HistoricalBar>> historyMap = new ConcurrentHashMap<>();

    public MarketDataService() {
        initCatalog();
        generateHistoricalData();
    }

    private void initCatalog() {
        // Seed initial market data
        addStock("AAPL", "Apple Inc.", 182.50, 180.20, 183.90, 179.80, 52400000, "Technology");
        addStock("MSFT", "Microsoft Corp.", 420.75, 416.30, 422.50, 415.10, 22100000, "Technology");
        addStock("NVDA", "NVIDIA Corporation", 875.20, 852.10, 882.00, 848.50, 48300000, "Semiconductors");
        addStock("GOOGL", "Alphabet Inc.", 155.40, 153.80, 156.90, 153.20, 25800000, "Communication");
        addStock("AMZN", "Amazon.com Inc.", 178.90, 175.60, 180.20, 174.90, 31200000, "Consumer Cyclical");
        addStock("TSLA", "Tesla Inc.", 175.30, 171.10, 178.40, 170.20, 68900000, "Automotive");
        addStock("JPM", "JPMorgan Chase & Co.", 198.60, 196.40, 199.80, 195.90, 11400000, "Financial Services");
        addStock("SPY", "SPDR S&P 500 ETF Trust", 510.80, 508.10, 512.30, 507.40, 72500000, "Index ETF");

        // Seed fundamental profiles
        fundamentalMap.put("AAPL", new FundamentalData(
                "AAPL", "Apple Inc.", "Technology", "Consumer Electronics",
                "$2.82 Trillion", 28.4, 25.1, 6.42, 0.53,
                199.62, 164.08, 1.12, 205.00, "BUY",
                "Apple designs, manufactures, and markets smartphones, personal computers, tablets, wearables, and accessories, and sells a variety of related services."
        ));

        fundamentalMap.put("MSFT", new FundamentalData(
                "MSFT", "Microsoft Corp.", "Technology", "Software - Infrastructure",
                "$3.12 Trillion", 36.8, 31.2, 11.45, 0.71,
                430.82, 309.45, 0.89, 465.00, "STRONG_BUY",
                "Microsoft develops and supports software, services, devices and solutions worldwide, including Azure Cloud, Windows, Office 365, and AI Copilot."
        ));

        fundamentalMap.put("NVDA", new FundamentalData(
                "NVDA", "NVIDIA Corporation", "Technology", "Semiconductors",
                "$2.16 Trillion", 65.2, 34.8, 13.40, 0.02,
                974.00, 258.50, 1.68, 1050.00, "STRONG_BUY",
                "NVIDIA is the pioneer of GPU-accelerated computing, dominating artificial intelligence training, data center infrastructure, and autonomous systems."
        ));

        fundamentalMap.put("GOOGL", new FundamentalData(
                "GOOGL", "Alphabet Inc.", "Communication", "Internet Content & Information",
                "$1.94 Trillion", 24.1, 20.3, 6.45, 0.49,
                160.22, 115.35, 1.05, 180.00, "BUY",
                "Alphabet operates Google Search, YouTube, Android, Chrome, and Google Cloud, investing aggressively in Gemini AI architectures."
        ));

        fundamentalMap.put("AMZN", new FundamentalData(
                "AMZN", "Amazon.com Inc.", "Consumer Cyclical", "Internet Retail",
                "$1.86 Trillion", 42.5, 31.0, 4.21, 0.00,
                189.77, 118.35, 1.16, 215.00, "STRONG_BUY",
                "Amazon engages in retail sale of consumer products and subscriptions through online stores, and powers enterprise computing via Amazon Web Services (AWS)."
        ));

        fundamentalMap.put("TSLA", new FundamentalData(
                "TSLA", "Tesla Inc.", "Consumer Cyclical", "Auto Manufacturers",
                "$558 Billion", 45.8, 38.2, 3.82, 0.00,
                299.29, 138.80, 2.34, 210.00, "HOLD",
                "Tesla designs, manufactures, and sells electric vehicles, energy generation and storage systems, and develops Full Self-Driving autonomous robotics."
        ));

        fundamentalMap.put("JPM", new FundamentalData(
                "JPM", "JPMorgan Chase & Co.", "Financial Services", "Banks - Diversified",
                "$568 Billion", 11.8, 10.9, 16.80, 2.31,
                205.88, 133.00, 1.08, 220.00, "BUY",
                "JPMorgan Chase is one of the largest financial institutions in the world, offering investment banking, commercial banking, asset management, and treasury services."
        ));

        fundamentalMap.put("SPY", new FundamentalData(
                "SPY", "SPDR S&P 500 ETF Trust", "Index ETF", "Large Cap Blend",
                "$504 Billion", 23.4, 21.0, 21.80, 1.34,
                524.61, 409.21, 1.00, 540.00, "BUY",
                "The SPDR S&P 500 ETF Trust tracks the investment results of the S&P 500 Index, representing approximately 500 top publicly traded U.S. companies."
        ));
    }

    private void addStock(String symbol, String name, double price, double prevClose,
                          double high, double low, long vol, String sector) {
        double change = round(price - prevClose);
        double changePercent = round((change / prevClose) * 100.0);
        quoteMap.put(symbol, new StockQuote(symbol, name, price, change, changePercent, high, low, vol, prevClose, sector));
    }

    /**
     * Generate 30 daily candlestick bars for charting and indicators.
     */
    private void generateHistoricalData() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Map.Entry<String, StockQuote> entry : quoteMap.entrySet()) {
            String sym = entry.getKey();
            StockQuote quote = entry.getValue();
            double basePrice = quote.getPreviousClose() * 0.88; // 30 days ago
            List<HistoricalBar> bars = new ArrayList<>();

            double current = basePrice;
            for (int i = 30; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                // Random walk drift
                double returnPct = (ThreadLocalRandom.current().nextDouble() - 0.48) * 0.035;
                double open = round(current);
                double close = (i == 0) ? quote.getCurrentPrice() : round(open * (1.0 + returnPct));
                double high = round(Math.max(open, close) * (1.0 + ThreadLocalRandom.current().nextDouble() * 0.015));
                double low = round(Math.min(open, close) * (1.0 - ThreadLocalRandom.current().nextDouble() * 0.015));
                long vol = ThreadLocalRandom.current().nextLong(15000000, 65000000);

                bars.add(new HistoricalBar(date.format(fmt), open, high, low, close, vol));
                current = close;
            }
            historyMap.put(sym, bars);
        }
    }

    /**
     * Simulate real-time market fluctuations (Geometric Brownian Motion tick).
     */
    public synchronized void simulateTick() {
        for (StockQuote quote : quoteMap.values()) {
            double oldPrice = quote.getCurrentPrice();
            // Random price tick (-0.4% to +0.4%)
            double delta = (ThreadLocalRandom.current().nextDouble() - 0.495) * 0.008 * oldPrice;
            double newPrice = round(Math.max(1.0, oldPrice + delta));

            quote.setCurrentPrice(newPrice);
            double change = round(newPrice - quote.getPreviousClose());
            quote.setChange(change);
            quote.setChangePercent(round((change / quote.getPreviousClose()) * 100.0));

            if (newPrice > quote.getDayHigh()) quote.setDayHigh(newPrice);
            if (newPrice < quote.getDayLow()) quote.setDayLow(newPrice);

            // Update today's latest candle in history
            List<HistoricalBar> bars = historyMap.get(quote.getSymbol());
            if (bars != null && !bars.isEmpty()) {
                HistoricalBar latest = bars.get(bars.size() - 1);
                latest.setClose(newPrice);
                if (newPrice > latest.getHigh()) latest.setHigh(newPrice);
                if (newPrice < latest.getLow()) latest.setLow(newPrice);
            }
        }
    }

    public List<StockQuote> getAllQuotes() {
        simulateTick();
        return new ArrayList<>(quoteMap.values());
    }

    public StockQuote getQuote(String symbol) {
        simulateTick();
        return quoteMap.get(symbol.toUpperCase());
    }

    public List<HistoricalBar> getHistory(String symbol) {
        return historyMap.getOrDefault(symbol.toUpperCase(), Collections.emptyList());
    }

    public FundamentalData getFundamental(String symbol) {
        return fundamentalMap.get(symbol.toUpperCase());
    }

    public boolean symbolExists(String symbol) {
        return quoteMap.containsKey(symbol.toUpperCase());
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}

