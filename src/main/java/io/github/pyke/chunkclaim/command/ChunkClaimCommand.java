package io.github.pyke.chunkclaim.command;

import dev.jorel.commandapi.CommandAPICommand;
import io.github.pyke.chunkclaim.ChunkClaim;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class ChunkClaimCommand extends CommandAPICommand {
    public ChunkClaimCommand(String commandName) {
        super(commandName);
        withPermission("chunkclaim.command");
        executesPlayer((sender, args) -> {
            Chunk chunk = sender.getLocation().getChunk();
            World world = chunk.getWorld();

            int baseX = chunk.getX() << 4;
            int baseZ = chunk.getZ() << 4;
            int y = sender.getLocation().getBlockY() - 1;

            Map<Location, BlockData> originBlocks = new HashMap<>();
            BlockData borderBlock = Bukkit.createBlockData(Material.RED_TERRACOTTA);

            for(int dx = 0; dx < 16; ++dx) {
                for(int dz = 0; dz < 16; ++dz) {
                    if (dx == 0 || dx == 15 || dz == 0 || dz == 15) {
                        Location loc = new Location(world, baseX + dx, y, baseZ + dz);
                        Block block = world.getBlockAt(loc);
                        BlockData originBlock = block.getBlockData();

                        originBlocks.put(loc, originBlock);

                        sender.sendBlockChange(loc, borderBlock);
                    }
                }
            }

            sender.sendMessage(ChunkClaim.SYSTEM_PREFIX.append(Component.text().color(TextColor.color(0xffffff))));

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Map.Entry<Location, BlockData> entry : originBlocks.entrySet()) {
                        sender.sendBlockChange(entry.getKey(), entry.getValue());
                    }
                    sender.sendMessage(ChunkClaim.SYSTEM_PREFIX.append(Component.text().color(TextColor.color(0xffffff))));
                }
            }.runTaskLater(ChunkClaim.getInstance(), 60L);
        });
    }
}
