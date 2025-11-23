package fr.heneria.cosmetics.model;

import org.bukkit.inventory.ItemStack;

public class ActiveCosmetic {
    private ItemStack realHelmet;
    private String currentHatId;
    private String currentParticleId;
    private String currentGadgetId;

    public ActiveCosmetic() {
    }

    public ItemStack getRealHelmet() {
        return realHelmet;
    }

    public void setRealHelmet(ItemStack realHelmet) {
        this.realHelmet = realHelmet;
    }

    public String getCurrentHatId() {
        return currentHatId;
    }

    public void setCurrentHatId(String currentHatId) {
        this.currentHatId = currentHatId;
    }

    public String getCurrentParticleId() {
        return currentParticleId;
    }

    public void setCurrentParticleId(String currentParticleId) {
        this.currentParticleId = currentParticleId;
    }

    public boolean hasHat() {
        return currentHatId != null;
    }

    public boolean hasParticle() {
        return currentParticleId != null;
    }

    public String getCurrentGadgetId() {
        return currentGadgetId;
    }

    public void setCurrentGadgetId(String currentGadgetId) {
        this.currentGadgetId = currentGadgetId;
    }

    public boolean hasGadget() {
        return currentGadgetId != null;
    }
}
