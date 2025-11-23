package fr.heneria.cosmetics.model;

import org.bukkit.Material;
import java.util.List;

public class Gadget extends Cosmetic {
    private final Material itemMaterial;

    public Gadget(String id, String name, String permission, String hdbId, org.bukkit.Material iconMaterial, Rarity rarity, List<String> lore, Material itemMaterial) {
        super(id, name, permission, hdbId, iconMaterial, rarity, lore, "gadgets");
        this.itemMaterial = itemMaterial;
    }

    public Material getItemMaterial() {
        return itemMaterial;
    }
}
