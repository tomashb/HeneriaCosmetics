package fr.heneria.cosmetics.model;

public enum Rarity {
    COMMON("<!italic><white>Rareté: <bold>COMMUN</bold>", 1),
    RARE("<!italic><white>Rareté: <aqua><bold>RARE</bold>", 2),
    EPIC("<!italic><white>Rareté: <light_purple><bold>ÉPIQUE</bold>", 3),
    LEGENDARY("<!italic><white>Rareté: <gold><bold>LÉGENDAIRE</bold>", 4),
    COMMUN("<!italic><white>Rareté: <bold>COMMUN</bold>", 1), // Legacy support
    EPIQUE("<!italic><white>Rareté: <light_purple><bold>ÉPIQUE</bold>", 3), // Legacy support
    LEGENDAIRE("<!italic><white>Rareté: <gold><bold>LÉGENDAIRE</bold>", 4); // Legacy support

    private final String display;
    private final int level;

    Rarity(String display, int level) {
        this.display = display;
        this.level = level;
    }

    public String getDisplay() {
        return display;
    }

    public int getLevel() {
        return level;
    }
}
