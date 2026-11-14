package com.portfoliopro.model;

import java.io.Serializable;
import java.util.List;

/**
 * Technical indicators calculated from price series (SMA, EMA, RSI, MACD, Bollinger Bands).
 */
public class TechnicalIndicators implements Serializable {
    private String symbol;
    private double currentPrice;
    private double sma20;
    private double sma50;
    private double ema12;
    private double ema26;
    private double rsi14;
    private double macd;
    private double macdSignal;
    private double macdHistogram;
    private double bollingerUpper;
    private double bollingerMiddle;
    private double bollingerLower;
    private String signal; // STRONG_BUY, BUY, NEUTRAL, SELL, STRONG_SELL
    private String rsiSignal;
    private String macdSignalInterpretation;
    private List<Double> rsiSeries;
    private List<Double> sma20Series;

    public TechnicalIndicators() {}

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public double getSma20() { return sma20; }
    public void setSma20(double sma20) { this.sma20 = sma20; }

    public double getSma50() { return sma50; }
    public void setSma50(double sma50) { this.sma50 = sma50; }

    public double getEma12() { return ema12; }
    public void setEma12(double ema12) { this.ema12 = ema12; }

    public double getEma26() { return ema26; }
    public void setEma26(double ema26) { this.ema26 = ema26; }

    public double getRsi14() { return rsi14; }
    public void setRsi14(double rsi14) { this.rsi14 = rsi14; }

    public double getMacd() { return macd; }
    public void setMacd(double macd) { this.macd = macd; }

    public double getMacdSignal() { return macdSignal; }
    public void setMacdSignal(double macdSignal) { this.macdSignal = macdSignal; }

    public double getMacdHistogram() { return macdHistogram; }
    public void setMacdHistogram(double macdHistogram) { this.macdHistogram = macdHistogram; }

    public double getBollingerUpper() { return bollingerUpper; }
    public void setBollingerUpper(double bollingerUpper) { this.bollingerUpper = bollingerUpper; }

    public double getBollingerMiddle() { return bollingerMiddle; }
    public void setBollingerMiddle(double bollingerMiddle) { this.bollingerMiddle = bollingerMiddle; }

    public double getBollingerLower() { return bollingerLower; }
    public void setBollingerLower(double bollingerLower) { this.bollingerLower = bollingerLower; }

    public String getSignal() { return signal; }
    public void setSignal(String signal) { this.signal = signal; }

    public String getRsiSignal() { return rsiSignal; }
    public void setRsiSignal(String rsiSignal) { this.rsiSignal = rsiSignal; }

    public String getMacdSignalInterpretation() { return macdSignalInterpretation; }
    public void setMacdSignalInterpretation(String macdSignalInterpretation) { this.macdSignalInterpretation = macdSignalInterpretation; }

    public List<Double> getRsiSeries() { return rsiSeries; }
    public void setRsiSeries(List<Double> rsiSeries) { this.rsiSeries = rsiSeries; }

    public List<Double> getSma20Series() { return sma20Series; }
    public void setSma20Series(List<Double> sma20Series) { this.sma20Series = sma20Series; }
}

