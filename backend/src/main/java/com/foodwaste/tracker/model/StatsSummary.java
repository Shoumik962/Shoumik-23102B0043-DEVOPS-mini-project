package com.foodwaste.tracker.model;

import java.util.Map;

/**
 * Model representing aggregated dashboard statistics.
 */
public class StatsSummary {
    private String totalWasteLogged;
    private double totalWasteKg;
    private int totalEntries;
    private int activeEntries;
    private int reviewedEntries;
    private int flaggedEntries;
    private String reductionRate;
    private int openAlerts;
    private Map<String, Double> categoryBreakdown;
    private Map<String, Double> sourceBreakdown;

    public StatsSummary() {}

    public StatsSummary(String totalWasteLogged, double totalWasteKg, int totalEntries, 
                        int activeEntries, int reviewedEntries, int flaggedEntries,
                        String reductionRate, int openAlerts,
                        Map<String, Double> categoryBreakdown, Map<String, Double> sourceBreakdown) {
        this.totalWasteLogged = totalWasteLogged;
        this.totalWasteKg = totalWasteKg;
        this.totalEntries = totalEntries;
        this.activeEntries = activeEntries;
        this.reviewedEntries = reviewedEntries;
        this.flaggedEntries = flaggedEntries;
        this.reductionRate = reductionRate;
        this.openAlerts = openAlerts;
        this.categoryBreakdown = categoryBreakdown;
        this.sourceBreakdown = sourceBreakdown;
    }

    public String getTotalWasteLogged() {
        return totalWasteLogged;
    }

    public void setTotalWasteLogged(String totalWasteLogged) {
        this.totalWasteLogged = totalWasteLogged;
    }

    public double getTotalWasteKg() {
        return totalWasteKg;
    }

    public void setTotalWasteKg(double totalWasteKg) {
        this.totalWasteKg = totalWasteKg;
    }

    public int getTotalEntries() {
        return totalEntries;
    }

    public void setTotalEntries(int totalEntries) {
        this.totalEntries = totalEntries;
    }

    public int getActiveEntries() {
        return activeEntries;
    }

    public void setActiveEntries(int activeEntries) {
        this.activeEntries = activeEntries;
    }

    public int getReviewedEntries() {
        return reviewedEntries;
    }

    public void setReviewedEntries(int reviewedEntries) {
        this.reviewedEntries = reviewedEntries;
    }

    public int getFlaggedEntries() {
        return flaggedEntries;
    }

    public void setFlaggedEntries(int flaggedEntries) {
        this.flaggedEntries = flaggedEntries;
    }

    public String getReductionRate() {
        return reductionRate;
    }

    public void setReductionRate(String reductionRate) {
        this.reductionRate = reductionRate;
    }

    public int getOpenAlerts() {
        return openAlerts;
    }

    public void setOpenAlerts(int openAlerts) {
        this.openAlerts = openAlerts;
    }

    public Map<String, Double> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public void setCategoryBreakdown(Map<String, Double> categoryBreakdown) {
        this.categoryBreakdown = categoryBreakdown;
    }

    public Map<String, Double> getSourceBreakdown() {
        return sourceBreakdown;
    }

    public void setSourceBreakdown(Map<String, Double> sourceBreakdown) {
        this.sourceBreakdown = sourceBreakdown;
    }
}
