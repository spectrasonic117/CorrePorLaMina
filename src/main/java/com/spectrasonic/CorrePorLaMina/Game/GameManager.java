package com.spectrasonic.CorrePorLaMina.Game;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.spectrasonic.CorrePorLaMina.Main;
import com.spectrasonic.CorrePorLaMina.Utils.ItemBuilder;
import com.spectrasonic.CorrePorLaMina.Utils.MessageUtils;
import com.spectrasonic.CorrePorLaMina.Utils.WoolIconUtils;
import org.bukkit.GameMode;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Getter
@Setter
public class GameManager {

    private static final GameManager INSTANCE = new GameManager();
    private final Main plugin = Main.getPlugin(Main.class);
    private boolean gameActive = false;
    private List<Location> minecartLocations = new ArrayList<>();
    private final Map<UUID, Material> playerWoolAssignment = new HashMap<>();
    private final Map<UUID, Minecart> playerMinecart = new HashMap<>();
    private final Map<UUID, BossBar> playerBossBar = new HashMap<>();
    private final Random random = new Random();

    private final List<Material> availableWools = Arrays.asList(
            Material.RED_WOOL,
            Material.ORANGE_WOOL,
            Material.YELLOW_WOOL,
            Material.LIME_WOOL,
            Material.PURPLE_WOOL,
            Material.LIGHT_BLUE_WOOL,
            Material.WHITE_WOOL,
            Material.MAGENTA_WOOL
    );

    private File minecartsFile;
    private FileConfiguration minecartsConfig;

    private GameManager() {
        loadMinecartsFile();
    }

    public static GameManager getInstance() {
        return INSTANCE;
    }

    public void reload() {
        plugin.reloadConfig();
        loadMinecartsFile();
    }

    private void loadMinecartsFile() {
        minecartsFile = new File(plugin.getDataFolder(), "minecarts.yml");
        if (!minecartsFile.exists()) {
            minecartsFile.getParentFile().mkdirs();
            try {
                minecartsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        minecartsConfig = YamlConfiguration.loadConfiguration(minecartsFile);
        minecartLocations.clear();
        if (minecartsConfig.contains("minecarts")) {
            List<?> list = minecartsConfig.getList("minecarts");
            if (list != null) {
                for (Object obj : list) {
                    if (obj instanceof Location loc) {
                        minecartLocations.add(loc);
                    }
                }
            }
        }
    }

    private void saveMinecartsFile() {
        minecartsConfig.set("minecarts", minecartLocations);
        try {
            minecartsConfig.save(minecartsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addMinecartLocation(Location loc) {
        minecartLocations.add(loc);
        saveMinecartsFile();
    }

    public boolean removeLastMinecartLocation() {
        if (!minecartLocations.isEmpty()) {
            minecartLocations.remove(minecartLocations.size() - 1);
            saveMinecartsFile();
            return true;
        }
        return false;
    }

    public boolean isGameActive() {
        return gameActive;
    }

    public void startGame(GameMode targetGameMode) {
        gameActive = true;
        List<Location> availableLocations = new ArrayList<>(minecartLocations);

    for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() != targetGameMode) continue;
            if (availableLocations.isEmpty()) {
            continue;
        }

        int index = random.nextInt(availableLocations.size());
        Location spawn = availableLocations.remove(index);
        Minecart cart = player.getWorld().spawn(spawn, Minecart.class);
        cart.addPassenger(player);
        playerMinecart.put(player.getUniqueId(), cart);

        ItemStack bow = ItemBuilder.setMaterial("BOW")
                .setName("<white><bold>Zapper")
                .addEnchantment("infinity", 1)
                .setUnbreakable(true)
                .setFlag("HIDE_ENCHANTS")
                .setCustomModelData(1000)
                .setFlag("HIDE_ATTRIBUTES")

                .build();
                
        player.getInventory().addItem(bow);
        player.getInventory().addItem(new ItemStack(Material.ARROW, 1));

        Material assignedWool = availableWools.get(random.nextInt(availableWools.size()));
        playerWoolAssignment.put(player.getUniqueId(), assignedWool);
        player.getInventory().addItem(new ItemStack(assignedWool));

        Bukkit.getLogger().info("Asignando lana " + assignedWool + " a " + player.getName());
        showWoolTitleForPlayer(player, assignedWool);

            showWoolBossBarForPlayer(player, assignedWool);
    }
}

    public void stopGame(GameMode targetGameMode) {
        gameActive = false;
        for (Minecart cart : playerMinecart.values()) cart.remove();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() != targetGameMode) {
                if (player.getGameMode() == GameMode.SPECTATOR) {
                    player.getInventory().clear();
                    player.setGameMode(GameMode.ADVENTURE);
                }
                removeWoolBossBar(player);
                continue;
            }
            player.getInventory().clear();
            removeWoolBossBar(player);
        }

        playerMinecart.clear();
        playerWoolAssignment.clear();
        playerBossBar.clear();
    }

    public void incrementCartSpeed(Player player) {
        Minecart cart = playerMinecart.get(player.getUniqueId());
        if (cart != null) {
            org.bukkit.util.Vector currentVelocity = cart.getVelocity();
            if (currentVelocity.lengthSquared() < 0.01) {
                currentVelocity = player.getLocation().getDirection().setY(0).normalize().multiply(0.2);
            } else {
                currentVelocity = currentVelocity.normalize()
                        .multiply(currentVelocity.length() + 0.3);
            }
            cart.setVelocity(currentVelocity);
        }
    }

    public void showWoolTitleForPlayer(Player player, Material wool) {
        String title;
        switch (wool) {
            case RED_WOOL:        title = "<red>Lana Roja"; break;
            case ORANGE_WOOL:     title = "<#ff7300>Lana Naranja"; break;
            case YELLOW_WOOL:     title = "<yellow>Lana Amarilla"; break;
            case LIME_WOOL:       title = "<green>Lana Verde"; break;
            case PURPLE_WOOL:     title = "<dark_purple>Lana Purpura"; break;
            case LIGHT_BLUE_WOOL: title = "<aqua>Lana Azul Claro"; break;
            case WHITE_WOOL:      title = "<white>Lana Blanca"; break;
            case MAGENTA_WOOL:    title = "<light_purple>Lana Magenta"; break;
            default: return;
        }
        MessageUtils.sendTitle(player, title, "", 1, 2, 1);
    }

    public void showWoolBossBarForPlayer(Player player, Material wool) {
        String icon = WoolIconUtils.getIcon(wool);
        if (icon.isEmpty()) return;

        BossBar bossBar = playerBossBar.get(player.getUniqueId());
        if (bossBar == null) {
            bossBar = Bukkit.createBossBar(icon, BarColor.WHITE, BarStyle.SOLID);
            bossBar.addPlayer(player);
            playerBossBar.put(player.getUniqueId(), bossBar);
        } else {
            bossBar.setTitle(icon);
}

        bossBar.setColor(getBarColorFromWool(wool));
        bossBar.setProgress(1.0);
        bossBar.setVisible(true);
    }

    public void removeWoolBossBar(Player player) {
        BossBar bossBar = playerBossBar.remove(player.getUniqueId());
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }

    public void onPlayerFinish(Player player) {
        removeWoolBossBar(player);
    }

    private BarColor getBarColorFromWool(Material wool) {
        return switch (wool) {
            case RED_WOOL -> BarColor.WHITE;
            case ORANGE_WOOL, YELLOW_WOOL -> BarColor.WHITE;
            case LIME_WOOL -> BarColor.WHITE;
            case PURPLE_WOOL, MAGENTA_WOOL -> BarColor.WHITE;
            case LIGHT_BLUE_WOOL -> BarColor.WHITE;
            case WHITE_WOOL -> BarColor.WHITE;
            default -> BarColor.WHITE;
        };
    }
}
