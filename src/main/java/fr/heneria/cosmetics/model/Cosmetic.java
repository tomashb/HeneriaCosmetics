package fr.heneria.cosmetics.model;

import java.util.List;

public abstract class Cosmetic {
    private final String id;
    private final String name;
    private final String permission;
    private final String hdbId;
    private final List<String> lore;
    private final String category;

    public Cosmetic(String id, String name, String permission, String hdbId, List<String> lore, String category) {
        this.id = id;
        this.name = name;
        this.permission = permission;
        this.hdbId = hdbId;
        this.lore = lore;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public String getHdbId() {
        return hdbId;
    }

    public List<String> getLore() {
        return lore;
    }

    public String getCategory() {
        return category;
    }
}
