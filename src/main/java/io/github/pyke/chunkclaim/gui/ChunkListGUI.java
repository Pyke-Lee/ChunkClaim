package io.github.pyke.chunkclaim.gui;

import io.github.pyke.chunkclaim.ChunkClaim;
import io.github.pyke.chunkclaim.command.ChunkAdminCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChunkListGUI implements Listener {
    private static final int ITEMS_PER_PAGE = 45;
    private static final NamespacedKey CLAIM_KEY = new NamespacedKey(ChunkClaim.getInstance(), "claim_owner");
    private static final ItemStack FILLER = createPillar();

    public static void open(Player player, int page) {
        List<Chunk> ownedChunks = getClaimedChunks();

        Inventory inventory = Bukkit.createInventory(new ChunkListHolder(page), 54, Component.text("Claimed Chunks"));

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, ownedChunks.size());
        for(int i = start; i < end; ++i) {
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

        for(int i = 46; i <= 52; ++i) { inventory.setItem(i, FILLER); }

        if (page > 0) { inventory.setItem(45, createButton(Material.LIME_STAINED_GLASS_PANE, Component.text("이전 페이지", NamedTextColor.WHITE))); }
        else { inventory.setItem(45, createButton(Material.RED_STAINED_GLASS_PANE, Component.text("이전 페이지", NamedTextColor.WHITE))); }

        if ((page + 1) * ITEMS_PER_PAGE < ownedChunks.size()) { inventory.setItem(53, createButton(Material.LIME_STAINED_GLASS_PANE, Component.text("다음 페이지", NamedTextColor.WHITE))); }
        else { inventory.setItem(53, createButton(Material.RED_STAINED_GLASS_PANE, Component.text("다음 페이지", NamedTextColor.WHITE))); }

        player.openInventory(inventory);
    }

    public static List<Chunk> getClaimedChunks() {
        List<Chunk> list = new ArrayList<>();
        for(World world : Bukkit.getWorlds()) {
            for(Chunk chunk : world.getLoadedChunks()) {
                String owner = chunk.getPersistentDataContainer().get(CLAIM_KEY, PersistentDataType.STRING);
                if(null != owner) { list.add(chunk); }
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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) { return; }
        if (!(event.getInventory().getHolder() instanceof ChunkListHolder holder)) { return; }

        event.setCancelled(true);
        int slot = event.getRawSlot();
        int currentPage = holder.getPage();

        if (slot >= 0 && slot < 44) {
            switch(event.getClick()) {
                case MIDDLE: {
                    ItemStack item = event.getCurrentItem();
                    ItemMeta meta = Objects.requireNonNull(item).getItemMeta();
                    String displayName = PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(meta.displayName()));

                    Pattern pattern = Pattern.compile("Chunk\\((-?\\d+), (-?\\d+)\\) @(.+)");
                    Matcher matcher = pattern.matcher(displayName);

                    if (matcher.matches()) {
                        int chunkX = Integer.parseInt(matcher.group(1));
                        int chunkZ = Integer.parseInt(matcher.group(2));
                        World world = Bukkit.getWorld(matcher.group(3));

                        ChunkAdminCommand.gotoChunk(player, chunkX, chunkZ, Objects.requireNonNull(world));
                    }

                    break;
                }

                case SHIFT_RIGHT: {
                    ItemStack item = event.getCurrentItem();
                    ItemMeta meta = Objects.requireNonNull(item).getItemMeta();
                    String displayName = PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(meta.displayName()));

                    Pattern pattern = Pattern.compile("Chunk\\((-?\\d+), (-?\\d+)\\) @(.+)");
                    Matcher matcher = pattern.matcher(displayName);

                    if (matcher.matches()) {
                        int chunkX = Integer.parseInt(matcher.group(1));
                        int chunkZ = Integer.parseInt(matcher.group(2));
                        World world = Bukkit.getWorld(matcher.group(3));

                        if (ChunkAdminCommand.unclaimChunk(player, chunkX, chunkZ, Objects.requireNonNull(world))) {
                            player.sendMessage(ChunkClaim.SYSTEM_PREFIX.append(Component.text("관리자가 땅을 강제집행하였습니다. (관리자: " + player.getName() + ")", NamedTextColor.WHITE)));
                            open(player, currentPage);
                        }
                    }
                    break;
                }

                default:
                    break;
            }
        }
        else if (slot == 45 && currentPage > 0) { open(player, currentPage - 1); }
        else if (slot == 53) {
            List<Chunk> chunks = getClaimedChunks();
            int totalPages = (int) Math.ceil((double) chunks.size() / (double) ITEMS_PER_PAGE);
            if (currentPage + 1 < totalPages) { open(player, currentPage + 1); }
        }
    }
}
