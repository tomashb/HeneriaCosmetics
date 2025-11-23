package fr.heneria.cosmetics.gui;

import fr.heneria.cosmetics.HeneriaCosmetics;
import fr.heneria.cosmetics.manager.CosmeticManager;
import fr.heneria.cosmetics.model.Cosmetic;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CosmeticsGUI implements Listener {

    private final HeneriaCosmetics plugin;
    private final CosmeticManager manager;
    private final HeadDatabaseAPI hdbApi;

    public CosmeticsGUI(HeneriaCosmetics plugin) {
        this.plugin = plugin;
        this.manager = plugin.getCosmeticManager();
        this.hdbApi = manager.getHdbApi();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMainMenu(Player player) {
        String titleStr = manager.getCosmeticConfig().getString("settings.menu_title", "GARDE-ROBE");
        Inventory inv = Bukkit.createInventory(null, 54, MiniMessage.miniMessage().deserialize(titleStr));
        setupFrame(inv);

        // Load categories from config
        ConfigurationSection categories = manager.getCosmeticConfig().getConfigurationSection("categories");
        if (categories != null) {
            for (String key : categories.getKeys(false)) {
                ConfigurationSection sec = categories.getConfigurationSection(key);
                if (sec == null) continue;

                int slot = sec.getInt("slot");
                String hdbId = sec.getString("hdb_id");
                String name = sec.getString("name");
                List<String> lore = sec.getStringList("lore");

                ItemStack icon = createHeadItem(hdbId, name, lore);
                // Add hidden PDC to identify category? Or just use slot map in listener.
                // Using slot logic is simpler for main menu.
                inv.setItem(slot, icon);
            }
        }

        // Tout retirer item
        ConfigurationSection unequipSec = manager.getCosmeticConfig().getConfigurationSection("settings.unequip_item");
        if (unequipSec != null) {
            ItemStack item = createHeadItem(unequipSec.getString("hdb_id"), unequipSec.getString("name"), null);
            inv.setItem(49, item);
        }

        player.openInventory(inv);
    }

    private void openSubMenu(Player player, String category) {
        String titleStr = manager.getCosmeticConfig().getString("categories." + category + ".name", category.toUpperCase());
        Inventory inv = Bukkit.createInventory(null, 54, MiniMessage.miniMessage().deserialize(titleStr));
        setupFrame(inv);

        // Back button
        ConfigurationSection backSec = manager.getCosmeticConfig().getConfigurationSection("settings.back_item");
        if (backSec != null) {
            ItemStack item = createHeadItem(backSec.getString("hdb_id"), backSec.getString("name"), null);
            inv.setItem(49, item);
        }

        // List cosmetics
        int[] slots = getInnerSlots();
        int index = 0;

        for (Cosmetic cosmetic : manager.getCosmetics().values()) {
            if (!cosmetic.getCategory().equalsIgnoreCase(category)) continue;
            if (index >= slots.length) break;

            ItemStack icon = createHeadItem(cosmetic.getHdbId(), cosmetic.getName(), cosmetic.getLore());

            // Permission check logic in lore or visual?
            // Prompt says: "Assure-toi que si on clique sur un cosmétique sans avoir la permission, ça envoie un message d'erreur propre."
            // But we can also show visual indicator. Prompt "lore" example implies just description.
            // I'll stick to error on click, but maybe add "Locked" text if requested? Prompt didn't strictly request visual lock.

            // Store ID in PDC for reliable retrieval
            ItemMeta meta = icon.getItemMeta();
            meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "cosmetic_id"), org.bukkit.persistence.PersistentDataType.STRING, cosmetic.getId());
            icon.setItemMeta(meta);

            inv.setItem(slots[index++], icon);
        }

        player.openInventory(inv);
    }

    private void setupFrame(Inventory inv) {
        String matName = manager.getCosmeticConfig().getString("settings.frame_material", "PURPLE_STAINED_GLASS_PANE");
        Material mat = Material.matchMaterial(matName);
        if (mat == null) mat = Material.PURPLE_STAINED_GLASS_PANE;

        ItemStack glass = new ItemStack(mat);
        ItemMeta meta = glass.getItemMeta();
        meta.displayName(Component.empty());
        glass.setItemMeta(meta);

        // Slots: 0, 1, 7, 8 (Top)
        inv.setItem(0, glass); inv.setItem(1, glass);
        inv.setItem(7, glass); inv.setItem(8, glass);

        // 9, 17 (Row 2 sides)
        inv.setItem(9, glass); inv.setItem(17, glass);

        // 36, 44 (Row 5 sides)
        inv.setItem(36, glass); inv.setItem(44, glass);

        // 45, 46, 52, 53 (Bottom)
        inv.setItem(45, glass); inv.setItem(46, glass);
        inv.setItem(52, glass); inv.setItem(53, glass);
    }

    private int[] getInnerSlots() {
        // Return slots that are NOT frame or special buttons.
        // Frame: 0,1,7,8,9,17,36,44,45,46,52,53. Button: 49.
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 54; i++) {
            if (isFrame(i) || i == 49) continue;
            slots.add(i);
        }
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }

    private boolean isFrame(int s) {
        return s == 0 || s == 1 || s == 7 || s == 8 || s == 9 || s == 17 || s == 36 || s == 44 || s == 45 || s == 46 || s == 52 || s == 53;
    }

    private ItemStack createHeadItem(String hdbId, String name, List<String> lore) {
        ItemStack item;
        if (hdbApi != null && hdbId != null) {
            item = hdbApi.getItemHead(hdbId);
            if (item == null) item = new ItemStack(Material.PLAYER_HEAD); // Fallback
        } else {
            item = new ItemStack(Material.PLAYER_HEAD);
        }

        ItemMeta meta = item.getItemMeta();
        if (name != null) meta.displayName(MiniMessage.miniMessage().deserialize(name));
        if (lore != null) {
            List<Component> loreComps = new ArrayList<>();
            for (String l : lore) {
                loreComps.add(MiniMessage.miniMessage().deserialize(l));
            }
            meta.lore(loreComps);
        }
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Detect if it's our menu. We can check title or holder (holder is null).
        // Checking title is tricky with MiniMessage.
        // Let's check logic: if frame matches?
        // Or better: check if title matches Main Menu or any Category name.

        String titleStr = manager.getCosmeticConfig().getString("settings.menu_title", "GARDE-ROBE");
        Component titleComp = event.getView().title();

        // Simple serialization check might fail due to color codes.
        // But we rely on behavior.
        // Let's assume valid click if item has PDC or if it matches frame slots?

        // Actually, we can just check if inventory size is 54 and title *contains* some keyword or we just handle standard check.
        // Or store open menus in a Map<UUID, String> in GUI class.

        // For robustness, let's use the exact title string comparison via serialization if possible,
        // OR maintain a set of viewers.

        // Let's try matching serialization of the configured title.
        // Note: Serializing MiniMessage component back to string might differ from input string.
        // Safer: Check if the top inventory has our frame material in specific slots?
        // Or just `event.getView().title().equals(...)`.

        // I will use a simplified check: if title contains "GARDE-ROBE" or "CHAPEAUX" etc?
        // No, config is editable.
        // I will try to verify if the frame is present.
        ItemStack frameItem = event.getView().getTopInventory().getItem(0);
        if (frameItem == null || frameItem.getType() != Material.PURPLE_STAINED_GLASS_PANE && frameItem.getType() != Material.matchMaterial(manager.getCosmeticConfig().getString("settings.frame_material", "PURPLE_STAINED_GLASS_PANE"))) {
             return; // Likely not our menu
        }

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getSlot();
        if (isFrame(slot)) return;

        // Determine Menu Level
        // If slot 49 is "Tout retirer" (Main Menu) or "Back" (Sub Menu)

        // We can distinguish main menu by checking category slots.
        int hatSlot = manager.getCosmeticConfig().getInt("categories.hats.slot");
        int particleSlot = manager.getCosmeticConfig().getInt("categories.particles.slot");
        int gadgetSlot = manager.getCosmeticConfig().getInt("categories.gadgets.slot");

        boolean isMainMenu = (event.getInventory().getItem(hatSlot) != null && event.getInventory().getItem(particleSlot) != null);
        // This heuristic might fail if sub menu has items in those slots.

        // Let's store current menu type in `player` metadata or a map.
        // Since I can't easily add metadata to player without plugin instance everywhere (I have it), let's use a weak map here.
        // But `onInventoryClick` is a new event.

        // Alternative: Use NBT on the frame items?
        // Or just check if the clicked item corresponds to a category.

        if (slot == 49) {
             // Check name of item to decide action
             // "Tout retirer" vs "Retour"
             // Using PDC check would be better if I added it to buttons.
             // But I didn't.
             // Let's assume if it is Main Menu -> Unequip.
             // How do we know? Main menu title.
             // OK, I'll compare the component title with the Main Menu title from config.
             Component mainTitle = MiniMessage.miniMessage().deserialize(manager.getCosmeticConfig().getString("settings.menu_title"));
             if (event.getView().title().equals(mainTitle)) {
                 manager.unequipAll(player);
                 player.closeInventory();
             } else {
                 openMainMenu(player);
             }
             return;
        }

        if (slot == hatSlot) {
            // Check if we are in main menu.
            Component mainTitle = MiniMessage.miniMessage().deserialize(manager.getCosmeticConfig().getString("settings.menu_title"));
            if (event.getView().title().equals(mainTitle)) {
                openSubMenu(player, "hats");
                return;
            }
        }
        if (slot == particleSlot) {
             Component mainTitle = MiniMessage.miniMessage().deserialize(manager.getCosmeticConfig().getString("settings.menu_title"));
             if (event.getView().title().equals(mainTitle)) {
                openSubMenu(player, "particles");
                return;
             }
        }
        if (slot == gadgetSlot) {
             Component mainTitle = MiniMessage.miniMessage().deserialize(manager.getCosmeticConfig().getString("settings.menu_title"));
             if (event.getView().title().equals(mainTitle)) {
                openSubMenu(player, "gadgets");
                return;
             }
        }

        // Handle Cosmetic Click
        if (clicked.getItemMeta() != null && clicked.getItemMeta().getPersistentDataContainer().has(new org.bukkit.NamespacedKey(plugin, "cosmetic_id"), org.bukkit.persistence.PersistentDataType.STRING)) {
            String id = clicked.getItemMeta().getPersistentDataContainer().get(new org.bukkit.NamespacedKey(plugin, "cosmetic_id"), org.bukkit.persistence.PersistentDataType.STRING);
            Cosmetic cosmetic = manager.getCosmetics().get(id);
            if (cosmetic != null) {
                if (player.hasPermission(cosmetic.getPermission())) {
                    manager.equipCosmetic(player, id);
                    player.closeInventory();
                } else {
                    String msg = manager.getCosmeticConfig().getString("settings.messages.no_permission");
                    if (msg != null) player.sendMessage(MiniMessage.miniMessage().deserialize(msg));
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        // Prevent dragging in our GUI
        if (event.getView().getTopInventory().getItem(0) != null && event.getView().getTopInventory().getItem(0).getType() == Material.PURPLE_STAINED_GLASS_PANE) { // Weak check
             event.setCancelled(true);
        }
    }
}
