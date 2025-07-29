package io.github.pyke.chunkclaim.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class ChunkListHolder implements InventoryHolder {
    private final int page;

    public ChunkListHolder(int page) {
        this.page = page;
    }

    public int getPage() { return page; }

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }
}
