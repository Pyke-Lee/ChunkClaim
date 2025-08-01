package io.github.pyke.chunkclaim.listener;

import io.github.pyke.chunkclaim.ChunkClaim;
import io.github.pyke.chunkclaim.command.ChunkAdminCommand;
import io.github.pyke.chunkclaim.gui.ChunkListHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChunkListListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) { return; }
        if (!(event.getInventory().getHolder() instanceof ChunkListHolder holder)) { return; }

        event.setCancelled(true);
        int slot = event.getRawSlot();
        int currentPage = holder.getPage();

        if (slot >= 0 && slot <= 44) {
            switch (event.getClick()) {
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
                            ChunkListHolder.open(player, currentPage);
                        }
                    }
                    break;
                }

                default:
                    break;
            }
        } else if (slot == 45 && currentPage > 0) {
            ChunkListHolder.open(player, currentPage - 1);
        } else if (slot == 53) {
            List<Chunk> chunks = ChunkListHolder.getClaimedChunks();
            int totalPages = (int) Math.ceil((double) chunks.size() / (double) ChunkListHolder.ITEMS_PER_PAGE);
            if (currentPage + 1 < totalPages) {
                ChunkListHolder.open(player, currentPage + 1);
            }
        }
    }
}
