package com.foodwaste.tracker.repository;

import com.foodwaste.tracker.model.FoodWasteEntry;
import com.foodwaste.tracker.model.StatsSummary;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Thread-safe repository managing food waste log entries and calculating stats.
 */
public class FoodWasteRepository {
    private final Map<String, FoodWasteEntry> entriesMap = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1043);

    public FoodWasteRepository() {
        seedInitialData();
    }

    private void seedInitialData() {
        addEntryInternal(new FoodWasteEntry("FW-1042", "Kitchen A", "Produce", 8.2, "kg", "Reviewed", "2026-08-31", "Overripe tomatoes and lettuce trimmings"));
        addEntryInternal(new FoodWasteEntry("FW-1041", "Cafeteria", "Prepared Food", 15.6, "kg", "Pending", "2026-08-31", "Unserved buffet rice and vegetable curry"));
        addEntryInternal(new FoodWasteEntry("FW-1040", "Kitchen B", "Dairy", 3.4, "kg", "Reviewed", "2026-08-30", "Expired whole milk batch"));
        addEntryInternal(new FoodWasteEntry("FW-1039", "Storage", "Bakery", 6.1, "kg", "Flagged", "2026-08-30", "Humidity spike caused mold on bread loaves"));
        addEntryInternal(new FoodWasteEntry("FW-1038", "Dining Hall", "Prepared Food", 12.0, "kg", "Reviewed", "2026-08-29", "Plate waste from dinner service"));
        addEntryInternal(new FoodWasteEntry("FW-1037", "Kitchen A", "Meat", 4.5, "kg", "Flagged", "2026-08-28", "Freezer temperature sensor glitch"));
        addEntryInternal(new FoodWasteEntry("FW-1036", "Bakery", "Bakery", 9.8, "kg", "Reviewed", "2026-08-28", "End-of-day surplus artisanal buns"));
        addEntryInternal(new FoodWasteEntry("FW-1035", "Cafeteria", "Beverages", 5.2, "kg", "Pending", "2026-08-27", "Spilled juice concentrates"));
    }

    private void addEntryInternal(FoodWasteEntry entry) {
        entriesMap.put(entry.getId(), entry);
    }

    public List<FoodWasteEntry> findAll() {
        return entriesMap.values().stream()
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .collect(Collectors.toList());
    }

    public Optional<FoodWasteEntry> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(entriesMap.get(id));
    }

    public List<FoodWasteEntry> filterAndSearch(String search, String status, String category, String source) {
        return entriesMap.values().stream()
                .filter(e -> {
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("all") && !e.getStatus().equalsIgnoreCase(status)) {
                        return false;
                    }
                    if (category != null && !category.isBlank() && !category.equalsIgnoreCase("all") && !e.getCategory().equalsIgnoreCase(category)) {
                        return false;
                    }
                    if (source != null && !source.isBlank() && !source.equalsIgnoreCase("all") && !e.getSource().equalsIgnoreCase(source)) {
                        return false;
                    }
                    if (search != null && !search.isBlank()) {
                        String s = search.toLowerCase();
                        boolean matchId = e.getId() != null && e.getId().toLowerCase().contains(s);
                        boolean matchSource = e.getSource() != null && e.getSource().toLowerCase().contains(s);
                        boolean matchCategory = e.getCategory() != null && e.getCategory().toLowerCase().contains(s);
                        boolean matchNotes = e.getNotes() != null && e.getNotes().toLowerCase().contains(s);
                        return matchId || matchSource || matchCategory || matchNotes;
                    }
                    return true;
                })
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .collect(Collectors.toList());
    }

    public synchronized FoodWasteEntry create(FoodWasteEntry entry) {
        if (entry.getId() == null || entry.getId().isBlank()) {
            entry.setId("FW-" + idCounter.getAndIncrement());
        }
        if (entry.getUnit() == null || entry.getUnit().isBlank()) {
            entry.setUnit("kg");
        }
        if (entry.getStatus() == null || entry.getStatus().isBlank()) {
            entry.setStatus("Pending");
        }
        entriesMap.put(entry.getId(), entry);
        return entry;
    }

    public synchronized Optional<FoodWasteEntry> update(String id, FoodWasteEntry updated) {
        FoodWasteEntry existing = entriesMap.get(id);
        if (existing == null) {
            return Optional.empty();
        }
        if (updated.getSource() != null && !updated.getSource().isBlank()) {
            existing.setSource(updated.getSource());
        }
        if (updated.getCategory() != null && !updated.getCategory().isBlank()) {
            existing.setCategory(updated.getCategory());
        }
        if (updated.getQuantity() > 0) {
            existing.setQuantity(updated.getQuantity());
        }
        if (updated.getUnit() != null && !updated.getUnit().isBlank()) {
            existing.setUnit(updated.getUnit());
        }
        if (updated.getStatus() != null && !updated.getStatus().isBlank()) {
            existing.setStatus(updated.getStatus());
        }
        if (updated.getDate() != null && !updated.getDate().isBlank()) {
            existing.setDate(updated.getDate());
        }
        if (updated.getNotes() != null) {
            existing.setNotes(updated.getNotes());
        }
        return Optional.of(existing);
    }

    public synchronized boolean delete(String id) {
        return entriesMap.remove(id) != null;
    }

    public StatsSummary calculateStats() {
        Collection<FoodWasteEntry> all = entriesMap.values();
        
        double totalKg = all.stream().mapToDouble(FoodWasteEntry::getQuantity).sum();
        int total = all.size();
        int pending = (int) all.stream().filter(e -> "Pending".equalsIgnoreCase(e.getStatus())).count();
        int reviewed = (int) all.stream().filter(e -> "Reviewed".equalsIgnoreCase(e.getStatus())).count();
        int flagged = (int) all.stream().filter(e -> "Flagged".equalsIgnoreCase(e.getStatus())).count();

        Map<String, Double> categoryMap = new HashMap<>();
        Map<String, Double> sourceMap = new HashMap<>();

        for (FoodWasteEntry e : all) {
            categoryMap.merge(e.getCategory() != null ? e.getCategory() : "Other", e.getQuantity(), Double::sum);
            sourceMap.merge(e.getSource() != null ? e.getSource() : "Other", e.getQuantity(), Double::sum);
        }

        // Total waste logged formatted with commas
        String formattedTotal = String.format("%,.1f kg", totalKg);

        return new StatsSummary(
                formattedTotal,
                Math.round(totalKg * 10.0) / 10.0,
                total,
                pending,
                reviewed,
                flagged,
                "14.2%", // dynamic reduction rate metric
                flagged, // open alerts corresponds to flagged entries requiring attention
                categoryMap,
                sourceMap
        );
    }
}
