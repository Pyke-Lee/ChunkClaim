package io.github.pyke.chunkclaim.listener;

import io.github.pyke.chunkclaim.ChunkClaim;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ChunkProtectionListener implements Listener {
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (isMyClaimChunk(player, event.getBlock().getChunk())) {
            player.sendMessage(ChunkClaim.SYSTEM_PREFIX.append(Component.text("이 땅은 다른 플레이어가 점유했습니다.", NamedTextColor.WHITE)));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (isMyClaimChunk(player, event.getBlock().getChunk())) {
            player.sendMessage(ChunkClaim.SYSTEM_PREFIX.append(Component.text("이 땅은 다른 플레이어가 점유했습니다.", NamedTextColor.WHITE)));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (isMyClaimChunk(player, Objects.requireNonNull(event.getClickedBlock()).getChunk())) {
            player.sendMessage(ChunkClaim.SYSTEM_PREFIX.append(Component.text("이 땅은 다른 플레이어가 점유했습니다.", NamedTextColor.WHITE)));
            event.setCancelled(true);
        }
    }

    public boolean isMyClaimChunk(Player player, @NotNull Chunk chunk) {
        NamespacedKey claimKey = new NamespacedKey(ChunkClaim.getInstance(), "claim_owner");

        if (chunk.getPersistentDataContainer().has(claimKey, PersistentDataType.STRING)) {
            String owner = chunk.getPersistentDataContainer().get(claimKey, PersistentDataType.STRING);
            return !player.getUniqueId().toString().equals(owner);
        }

        return true;
    }
}
