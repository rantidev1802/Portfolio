package com.portfoliopro.service;

import com.portfoliopro.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Portfolio Management Service computing Net Asset Value (NAV),
 * Unrealized/Realized P&L, asset weights, and sector allocation.
 */
@Service
public class PortfolioService {

    private final TradingService tradingService;
    private final MarketDataService marketDataService;

    public PortfolioService(TradingService tradingService, MarketDataService marketDataService) {
        this.tradingService = tradingService;
        this.marketDataService = marketDataService;
    }

    public PortfolioSummary getPortfolioSummary() {
        Map<String, Position> holdingsMap = tradingService.getHoldings();
        List<Position> positionList = new ArrayList<>(holdingsMap.values());

        double investedValue = 0.0;
        double totalUnrealizedPnL = 0.0;
        double totalCostBasis = 0.0;
        double dailyPnL = 0.0;

        for (Position pos : positionList) {
            StockQuote quote = marketDataService.getQuote(pos.getSymbol());
            if (quote != null) {
                pos.setCurrentPrice(quote.getCurrentPrice());
            }
            investedValue += pos.getMarketValue();
            totalUnrealizedPnL += pos.getUnrealizedPnL();
            totalCostBasis += pos.getCostBasis();

            if (quote != null) {
                dailyPnL += (quote.getChange() * pos.getShares());
            }
        }

        double cash = tradingService.getCashBalance();
        double totalPortfolioValue = round(cash + investedValue);
        investedValue = round(investedValue);
        totalUnrealizedPnL = round(totalUnrealizedPnL);
        dailyPnL = round(dailyPnL);

        // Compute weights
        for (Position pos : positionList) {
            if (totalPortfolioValue > 0) {
                pos.setPortfolioWeight(round((pos.getMarketValue() / totalPortfolioValue) * 100.0));
            } else {
                pos.setPortfolioWeight(0.0);
            }
        }

        double totalUnrealizedPct = (totalCostBasis > 0) ? round((totalUnrealizedPnL / totalCostBasis) * 100.0) : 0.0;
        double dailyPnLPct = (totalPortfolioValue > 0) ? round((dailyPnL / totalPortfolioValue) * 100.0) : 0.0;

        PortfolioSummary summary = new PortfolioSummary();
        summary.setTotalPortfolioValue(totalPortfolioValue);
        summary.setCashBalance(round(cash));
        summary.setInvestedValue(investedValue);
        summary.setTotalUnrealizedPnL(totalUnrealizedPnL);
        summary.setTotalUnrealizedPnLPercent(totalUnrealizedPct);
        summary.setTotalRealizedPnL(tradingService.getTotalRealizedPnL());
        summary.setDailyPnL(dailyPnL);
        summary.setDailyPnLPercent(dailyPnLPct);
        summary.setTotalPositions(positionList.size());
        summary.setPositions(positionList);

        return summary;
    }

    public List<AssetAllocation> getAssetAllocations() {
        PortfolioSummary summary = getPortfolioSummary();
        List<AssetAllocation> allocations = new ArrayList<>();
        double totalVal = summary.getTotalPortfolioValue();

        if (totalVal <= 0) return allocations;

        String[] palette = {"#3b82f6", "#10b981", "#8b5cf6", "#f59e0b", "#ec4899", "#06b6d4", "#6366f1"};
        int colorIdx = 0;

        for (Position pos : summary.getPositions()) {
            double pct = round((pos.getMarketValue() / totalVal) * 100.0);
            String col = palette[colorIdx % palette.length];
            allocations.add(new AssetAllocation(pos.getSymbol(), pos.getMarketValue(), pct, col));
            colorIdx++;
        }

        // Cash component
        double cashPct = round((summary.getCashBalance() / totalVal) * 100.0);
        allocations.add(new AssetAllocation("Cash USD", summary.getCashBalance(), cashPct, "#64748b"));

        return allocations;
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}

