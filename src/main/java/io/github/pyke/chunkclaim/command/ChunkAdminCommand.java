package io.github.pyke.chunkclaim.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.WorldArgument;
import io.github.pyke.chunkclaim.ChunkClaim;
import io.github.pyke.chunkclaim.gui.ChunkListGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class ChunkAdminCommand{
    private static final NamespacedKey CLAIM_KEY = new NamespacedKey(ChunkClaim.getInstance(), "claim_owner");

    public static void register() {
        new CommandAPICommand("chunk")
                .withSubcommand(
                        new CommandAPICommand("force_claim")
                                .withPermission("chunk.force")
                                .withOptionalArguments(new PlayerArgument("target"))
                                .executesPlayer((sender, args) -> {
                                    Player target = (Player) args.getOrDefault("target", sender);
                                    if (null == target) { return; }

                                    Chunk chunk = target.getLocation().getChunk();
                                    chunk.getPersistentDataContainer().set(CLAIM_KEY, PersistentDataType.STRING, target.getUniqueId().toString());
                                    Bukkit.broadcast(ChunkClaim.SYSTEM_PREFIX.append(Component.text("관리자가 땅을 강제집행하였습니다. (관리자: " + sender.getName() + ") (" + chunk.getX() + ", " + chunk.getZ() + ") @" + chunk.getWorld().getName(), NamedTextColor.WHITE)));
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("force_unclaim")
                                .withPermission("chunk.force")
                                .executesPlayer((sender, args) -> {
                                    int chunkX = sender.getLocation().getChunk().getX();
                                    int chunkZ = sender.getLocation().getChunk().getZ();
                                    World world = sender.getWorld();

                                    if (unclaimChunk(sender, chunkX, chunkZ, world)) { Bukkit.broadcast(ChunkClaim.SYSTEM_PREFIX.append(Component.text("관리자가 땅을 강제집행하였습니다. (관리자: " + sender.getName() + ") (" + chunkX + ", " + chunkZ + ") @" + world.getName(), NamedTextColor.WHITE))); }
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("list")
                                .withPermission("chunk.list")
                                .executesPlayer((sender, args) -> {
                                    ChunkListGUI.open(sender, 0);
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("info")
                                .withPermission("chunk.info")
                                .executesPlayer((sender, args) -> {
                                    Chunk chunk = sender.getLocation().getChunk();
                                    String owner = chunk.getPersistentDataContainer().get(CLAIM_KEY, PersistentDataType.STRING);
                                    if (null == owner) {
                                        sender.sendMessage(ChunkClaim.SYSTEM_PREFIX.append(Component.text("현재 땅에는 주인이 없습니다. (" + chunk.getX() + ", " + chunk.getZ() + ") @" + chunk.getWorld().getName(), NamedTextColor.WHITE)));
                                    }
                                    else {
                                        OfflinePlayer ownerPlayer = Bukkit.getOfflinePlayer(UUID.fromString(owner));
                                        sender.sendMessage(ChunkClaim.SYSTEM_PREFIX.append(Component.text("땅 소유자: " + ownerPlayer.getName(), NamedTextColor.WHITE)));
                                    }
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("goto")
                                .withPermission("chunk.goto")
                                .withArguments(new IntegerArgument("x"))
                                .withArguments(new IntegerArgument("z"))
                                .withOptionalArguments(new WorldArgument("world"))
                                .executesPlayer((sender, args) -> {
                                    int chunkX = (int) args.get("x");
                                    int chunkZ = (int) args.get("z");

                                    World world = (World) args.getOrDefault("world", sender.getWorld());

                                    gotoChunk(sender, chunkX, chunkZ, world);
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("item")
                                .withPermission("chunk.item")
                                .withOptionalArguments(new PlayerArgument("target"))
                                .executesPlayer((sender, args) -> {
                                    Player target = (Player) args.getOrDefault("target", sender);
                                    if (null == target) { return; }
                                    
                                    getChuckClaimItem(target);
                                })
                )
                .register();
    }

    public static void gotoChunk(Player player, int chunkX, int chunkZ, World world) {
        int blockX = (chunkX << 4) + 8;
        int blockZ = (chunkZ << 4) + 8;
        int blockY = world.getHighestBlockYAt(blockX, blockZ);

        Location targetLocation = new Location(world, blockX + 0.5, blockY + 1, blockZ + 0.5);
        player.teleport(targetLocation);
        player.sendMessage(ChunkClaim.SYSTEM_PREFIX.append(Component.text("땅으로 이동했습니다." + chunkX + ", " + chunkZ + ") @" + world.getName(), NamedTextColor.WHITE)));
    }

    public static boolean unclaimChunk(Player player, int chunkX, int chunkZ, World world) {
        Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        NamespacedKey claimKey = new NamespacedKey(ChunkClaim.getInstance(), "claim_owner");

        if (chunk.getPersistentDataContainer().has(claimKey, PersistentDataType.STRING)) {
            chunk.getPersistentDataContainer().remove(claimKey);
            player.sendMessage(ChunkClaim.SYSTEM_PREFIX.append(Component.text("땅의 점유를 해제하였습니다. (" + chunkX + ", " + chunkZ + ") @" + world.getName(), NamedTextColor.WHITE)));
            return true;
        }
        else {
            player.sendMessage(ChunkClaim.SYSTEM_PREFIX.append(Component.text("이 땅은 점유되지 않았습니다. (" + chunkX + ", " + chunkZ + ") @" + world.getName(), NamedTextColor.WHITE)));
            return false;
        }
    }

    private static void getChuckClaimItem(Player player) {
        NamespacedKey chunkclaimKey = new NamespacedKey(ChunkClaim.getInstance(), "chunkclaim");

        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("땅 문서", NamedTextColor.WHITE));
        meta.getPersistentDataContainer().set(chunkclaimKey, PersistentDataType.STRING, "true");
        item.setItemMeta(meta);

        player.getInventory().addItem(item);
        player.sendMessage(ChunkClaim.SYSTEM_PREFIX.append(Component.text("땅 문서가 지급되었습니다.", NamedTextColor.WHITE)));
    }
}
