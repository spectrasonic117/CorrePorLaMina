package com.spectrasonic.CorrePorLaMina.Utils;

import org.bukkit.Material;

import java.util.Map;

public class WoolIconUtils {
    private static final Map<Material, String> WOOL_ICONS = Map.of(
            Material.RED_WOOL, "\uE058",
            Material.ORANGE_WOOL, "\uE055",
            Material.YELLOW_WOOL, "\uE053",
            Material.LIME_WOOL, "\uE052",
            Material.PURPLE_WOOL, "\uE057",
            Material.LIGHT_BLUE_WOOL, "\uE050",
            Material.WHITE_WOOL, "\uE059",
            Material.MAGENTA_WOOL, "\uE054"
    );

    public static String getIcon(Material wool) {
        return WOOL_ICONS.getOrDefault(wool, "");
    }
}