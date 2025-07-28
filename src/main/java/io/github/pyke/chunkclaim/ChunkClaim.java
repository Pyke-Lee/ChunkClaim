package io.github.pyke.chunkclaim;

import org.bukkit.plugin.java.JavaPlugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkClaim extends JavaPlugin {
    public static final Logger LOGGER = LogManager.getLogger("ChunkClaim");

    @Override
    public void onEnable() {
        LOGGER.info("ChunkClaim 플러그인 활성화!");
    }

    @Override
    public void onDisable() {
        LOGGER.info("ChunkClaim 플러그인 비활성화!");
    }
}
