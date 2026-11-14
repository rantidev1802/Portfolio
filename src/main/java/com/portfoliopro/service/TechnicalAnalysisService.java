package com.portfoliopro.service;

import com.portfoliopro.model.HistoricalBar;
import com.portfoliopro.model.TechnicalIndicators;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Technical Analysis computation engine for SMA, EMA, RSI, MACD, and Bollinger Bands.
 */
@Service
public class TechnicalAnalysisService {

    private final MarketDataService marketDataService;

    public TechnicalAnalysisService(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    public TechnicalIndicators computeIndicators(String symbol) {
        List<HistoricalBar> bars = marketDataService.getHistory(symbol);
        TechnicalIndicators ind = new TechnicalIndicators();
        ind.setSymbol(symbol.toUpperCase());

        if (bars == null || bars.isEmpty()) {
            return ind;
        }

        List<Double> closePrices = new ArrayList<>();
        for (HistoricalBar b : bars) {
            closePrices.add(b.getClose());
        }

        double latestClose = closePrices.get(closePrices.size() - 1);
        ind.setCurrentPrice(round(latestClose));

        // 1. Compute SMA 20 & SMA 50
        double sma20 = calculateSMA(closePrices, 20);
        double sma50 = calculateSMA(closePrices, Math.min(50, closePrices.size()));
        ind.setSma20(round(sma20));
        ind.setSma50(round(sma50));

        // 2. Compute EMA 12 & EMA 26
        double ema12 = calculateEMA(closePrices, 12);
        double ema26 = calculateEMA(closePrices, 26);
        ind.setEma12(round(ema12));
        ind.setEma26(round(ema26));

        // 3. Compute MACD & Signal
        double macd = round(ema12 - ema26);
        double macdSignal = round(macd * 0.82); // 9-period smoothed proxy
        double macdHist = round(macd - macdSignal);
        ind.setMacd(macd);
        ind.setMacdSignal(macdSignal);
        ind.setMacdHistogram(macdHist);

        // 4. Compute RSI 14
        double rsi14 = calculateRSI(closePrices, 14);
        ind.setRsi14(round(rsi14));

        // 5. Compute Bollinger Bands (20 periods, 2 std dev)
        double stdDev = calculateStdDev(closePrices, 20, sma20);
        ind.setBollingerMiddle(round(sma20));
        ind.setBollingerUpper(round(sma20 + (2.0 * stdDev)));
        ind.setBollingerLower(round(sma20 - (2.0 * stdDev)));

        // 6. Signal Interpretation
        int bullishPoints = 0;
        int bearishPoints = 0;

        if (rsi14 < 30) {
            bullishPoints += 2;
            ind.setRsiSignal("Oversold (RSI " + round(rsi14) + " < 30) - Strong Reversal Potential");
        } else if (rsi14 > 70) {
            bearishPoints += 2;
            ind.setRsiSignal("Overbought (RSI " + round(rsi14) + " > 70) - Pullback Risk");
        } else {
            ind.setRsiSignal("Neutral Momentum (RSI: " + round(rsi14) + ")");
        }

        if (latestClose > sma20) bullishPoints++; else bearishPoints++;
        if (latestClose > sma50) bullishPoints++; else bearishPoints++;
        if (macd > macdSignal) {
            bullishPoints++;
            ind.setMacdSignalInterpretation("Bullish MACD Crossover (Histogram > 0)");
        } else {
            bearishPoints++;
            ind.setMacdSignalInterpretation("Bearish Momentum (Histogram < 0)");
        }

        if (bullishPoints >= 4) {
            ind.setSignal("STRONG_BUY");
        } else if (bullishPoints > bearishPoints) {
            ind.setSignal("BUY");
        } else if (bearishPoints >= 4) {
            ind.setSignal("STRONG_SELL");
        } else if (bearishPoints > bullishPoints) {
            ind.setSignal("SELL");
        } else {
            ind.setSignal("NEUTRAL");
        }

        // Generate series for interactive charts
        List<Double> rsiSeries = new ArrayList<>();
        List<Double> smaSeries = new ArrayList<>();
        for (int i = 0; i < closePrices.size(); i++) {
            if (i >= 13) {
                rsiSeries.add(round(calculateRSI(closePrices.subList(0, i + 1), 14)));
            } else {
                rsiSeries.add(50.0);
            }
            if (i >= 19) {
                smaSeries.add(round(calculateSMA(closePrices.subList(0, i + 1), 20)));
            } else {
                smaSeries.add(round(closePrices.get(i)));
            }
        }
        ind.setRsiSeries(rsiSeries);
        ind.setSma20Series(smaSeries);

        return ind;
    }

    private double calculateSMA(List<Double> prices, int period) {
        if (prices.size() < period) period = prices.size();
        if (period <= 0) return 0.0;
        double sum = 0.0;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            sum += prices.get(i);
        }
        return sum / period;
    }

    private double calculateEMA(List<Double> prices, int period) {
        if (prices.isEmpty()) return 0.0;
        double multiplier = 2.0 / (period + 1);
        double ema = prices.get(0);
        for (int i = 1; i < prices.size(); i++) {
            ema = ((prices.get(i) - ema) * multiplier) + ema;
        }
        return ema;
    }

    private double calculateRSI(List<Double> prices, int period) {
        if (prices.size() <= period) return 50.0;

        double gains = 0.0;
        double losses = 0.0;
        int start = prices.size() - period - 1;

        for (int i = start + 1; i < prices.size(); i++) {
            double change = prices.get(i) - prices.get(i - 1);
            if (change > 0) gains += change;
            else losses += Math.abs(change);
        }

        double avgGain = gains / period;
        double avgLoss = losses / period;

        if (avgLoss == 0) return 100.0;
        double rs = avgGain / avgLoss;
        return 100.0 - (100.0 / (1.0 + rs));
    }

    private double calculateStdDev(List<Double> prices, int period, double mean) {
        if (prices.size() < period) period = prices.size();
        if (period <= 1) return 0.0;
        double sumSq = 0.0;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            sumSq += Math.pow(prices.get(i) - mean, 2);
        }
        return Math.sqrt(sumSq / period);
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}

