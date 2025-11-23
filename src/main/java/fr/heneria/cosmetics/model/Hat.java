package fr.heneria.cosmetics.model;

import java.util.List;

public class Hat extends Cosmetic {
    public Hat(String id, String name, String permission, String hdbId, List<String> lore) {
        super(id, name, permission, hdbId, lore, "hats");
    }
}
