package com.portfoliopro.model;

import java.io.Serializable;

/**
 * Breakdown of portfolio capital by asset / sector for portfolio visualization.
 */
public class AssetAllocation implements Serializable {
    private String label;
    private double value;
    private double percentage;
    private String color;

    public AssetAllocation() {}

    public AssetAllocation(String label, double value, double percentage, String color) {
        this.label = label;
        this.value = value;
        this.percentage = percentage;
        this.color = color;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}

