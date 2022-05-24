/*
 * BlackHoleBedWars
 * Copyright (c) 2022. YumaHisai
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.yumahisai.blholebw.shop.main;

import com.yumahisai.blholebw.BedWars;
import com.yumahisai.blholebw.api.arena.shop.IBuyItem;
import com.yumahisai.blholebw.api.arena.shop.IContentTier;
import com.yumahisai.blholebw.api.configuration.ConfigPath;
import com.yumahisai.blholebw.shop.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class ContentTier implements IContentTier {

    private int value, price;
    private ItemStack itemStack;
    private Material currency;
    private List<IBuyItem> buyItemsList = new ArrayList<>();
    private boolean loaded = false;

    /**
     * Create a content tier for a category content
     */
    public ContentTier(String path, String tierName, String identifier, YamlConfiguration yml) {
        BedWars.debug("Loading content tier" + path);

        if (yml.get(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_MATERIAL) == null) {
            BedWars.plugin.getLogger().severe("tier-item material not set at " + path);
            return;
        }

        try {
            value = Integer.parseInt(tierName.replace("tier", ""));
        } catch (Exception e) {
            BedWars.plugin.getLogger().severe(path + " doesn't end with a number. It's not recognized as a tier!");
            return;
        }

        if (yml.get(path + ConfigPath.SHOP_CONTENT_TIER_SETTINGS_COST) == null) {
            BedWars.plugin.getLogger().severe("Cost not set for " + path);
            return;
        }
        price = yml.getInt(path + ConfigPath.SHOP_CONTENT_TIER_SETTINGS_COST);

        if (yml.get(path + ConfigPath.SHOP_CONTENT_TIER_SETTINGS_CURRENCY) == null) {
            BedWars.plugin.getLogger().severe("Currency not set for " + path);
            return;
        }

        if (yml.getString(path + ConfigPath.SHOP_CONTENT_TIER_SETTINGS_CURRENCY).isEmpty()) {
            BedWars.plugin.getLogger().severe("Invalid currency at " + path);
            return;
        }

        switch (yml.getString(path + ConfigPath.SHOP_CONTENT_TIER_SETTINGS_CURRENCY).toLowerCase()) {
            case "iron":
            case "gold":
            case "diamond":
            case "vault":
            case "emerald":
                currency = CategoryContent.getCurrency(yml.getString(path + ConfigPath.SHOP_CONTENT_TIER_SETTINGS_CURRENCY).toLowerCase());
                break;
            default:
                BedWars.plugin.getLogger().severe("Invalid currency at " + path);
                currency = Material.IRON_INGOT;
                break;
        }

        itemStack = BedWars.nms.createItemStack(yml.getString(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_MATERIAL),
                yml.get(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_AMOUNT) == null ? 1 : yml.getInt(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_AMOUNT),
                (short) (yml.get(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_DATA) == null ? 0 : yml.getInt(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_DATA)));


        if (yml.get(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_ENCHANTED) != null) {
            if (yml.getBoolean(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_ENCHANTED)) {
                itemStack = ShopManager.enchantItem(itemStack);
            }
        }

        // potion display color based on NBT tag
        if (yml.getString(path + ".tier-item.potion-display") != null && !yml.getString(path + ".tier-item.potion-display").isEmpty()) {
            itemStack = BedWars.nms.setTag(itemStack, "Potion", yml.getString(path + ".tier-item.potion-display"));
        }
        // 1.16+ custom color
        if (yml.getString(path + ".tier-item.potion-color") != null && !yml.getString(path + ".tier-item.potion-color").isEmpty()) {
            itemStack = BedWars.nms.setTag(itemStack, "CustomPotionColor", yml.getString(path + ".tier-item.potion-color"));
        }

        if (itemStack != null) {
            itemStack.setItemMeta(ShopManager.hideItemStuff(itemStack.getItemMeta()));
        }

        IBuyItem bi;
        if (yml.get(path + "." + ConfigPath.SHOP_CONTENT_BUY_ITEMS_PATH) != null) {
            for (String s : yml.getConfigurationSection(path + "." + ConfigPath.SHOP_CONTENT_BUY_ITEMS_PATH).getKeys(false)) {
                bi = new BuyItem(path + "." + ConfigPath.SHOP_CONTENT_BUY_ITEMS_PATH + "." + s, yml, identifier, this);
                if (bi.isLoaded()) buyItemsList.add(bi);
            }
        }
        if (yml.get(path + "." + ConfigPath.SHOP_CONTENT_BUY_CMDS_PATH) != null) {
            bi = new BuyCommand(path + "." + ConfigPath.SHOP_CONTENT_BUY_CMDS_PATH, yml, identifier);
            if (bi.isLoaded()) buyItemsList.add(bi);
        }

        if (buyItemsList.isEmpty()) {
            Bukkit.getLogger().warning("Loaded 0 buy content for: " + path);
        }

        loaded = true;
    }

    /**
     * Get tier price
     */
    public int getPrice() {
        return price;
    }

    /**
     * Get tier currency
     */
    public Material getCurrency() {
        return currency;
    }

    /**
     * Set tier currency.
     */
    public void setCurrency(Material currency) {
        this.currency = currency;
    }

    /**
     * Set tier price.
     */
    public void setPrice(int price) {
        this.price = price;
    }

    /**
     * Set tier preview item.
     */
    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * Set list of items that you receive on buy.
     */
    public void setBuyItemsList(List<IBuyItem> buyItemsList) {
        this.buyItemsList = buyItemsList;
    }

    /**
     * Get item stack with name and lore in player's language
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Get tier level
     */
    public int getValue() {
        return value;
    }

    /**
     * Check if tier is loaded
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Get items
     */
    public List<IBuyItem> getBuyItemsList() {
        return buyItemsList;
    }
}
