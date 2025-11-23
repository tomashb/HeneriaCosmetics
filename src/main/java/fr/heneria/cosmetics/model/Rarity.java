package fr.heneria.cosmetics.model;

public enum Rarity {
    COMMUN("<gray>☆☆☆☆☆ <white>Commun"),
    RARE("<aqua>★★☆☆☆ <blue>Rare"),
    EPIQUE("<light_purple>★★★★☆ <#d000ff>Épique"),
    LEGENDAIRE("<yellow>★★★★★ <gold>Légendaire");

    private final String display;

    Rarity(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}
