package fr.heneria.cosmetics.gui;

import fr.heneria.cosmetics.HeneriaCosmetics;
import fr.heneria.cosmetics.manager.CosmeticManager;
import fr.heneria.cosmetics.model.Hat;
import fr.heneria.cosmetics.model.ParticleEffect;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
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
        if (Bukkit.getPluginManager().getPlugin("HeadDatabase") != null) {
            this.hdbApi = new HeadDatabaseAPI();
        } else {
            this.hdbApi = null;
        }
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text("Cosmétiques"));
        setupFrame(inv);

        // Slot 20: Chapeaux
        inv.setItem(20, createItem(Material.DIAMOND_HELMET, "&bChapeaux", "&7Cliquez pour voir les chapeaux."));

        // Slot 24: Particules
        inv.setItem(24, createItem(Material.BLAZE_POWDER, "&6Particules", "&7Cliquez pour voir les particules."));

        // Slot 49: Tout retirer
        inv.setItem(49, createItem(Material.BARRIER, "&cTout retirer", "&7Cliquez pour retirer tous vos cosmétiques."));

        player.openInventory(inv);
    }

    private void openHatsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text("Chapeaux"));
        setupFrame(inv);

        // Back button at 49 or somewhere? Usually 49 is close or back.
        inv.setItem(49, createItem(Material.ARROW, "&cRetour", "&7Retour au menu principal"));

        int[] slots = getInnerSlots();
        int index = 0;

        for (Hat hat : manager.getHats().values()) {
            if (index >= slots.length) break;

            ItemStack icon;
            if (hat.getHdbId() != null && hdbApi != null) {
                icon = hdbApi.getItemHead(hat.getHdbId());
            } else if (hat.getMaterial() != null) {
                icon = new ItemStack(hat.getMaterial());
            } else {
                icon = new ItemStack(Material.PAPER);
            }

            ItemMeta meta = icon.getItemMeta();
            meta.displayName(legacyToComponent(hat.getName()));
            List<Component> lore = new ArrayList<>();
            if (hat.getLore() != null) {
                for (String l : hat.getLore()) {
                    lore.add(legacyToComponent(l));
                }
            }

            // Check permission
            if (!player.hasPermission(hat.getPermission())) {
                lore.add(Component.empty());
                lore.add(legacyToComponent("&cVerrouillé"));
            } else {
                lore.add(Component.empty());
                lore.add(legacyToComponent("&eCliquez pour équiper"));
            }

            meta.lore(lore);
            icon.setItemMeta(meta);

            inv.setItem(slots[index++], icon);
        }

        player.openInventory(inv);
    }

    private void openParticlesMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text("Particules"));
        setupFrame(inv);

        inv.setItem(49, createItem(Material.ARROW, "&cRetour", "&7Retour au menu principal"));

        int[] slots = getInnerSlots();
        int index = 0;

        for (ParticleEffect particle : manager.getParticles().values()) {
            if (index >= slots.length) break;

            ItemStack icon = new ItemStack(particle.getIcon());
            ItemMeta meta = icon.getItemMeta();
            meta.displayName(legacyToComponent(particle.getName()));

             List<Component> lore = new ArrayList<>();
             if (particle.getLore() != null) {
                 for (String l : particle.getLore()) {
                     lore.add(legacyToComponent(l));
                 }
             }

            if (!player.hasPermission(particle.getPermission())) {
                lore.add(Component.empty());
                lore.add(legacyToComponent("&cVerrouillé"));
            } else {
                lore.add(Component.empty());
                lore.add(legacyToComponent("&eCliquez pour équiper"));
            }
            meta.lore(lore);
            icon.setItemMeta(meta);

            inv.setItem(slots[index++], icon);
        }

        player.openInventory(inv);
    }

    private void setupFrame(Inventory inv) {
        ItemStack glass = createItem(Material.PURPLE_STAINED_GLASS_PANE, " ", null);

        // 0, 1, 7, 8
        inv.setItem(0, glass);
        inv.setItem(1, glass);
        inv.setItem(7, glass);
        inv.setItem(8, glass);

        // Bottom row (45-53) except 49 potentially? The prompt says "Remplis les slots 0,1,7,8 et le bas"
        for (int i = 45; i < 54; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, glass);
            }
        }
    }

    private int[] getInnerSlots() {
        // Simple 54 slots logic, excluding borders 0,1,7,8 and bottom 45-53.
        // And maybe avoid sides? "Design Heneria" usually implies a specific layout but prompt just says "Design des Menus Heneria (le cadre orange)" but "pour que ce soit cohérent".
        // BUT prompt says: "Remplis les slots 0,1,7,8 et le bas avec des vitres violettes".
        // It doesn't mention sides (9, 17, 18, 26, 27, 35, 36, 44).
        // I'll fill all empty slots or just returns available slots.
        // For simplicity I'll return indices from 9 to 44.
        int[] slots = new int[36];
        for (int i = 0; i < 36; i++) {
            slots[i] = i + 9;
        }
        return slots;
    }

    private ItemStack createItem(Material material, String name, String loreLine) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(legacyToComponent(name));
            if (loreLine != null) {
                List<Component> lore = new ArrayList<>();
                lore.add(legacyToComponent(loreLine));
                meta.lore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private Component legacyToComponent(String text) {
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Component titleComp = event.getView().title();
        String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(titleComp);

        // Check if it is our menu
        if (!title.equals("Cosmétiques") && !title.equals("Chapeaux") && !title.equals("Particules")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || clicked.getType() == Material.PURPLE_STAINED_GLASS_PANE) return;

        int slot = event.getSlot();

        if (title.equals("Cosmétiques")) {
            if (slot == 20) {
                openHatsMenu(player);
            } else if (slot == 24) {
                openParticlesMenu(player);
            } else if (slot == 49) {
                manager.unequipAll(player);
                player.closeInventory();
            }
        } else if (title.equals("Chapeaux")) {
            if (slot == 49) {
                openMainMenu(player);
                return;
            }
            // Find which hat was clicked.
            // We can match by display name or keep a map. iterating is fine for small list.
            for (Hat hat : manager.getHats().values()) {
                String hatName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(legacyToComponent(hat.getName()));
                String clickedName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(clicked.getItemMeta().displayName());

                if (hatName.equals(clickedName)) {
                    if (player.hasPermission(hat.getPermission())) {
                        manager.equipHat(player, hat.getId());
                        player.closeInventory();
                    } else {
                        String msg = manager.getCosmeticConfig().getString("messages.no_permission");
                        if (msg != null) player.sendMessage(MiniMessage.miniMessage().deserialize(msg));
                    }
                    return;
                }
            }
        } else if (title.equals("Particules")) {
            if (slot == 49) {
                openMainMenu(player);
                return;
            }
            for (ParticleEffect particle : manager.getParticles().values()) {
                String pName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(legacyToComponent(particle.getName()));
                String clickedName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(clicked.getItemMeta().displayName());

                if (pName.equals(clickedName)) {
                     if (player.hasPermission(particle.getPermission())) {
                        manager.equipParticle(player, particle.getId());
                        player.closeInventory();
                    } else {
                        String msg = manager.getCosmeticConfig().getString("messages.no_permission");
                         if (msg != null) player.sendMessage(MiniMessage.miniMessage().deserialize(msg));
                    }
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Component titleComp = event.getView().title();
        String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(titleComp);
        if (title.equals("Cosmétiques") || title.equals("Chapeaux") || title.equals("Particules")) {
            event.setCancelled(true);
        }
    }
}
