package fr.heneria.cosmetics.model;

public enum Rarity {
    COMMON("<!italic><white>Rareté: <bold>COMMUN</bold>"),
    RARE("<!italic><white>Rareté: <aqua><bold>RARE</bold>"),
    EPIC("<!italic><white>Rareté: <light_purple><bold>ÉPIQUE</bold>"),
    LEGENDARY("<!italic><white>Rareté: <gold><bold>LÉGENDAIRE</bold>"),
    COMMUN("<!italic><white>Rareté: <bold>COMMUN</bold>"), // Legacy support
    EPIQUE("<!italic><white>Rareté: <light_purple><bold>ÉPIQUE</bold>"), // Legacy support
    LEGENDAIRE("<!italic><white>Rareté: <gold><bold>LÉGENDAIRE</bold>"); // Legacy support

    private final String display;

    Rarity(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}
