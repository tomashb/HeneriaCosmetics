package fr.heneria.cosmetics.model;

import org.bukkit.Material;
import java.util.List;

public class Hat extends Cosmetic {
    private final String hdbId;
    private final Material material; // For vanilla blocks

    public Hat(String id, String name, String permission, Material icon, List<String> lore, String hdbId, Material material) {
        super(id, name, permission, icon, lore);
        this.hdbId = hdbId;
        this.material = material;
    }

    public String getHdbId() {
        return hdbId;
    }

    public Material getMaterial() {
        return material;
    }
}
