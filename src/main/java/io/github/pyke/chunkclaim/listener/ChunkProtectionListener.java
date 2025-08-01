package io.github.pyke.chunkclaim.listener;

import io.github.pyke.chunkclaim.ChunkClaim;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

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
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) { return; }
        if (event.getClickedBlock() == null) { return; }

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        if (!isInteractiveBlock(block)) { return; }

        if (isMyClaimChunk(player, block.getChunk())) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            player.sendMessage(ChunkClaim.SYSTEM_PREFIX.append(Component.text("이 땅은 다른 플레이어가 점유했습니다.", NamedTextColor.WHITE)));
        }
    }

    public boolean isMyClaimChunk(Player player, @NotNull Chunk chunk) {
        if (chunk.getPersistentDataContainer().has(ChunkClaim.CLAIM_KEY, PersistentDataType.STRING)) {
            String owner = chunk.getPersistentDataContainer().get(ChunkClaim.CLAIM_KEY, PersistentDataType.STRING);
            return !player.getUniqueId().toString().equals(owner);
        }

        return true;
    }

    private boolean isInteractiveBlock(Block block) {
        BlockState state = block.getState();

        return state instanceof InventoryHolder
            || state instanceof Lectern
            || state instanceof Jukebox
            || state instanceof BrewingStand
            || state instanceof CommandBlock
            || state instanceof Sign
            || block.getType().name().endsWith("_DOOR")
            || block.getType().name().endsWith("_BUTTON")
            || block.getType().name().endsWith("_LEVER");
    }
}
