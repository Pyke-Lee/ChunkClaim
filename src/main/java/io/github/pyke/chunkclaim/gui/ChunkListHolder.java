package io.github.pyke.chunkclaim.gui;

import io.github.pyke.chunkclaim.ChunkClaim;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChunkListHolder implements InventoryHolder {
    public static final int ITEMS_PER_PAGE = 45;
    private static final ItemStack FILLER = createPillar();

    private final int page;
    private final Inventory inventory;

    public ChunkListHolder(int page) {
        this.page = page;
        this.inventory = Bukkit.createInventory(this, 54, Component.text("Claimed Chunks"));
        fillInventory();
    }

    @Override
    public @NotNull Inventory getInventory() { return inventory; }

    public int getPage() { return page; }

    public static void open(Player player, int page) {
        ChunkListHolder holder = new ChunkListHolder(page);
        player.openInventory(holder.getInventory());
    }

    private void fillInventory() {
        List<Chunk> ownedChunks = getClaimedChunks();

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, ownedChunks.size());
        for (int i = start; i < end; ++i) {
            Chunk chunk = ownedChunks.get(i);
            int slot = i - start;

            ItemStack item = new ItemStack(Material.GRASS_BLOCK);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("Chunk(" + chunk.getX() + ", " + chunk.getZ() + ") @" + chunk.getWorld().getName(), NamedTextColor.WHITE));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("MIDDLE : ", NamedTextColor.GREEN).append(Component.text("텔레포트", NamedTextColor.WHITE)));
            lore.add(Component.text("SHIFE + RIGHT : ", NamedTextColor.RED).append(Component.text("해제", NamedTextColor.WHITE)));
            meta.lore(lore);
            item.setItemMeta(meta);
            inventory.setItem(slot, item);
        }

        for (int i = 46; i <= 52; ++i) { inventory.setItem(i, FILLER); }

        inventory.setItem(45, createButton(page > 0 ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE, Component.text("이전 페이지", NamedTextColor.WHITE)));
        inventory.setItem(53, createButton((page + 1) * ITEMS_PER_PAGE < ownedChunks.size() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE, Component.text("다음 페이지", NamedTextColor.WHITE)));
    }

    public static List<Chunk> getClaimedChunks() {
        List<Chunk> list = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                String owner = chunk.getPersistentDataContainer().get(ChunkClaim.CLAIM_KEY, PersistentDataType.STRING);
                if (owner != null) { list.add(chunk); }
            }
        }
        return list;
    }

    private static ItemStack createPillar() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setHideTooltip(true);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createButton(Material material, Component displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(displayName);
        item.setItemMeta(meta);
        return item;
    }
}