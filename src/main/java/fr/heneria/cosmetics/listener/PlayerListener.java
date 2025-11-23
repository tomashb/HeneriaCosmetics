package fr.heneria.cosmetics.listener;

import fr.heneria.cosmetics.HeneriaCosmetics;
import fr.heneria.cosmetics.manager.CosmeticManager;
import fr.heneria.cosmetics.model.ActiveCosmetic;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    private final HeneriaCosmetics plugin;
    private final CosmeticManager manager;

    public PlayerListener(HeneriaCosmetics plugin) {
        this.plugin = plugin;
        this.manager = plugin.getCosmeticManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ActiveCosmetic active = manager.getActiveCosmetic(player.getUniqueId());
        if (active != null && active.hasHat()) {
            // Check if the slot clicked is the helmet slot (39 in player inventory)
            if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
                 // The armor slots are usually index 36, 37, 38, 39. Helmet is 39.
                 // But wait, InventoryClickEvent slot indices depend on the view.
                 // If it is PlayerInventory, slot 39 is helmet.
                 // Let's check safely.
                 if (event.getSlot() == 39) { // Helmet slot
                     event.setCancelled(true);
                     player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize("<red>Vous ne pouvez pas retirer votre chapeau cosmétique manuellement. Utilisez /cosmetics."));
                 }
            }
            // Also check shift clicks into armor slot?
            if (event.isShiftClick()) {
                // If shift clicking an item that COULD go into helmet slot, we should block if helmet slot is occupied by cosmetic.
                // But if it is occupied, it won't go in.
                // If we are shift clicking the cosmetic OUT of the helmet slot?
                if (event.getSlot() == 39) {
                     event.setCancelled(true);
                     player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize("<red>Vous ne pouvez pas retirer votre chapeau cosmétique manuellement. Utilisez /cosmetics."));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        ActiveCosmetic active = manager.getActiveCosmetic(player.getUniqueId());

        if (active != null && active.hasHat()) {
            if (event.getKeepInventory()) {
                manager.unequipHat(player);
            } else {
                // Remove cosmetic from drops
                event.getDrops().removeIf(item -> {
                    if (item == null || item.getItemMeta() == null) return false;
                    return item.getItemMeta().getPersistentDataContainer().has(new org.bukkit.NamespacedKey(plugin, "cosmetic_hat"), org.bukkit.persistence.PersistentDataType.STRING);
                });

                // Add real helmet to drops
                if (active.getRealHelmet() != null) {
                    event.getDrops().add(active.getRealHelmet());
                }

                // Clear active cosmetic state but don't try to restore inventory (it's already dropped)
                // We just need to reset the manager's state for this player so they don't "have" a hat anymore.
                // But unequipHat tries to set inventory.
                // We should manually reset state.
                active.setCurrentHatId(null);
                active.setRealHelmet(null);
            }
        }
    }
}
