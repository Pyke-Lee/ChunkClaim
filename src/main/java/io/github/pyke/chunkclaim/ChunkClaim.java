package io.github.pyke.chunkclaim;

import io.github.pyke.chunkclaim.command.ChunkClaimCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkClaim extends JavaPlugin {
    public static final Component SYSTEM_PREFIX = Component.text("[SYSTEM] ").color(TextColor.color(0xffaa00));
    public static final Logger LOGGER = LogManager.getLogger("ChunkClaim");
    private static Plugin instance;

    @Override
    public void onEnable() {
        instance = this;

        new ChunkClaimCommand("ChunkClaim").register();

        LOGGER.info("ChunkClaim 플러그인이 활성화되었습니다!");
    }

    @Override
    public void onDisable() {
        LOGGER.info("ChunkClaim 플러그인이 비활성화되었습니다!");
    }

    public static Plugin getInstance() { return instance; }
}
