package fr.heneria.cosmetics.model;

import org.bukkit.Material;
import java.util.List;

public class Gadget extends Cosmetic {
    private final Material itemMaterial;

    public Gadget(String id, String name, String permission, String hdbId, List<String> lore, Material itemMaterial) {
        super(id, name, permission, hdbId, lore, "gadgets");
        this.itemMaterial = itemMaterial;
    }

    public Material getItemMaterial() {
        return itemMaterial;
    }
}
