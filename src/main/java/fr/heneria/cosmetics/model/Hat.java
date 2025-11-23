package fr.heneria.cosmetics.model;

import java.util.List;

import org.bukkit.Material;

public class Hat extends Cosmetic {
    public Hat(String id, String name, String permission, String hdbId, Material iconMaterial, Rarity rarity, List<String> lore) {
        super(id, name, permission, hdbId, iconMaterial, rarity, lore, "hats");
    }
}
