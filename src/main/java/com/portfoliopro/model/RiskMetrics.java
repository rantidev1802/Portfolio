package com.portfoliopro.model;

import java.io.Serializable;

/**
 * Quantitative risk management metrics: Beta, Sharpe, VaR, Volatility, Max Drawdown.
 */
public class RiskMetrics implements Serializable {
    private double portfolioBeta;        // Systematic risk compared to SPY
    private double sharpeRatio;          // Risk-adjusted excess return per unit of volatility
    private double var95Dollar;          // 1-day Value at Risk (95% confidence) in Dollars
    private double var95Percent;         // VaR as percentage of portfolio
    private double annualizedVolatility; // Annualized standard deviation of returns (%)
    private double maxDrawdown;          // Peak-to-trough decline (%)
    private String riskLevel;            // Low, Moderate, High, Aggressive
    private double diversificationIndex; // Concentration metric (0 to 100, 100 = perfectly diversified)

    public RiskMetrics() {}

    public RiskMetrics(double portfolioBeta, double sharpeRatio, double var95Dollar,
                       double var95Percent, double annualizedVolatility, double maxDrawdown,
                       String riskLevel, double diversificationIndex) {
        this.portfolioBeta = portfolioBeta;
        this.sharpeRatio = sharpeRatio;
        this.var95Dollar = var95Dollar;
        this.var95Percent = var95Percent;
        this.annualizedVolatility = annualizedVolatility;
        this.maxDrawdown = maxDrawdown;
        this.riskLevel = riskLevel;
        this.diversificationIndex = diversificationIndex;
    }

    public double getPortfolioBeta() { return portfolioBeta; }
    public void setPortfolioBeta(double portfolioBeta) { this.portfolioBeta = portfolioBeta; }

    public double getSharpeRatio() { return sharpeRatio; }
    public void setSharpeRatio(double sharpeRatio) { this.sharpeRatio = sharpeRatio; }

    public double getVar95Dollar() { return var95Dollar; }
    public void setVar95Dollar(double var95Dollar) { this.var95Dollar = var95Dollar; }

    public double getVar95Percent() { return var95Percent; }
    public void setVar95Percent(double var95Percent) { this.var95Percent = var95Percent; }

    public double getAnnualizedVolatility() { return annualizedVolatility; }
    public void setAnnualizedVolatility(double annualizedVolatility) { this.annualizedVolatility = annualizedVolatility; }

    public double getMaxDrawdown() { return maxDrawdown; }
    public void setMaxDrawdown(double maxDrawdown) { this.maxDrawdown = maxDrawdown; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public double getDiversificationIndex() { return diversificationIndex; }
    public void setDiversificationIndex(double diversificationIndex) { this.diversificationIndex = diversificationIndex; }
}

