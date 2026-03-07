package com.ilyrac.loadstone.loader;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum LoaderTier{
    IRON(0, Items.IRON_INGOT),
    DIAMOND(1, Items.DIAMOND),
    NETHERITE(2, Items.NETHERITE_INGOT);

    private final int radius;
    private final Item activatorItem;

    LoaderTier(int radius, Item activatorItem) {
        this.radius = radius;
        this.activatorItem = activatorItem;
    }

    public int getRadius() { return radius; }

    public Item getActivatorItem() { return activatorItem; }

    public static LoaderTier fromItem(Item item) {
        for (LoaderTier tier : values()) {
            if (tier.activatorItem == item) return tier;
        }
        return null;
    }
}