package fr.heneria.cosmetics.model;

import org.bukkit.Material;
import java.util.List;

public abstract class Cosmetic {
    private final String id;
    private final String name;
    private final String permission;
    private final Material icon;
    private final List<String> lore;

    public Cosmetic(String id, String name, String permission, Material icon, List<String> lore) {
        this.id = id;
        this.name = name;
        this.permission = permission;
        this.icon = icon;
        this.lore = lore;
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

    public Material getIcon() {
        return icon;
    }

    public List<String> getLore() {
        return lore;
    }
}
