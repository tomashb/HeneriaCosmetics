package fr.heneria.cosmetics.model;

import org.bukkit.Particle;
import java.util.List;

public class ParticleEffect extends Cosmetic {
    public enum Style {
        RING,
        TRAIL
    }

    private final Particle particleType;
    private final Style style;

    public ParticleEffect(String id, String name, String permission, String hdbId, List<String> lore, Particle particleType, Style style) {
        super(id, name, permission, hdbId, lore, "particles");
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
