package com.foodwaste.tracker.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Model representing a single food waste log entry.
 */
public class FoodWasteEntry {
    private String id;
    private String source;
    private String category;
    private double quantity;
    private String unit;
    private String status; // Reviewed, Pending, Flagged
    private String date;
    private String notes;

    public FoodWasteEntry() {
        this.unit = "kg";
        this.status = "Pending";
        this.date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public FoodWasteEntry(String id, String source, String category, double quantity, String unit, String status, String date, String notes) {
        this.id = id;
        this.source = source;
        this.category = category;
        this.quantity = quantity;
        this.unit = (unit == null || unit.isBlank()) ? "kg" : unit;
        this.status = (status == null || status.isBlank()) ? "Pending" : status;
        this.date = (date == null || date.isBlank()) ? LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) : date;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Formatted quantity string for frontend display (e.g., "8.2 kg").
     */
    public String getFormattedQuantity() {
        return String.format("%.1f %s", quantity, unit != null ? unit : "kg");
    }
}
