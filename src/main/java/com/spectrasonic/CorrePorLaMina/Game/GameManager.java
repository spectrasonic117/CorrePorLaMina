package com.spectrasonic.CorrePorLaMina.Game;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.spectrasonic.CorrePorLaMina.Main;
import com.spectrasonic.CorrePorLaMina.Utils.ItemBuilder;
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
    // Almacena la asignación de lana para cada jugador
    private final Map<UUID, Material> playerWoolAssignment = new HashMap<>();
    // Almacena el minecart en el que está cada jugador
    private final Map<UUID, Minecart> playerMinecart = new HashMap<>();
    private final Random random = new Random();

    private final List<Material> availableWools = Arrays.asList(
            Material.RED_WOOL,
            Material.ORANGE_WOOL,
            Material.YELLOW_WOOL,
            Material.LIME_WOOL,
            Material.PURPLE_WOOL,
            Material.LIGHT_BLUE_WOOL,
            Material.WHITE_WOOL,
            Material.MAGENTA_WOOL);
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
        // Para cada jugador online se asigna un minecart y se entregan los ítems
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Solo afectar a jugadores en modo ADVENTURE
            if (player.getGameMode() != targetGameMode) {
                continue;
            }

            if (minecartLocations.isEmpty())
                continue;
            Location spawn = minecartLocations.get(random.nextInt(minecartLocations.size()));
            Minecart cart = player.getWorld().spawn(spawn, Minecart.class);
            cart.addPassenger(player);
            playerMinecart.put(player.getUniqueId(), cart);
            // Dar arco irrompible con Infinity 1 y una flecha
            ItemStack bow = ItemBuilder.setMaterial("BOW")
                    .addEnchantment("infinity", 1)
                    .build();
            bow.getItemMeta().setUnbreakable(true);
            player.getInventory().addItem(bow);
            player.getInventory().addItem(new ItemStack(Material.ARROW, 1));
            // Asignar lana aleatoria (sin WHITE_WOOL ni LIGHT_GRAY_WOOL)
            Material assignedWool = availableWools.get(random.nextInt(availableWools.size()));
            playerWoolAssignment.put(player.getUniqueId(), assignedWool);
            player.getInventory().addItem(new ItemStack(assignedWool));
        }
        // Aquí se podría iniciar la lógica de ticks para verificar que un minecart
        // llegue al final
    }

    public void stopGame(GameMode targetGameMode) {
        gameActive = false;
        // Se remueven todos los minecarts creados y se limpian las asignaciones
        for (Minecart cart : playerMinecart.values()) {
            cart.remove();
        }

        // Limpiar inventarios de los jugadores completamente
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Solo afectar a jugadores en modo ADVENTURE
            if (player.getGameMode() != targetGameMode) {
                // Cambiar jugadores en SPECTATOR a ADVENTURE
                if (player.getGameMode() == GameMode.SPECTATOR) {
                    player.getInventory().clear();
                    player.setGameMode(GameMode.ADVENTURE);
                }
                continue;
            }

            // Limpiar completamente el inventario
            player.getInventory().clear();
        }

        playerMinecart.clear();
        playerWoolAssignment.clear();
    }

    public void incrementCartSpeed(Player player) {
        Minecart cart = playerMinecart.get(player.getUniqueId());
        if (cart != null) {
            // Obtener la velocidad actual y aumentarla en la misma dirección
            // en lugar de usar la dirección de la ubicación
            org.bukkit.util.Vector currentVelocity = cart.getVelocity();

            // Si la velocidad es muy baja, establecer una dirección base
            if (currentVelocity.lengthSquared() < 0.01) {
                // Usar la dirección en la que está mirando el jugador como base
                currentVelocity = player.getLocation().getDirection().setY(0).normalize().multiply(0.2);
            } else {
                // Normalizar la velocidad actual y aumentarla
                currentVelocity = currentVelocity.normalize().multiply(
                        currentVelocity.length() + 0.3);
            }

            cart.setVelocity(currentVelocity);
        }
    }

}
