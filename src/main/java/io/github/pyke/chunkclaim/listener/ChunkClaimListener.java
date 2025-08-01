package io.github.pyke.chunkclaim.listener;

import io.github.pyke.chunkclaim.ChunkClaim;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChunkClaimListener implements Listener {
    private final Map<UUID, Chunk> activeChunks = new HashMap<>();
    private final Map<UUID, Map<Location, BlockData>> shownBorders = new HashMap<>();
    private final BlockData borderBlock = Bukkit.createBlockData(Material.RED_TERRACOTTA);

    private final Map<UUID, Chunk> pendingClaims = new HashMap<>();
    private final Map<UUID, BukkitRunnable> claimTimers = new HashMap<>();

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) { return; }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        ItemStack item = event.getItem();
        if (null == item || !item.getType().equals(Material.PAPER)) { return; }

        ItemMeta meta = item.getItemMeta();
        if (null == meta || !meta.hasDisplayName()) { return; }
        if (!Component.text("땅 문서", NamedTextColor.WHITE).equals(meta.displayName())) { return; }

        NamespacedKey key = new NamespacedKey(ChunkClaim.getInstance(), "chunkclaim");
        if (!meta.getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN)) { return; }

        Chunk chunk = player.getLocation().getChunk();

        if (pendingClaims.containsKey(uuid)) {
            Chunk pendingChunk = pendingClaims.get(uuid);
            if (!pendingChunk.equals(chunk)) {
                player.sendMessage(ChunkClaim.SYSTEM_PREFIX.append(Component.text("다른 청크로 이동했습니다. 다시 시도하세요.", NamedTextColor.WHITE)));
                return;
            }

            if (!chunk.getPersistentDataContainer().has(ChunkClaim.CLAIM_KEY, PersistentDataType.STRING)) {
                consumeItem(player, 1);
                chunk.getPersistentDataContainer().set(ChunkClaim.CLAIM_KEY, PersistentDataType.STRING, uuid.toString());
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                player.sendMessage(ChunkClaim.SYSTEM_PREFIX.append(Component.text("땅을 점유했습니다. (" + chunk.getX() + ", " + chunk.getZ() + ") @" + chunk.getWorld().getName(), NamedTextColor.WHITE)));
            } else {
                player.sendMessage(ChunkClaim.SYSTEM_PREFIX.append(Component.text("이미 점유된 땅입니다.", NamedTextColor.WHITE)));
            }

            clearChunkBorder(player);
            pendingClaims.remove(uuid);

            if (claimTimers.containsKey(uuid)) {
                claimTimers.get(uuid).cancel();
                claimTimers.remove(uuid);
            }

            return;
        }

        if (chunk.getPersistentDataContainer().has(ChunkClaim.CLAIM_KEY, PersistentDataType.STRING)) {
            String owner = chunk.getPersistentDataContainer().get(ChunkClaim.CLAIM_KEY, PersistentDataType.STRING);
            if (uuid.toString().equals(owner)) {
                player.sendMessage(ChunkClaim.SYSTEM_PREFIX.append(Component.text("이미 당신이 점유한 땅입니다.", NamedTextColor.WHITE)));
            } else {
                player.sendMessage(ChunkClaim.SYSTEM_PREFIX.append(Component.text("이 땅은 다른 플레이어가 점유했습니다.", NamedTextColor.WHITE)));
            }
            return;
        }

        showChunkBorder(player, chunk);
        activeChunks.put(uuid, chunk);
        pendingClaims.put(uuid, chunk);

        player.sendMessage(ChunkClaim.SYSTEM_PREFIX.append(Component.text("10초 안에 다시 우클릭하면 땅을 점유합니다.", NamedTextColor.WHITE)));

        BukkitRunnable timer = new BukkitRunnable() {
            @Override
            public void run() {
                clearChunkBorder(player);
                activeChunks.remove(uuid);
                pendingClaims.remove(uuid);
                claimTimers.remove(uuid);
                player.sendMessage(ChunkClaim.SYSTEM_PREFIX.append(Component.text("땅 점유가 취소되었습니다.", NamedTextColor.WHITE)));
            }
        };
        timer.runTaskLater(ChunkClaim.getInstance(), 200L);
        claimTimers.put(uuid, timer);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!pendingClaims.containsKey(uuid)) { return; }

        Chunk prevChunk = activeChunks.get(uuid);
        Chunk currentChunk = player.getLocation().getChunk();

        if (null == prevChunk || prevChunk.equals(currentChunk)) { return; }

        clearChunkBorder(player);
        showChunkBorder(player, currentChunk);
        activeChunks.put(uuid, currentChunk);
        pendingClaims.put(uuid, currentChunk);
    }

    private void showChunkBorder(Player player, Chunk chunk) {
        World world = chunk.getWorld();
        int baseX = chunk.getX() << 4;
        int baseZ = chunk.getZ() << 4;

        Map<Location, BlockData> backup = new HashMap<>();

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                if (dx == 0 || dx == 15 || dz == 0 || dz == 15) {
                    int worldX = baseX + dx;
                    int worldZ = baseZ + dz;
                    int y = world.getHighestBlockYAt(worldX, worldZ);

                    Location loc = new Location(world, worldX, y, worldZ);
                    BlockData original = world.getBlockAt(loc).getBlockData();

                    backup.put(loc, original);
                    player.sendBlockChange(loc, borderBlock);
                }
            }
        }

        shownBorders.put(player.getUniqueId(), backup);
    }

    private void clearChunkBorder(Player player) {
        UUID uuid = player.getUniqueId();
        Map<Location, BlockData> backup = shownBorders.remove(uuid);
        if (null == backup) { return; }

        for (Map.Entry<Location, BlockData> entry : backup.entrySet()) {
            player.sendBlockChange(entry.getKey(), entry.getValue());
        }
    }

    private void consumeItem(Player player, int amount) {
        ItemStack item = player.getInventory().getItemInMainHand();
        item.subtract(amount);
    }
}