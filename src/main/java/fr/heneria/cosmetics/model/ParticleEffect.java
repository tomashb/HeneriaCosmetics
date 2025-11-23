package fr.heneria.cosmetics.model;

import org.bukkit.Material;
import org.bukkit.Particle;
import java.util.List;

public class ParticleEffect extends Cosmetic {
    public enum Style {
        RING,
        TRAIL
    }

    private final Particle particleType;
    private final Style style;

    public ParticleEffect(String id, String name, String permission, String hdbId, Material iconMaterial, Rarity rarity, List<String> lore, Particle particleType, Style style) {
        super(id, name, permission, hdbId, iconMaterial, rarity, lore, "particles");
        this.particleType = particleType;
        this.style = style;
    }

    public Particle getParticleType() {
        return particleType;
    }

    public Style getStyle() {
        return style;
    }
}
