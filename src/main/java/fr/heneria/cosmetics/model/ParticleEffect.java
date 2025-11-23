package fr.heneria.cosmetics.model;

import org.bukkit.Material;
import org.bukkit.Particle;
import java.util.List;

public class ParticleEffect extends Cosmetic {
    private final Particle particleType;
    private final int count;

    public ParticleEffect(String id, String name, String permission, Material icon, List<String> lore, Particle particleType, int count) {
        super(id, name, permission, icon, lore);
        this.particleType = particleType;
        this.count = count;
    }

    public Particle getParticleType() {
        return particleType;
    }

    public int getCount() {
        return count;
    }
}
