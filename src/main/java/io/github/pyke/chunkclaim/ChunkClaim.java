package io.github.pyke.chunkclaim;

import io.github.pyke.chunkclaim.command.ChunkAdminCommand;
import io.github.pyke.chunkclaim.listener.ChunkListListener;
import io.github.pyke.chunkclaim.listener.ChunkClaimListener;
import io.github.pyke.chunkclaim.listener.ChunkProtectionListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkClaim extends JavaPlugin {
    public static final Component SYSTEM_PREFIX = Component.text("[SYSTEM] ").color(TextColor.color(0xffaa00));
    public static final Logger LOGGER = LogManager.getLogger("ChunkClaim");
    public static NamespacedKey CLAIM_KEY = null;
    private static Plugin instance;

    @Override
    public void onEnable() {
        instance = this;
        CLAIM_KEY = new NamespacedKey(this, "claim_owner");

        ChunkAdminCommand.register();

        getServer().getPluginManager().registerEvents(new ChunkClaimListener(), this);
        getServer().getPluginManager().registerEvents(new ChunkProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new ChunkListListener(), this);

        LOGGER.info("ChunkClaim 플러그인이 활성화되었습니다!");
    }

    @Override
    public void onDisable() {
        LOGGER.info("ChunkClaim 플러그인이 비활성화되었습니다!");
    }

    public static Plugin getInstance() { return instance; }
}
