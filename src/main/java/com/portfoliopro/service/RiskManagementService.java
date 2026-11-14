package com.portfoliopro.service;

import com.portfoliopro.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Quantitative Risk Management engine computing portfolio-level Beta,
 * Sharpe Ratio, Value at Risk (VaR 95%), Volatility, and Maximum Drawdown.
 */
@Service
public class RiskManagementService {

    private final PortfolioService portfolioService;
    private final MarketDataService marketDataService;

    // Benchmark assumptions
    private static final double RISK_FREE_RATE = 0.045; // 4.5% US Treasury yield

    public RiskManagementService(PortfolioService portfolioService, MarketDataService marketDataService) {
        this.portfolioService = portfolioService;
        this.marketDataService = marketDataService;
    }

    public RiskMetrics calculateRiskMetrics() {
        PortfolioSummary summary = portfolioService.getPortfolioSummary();
        List<Position> positions = summary.getPositions();
        double totalPortfolioValue = summary.getTotalPortfolioValue();
        double investedValue = summary.getInvestedValue();

        if (totalPortfolioValue <= 0 || positions.isEmpty()) {
            return new RiskMetrics(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "LOW (100% Cash)", 100.0);
        }

        // 1. Calculate Weighted Portfolio Beta
        double weightedBetaSum = 0.0;
        double maxDrawdownSum = 0.0;
        double sumWeightsSquared = 0.0; // Herfindahl index for diversification

        for (Position pos : positions) {
            FundamentalData fund = marketDataService.getFundamental(pos.getSymbol());
            double beta = (fund != null) ? fund.getBeta() : 1.0;
            double weightInInvested = (investedValue > 0) ? (pos.getMarketValue() / investedValue) : 0.0;
            weightedBetaSum += (beta * weightInInvested);

            // Maximum Drawdown estimation from 52W high
            if (fund != null && fund.getFiftyTwoWeekHigh() > 0) {
                double dd = (fund.getFiftyTwoWeekHigh() - pos.getCurrentPrice()) / fund.getFiftyTwoWeekHigh() * 100.0;
                maxDrawdownSum += (Math.max(0.0, dd) * weightInInvested);
            }

            // Concentration weight relative to total NAV
            double navWeight = pos.getMarketValue() / totalPortfolioValue;
            sumWeightsSquared += Math.pow(navWeight, 2);
        }

        // Add cash weight squared
        double cashWeight = summary.getCashBalance() / totalPortfolioValue;
        sumWeightsSquared += Math.pow(cashWeight, 2);

        // Portfolio Beta scaled by invested proportion
        double investedFraction = investedValue / totalPortfolioValue;
        double portfolioBeta = round(weightedBetaSum * investedFraction);

        // 2. Annualized Volatility estimation
        // Benchmark SPY volatility ~ 14.5% annualized
        double benchmarkVol = 0.145;
        double portfolioVol = benchmarkVol * (portfolioBeta > 0.1 ? portfolioBeta : 0.5);
        double annualizedVolPct = round(portfolioVol * 100.0);
        double dailyVol = portfolioVol / Math.sqrt(252.0);

        // 3. Value at Risk (1-Day VaR at 95% confidence, z = 1.645)
        double var95Pct = round(1.645 * dailyVol * 100.0);
        double var95Dollar = round(totalPortfolioValue * (var95Pct / 100.0));

        // 4. Sharpe Ratio
        // Expected portfolio return = Rf + Beta * (Market Return ~ 10% - Rf)
        double expectedReturn = RISK_FREE_RATE + portfolioBeta * (0.10 - RISK_FREE_RATE);
        double sharpe = (portfolioVol > 0) ? (expectedReturn - RISK_FREE_RATE) / portfolioVol : 0.0;
        double sharpeRatio = round(sharpe);

        // 5. Maximum Drawdown
        double maxDrawdown = round(maxDrawdownSum * investedFraction);

        // 6. Diversification Score (0 to 100)
        // Herfindahl Index ranges from 1/N (best) to 1.0 (worst)
        double diversificationScore = round(Math.max(0.0, Math.min(100.0, (1.0 - sumWeightsSquared) * 125.0)));

        // 7. Qualitative Risk Level
        String riskLevel;
        if (portfolioBeta < 0.6) {
            riskLevel = "Conservative / Low";
        } else if (portfolioBeta <= 1.05) {
            riskLevel = "Moderate / Balanced";
        } else if (portfolioBeta <= 1.35) {
            riskLevel = "Growth / Elevated";
        } else {
            riskLevel = "Aggressive / High";
        }

        return new RiskMetrics(
                portfolioBeta,
                sharpeRatio,
                var95Dollar,
                var95Pct,
                annualizedVolPct,
                maxDrawdown,
                riskLevel,
                diversificationScore
        );
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}

