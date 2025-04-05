package com.spectrasonic.CorrePorLaMina.Game;

import com.spectrasonic.CorrePorLaMina.Utils.MessageUtils;
import com.spectrasonic.CorrePorLaMina.Utils.SoundUtils;
import com.spectrasonic.CorrePorLaMina.Utils.PointsManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

public class GameListener implements Listener {

    private final GameManager gameManager = GameManager.getInstance();
    private final PointsManager pointsManager;

    public GameListener(PointsManager pointsManager) {
        this.pointsManager = pointsManager;
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!gameManager.isGameActive())
            return;
        if (!(event.getEntity() instanceof Arrow arrow))
            return;
        if (!(arrow.getShooter() instanceof Player player))
            return;

        // Cancel the event if the arrow hits a minecart in the game
        if (event.getHitEntity() instanceof Minecart minecart &&
                gameManager.getPlayerMinecart().containsValue(minecart)) {
            event.setCancelled(true);
            return;
        }

        Block hitBlock = event.getHitBlock();
        if (hitBlock == null)
            return;
        Material hitType = hitBlock.getType();

        if (hitType.toString().endsWith("_WOOL") &&
                !hitType.equals(Material.BLACK_WOOL) &&
                !hitType.equals(Material.LIGHT_GRAY_WOOL) &&
                !hitType.equals(Material.BLUE_WOOL) &&
                !hitType.equals(Material.PINK_WOOL) &&
                !hitType.equals(Material.GRAY_WOOL) &&
                !hitType.equals(Material.GREEN_WOOL) &&
                !hitType.equals(Material.BROWN_WOOL) &&
                !hitType.equals(Material.CYAN_WOOL)) {
            Material assigned = gameManager.getPlayerWoolAssignment().get(player.getUniqueId());
            if (assigned != null && hitType.equals(assigned)) {
                // incrementar velocidad del minecart

                gameManager.incrementCartSpeed(player);

                pointsManager.addPoints(player, 1);
                MessageUtils.sendActionBar(player, "<green><b>+1 Punto");
                SoundUtils.playerSound(player, org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        // Verificar si el juego está activo
        if (!gameManager.isGameActive()) {
            return;
        }

        // Verificar si el vehículo es un minecart
        if (!(event.getVehicle() instanceof Minecart)) {
            return;
        }

        // Verificar si quien sale es un jugador
        if (!(event.getExited() instanceof Player player)) {
            return;
        }

        // Verificar si el jugador tiene permiso de bypass
        if (player.hasPermission("game.bypass")) {
            return;
        }

        // Verificar si este minecart pertenece a un jugador en el juego
        if (gameManager.getPlayerMinecart().containsValue(event.getVehicle())) {
            // Cancelar el evento para evitar que el jugador se baje
            event.setCancelled(true);

            // Notificar al jugador
            MessageUtils.sendActionBar(player, "<red>¡No puedes bajarte!</red>");
        }
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        // Prevent any damage to minecarts during the game
        if (!gameManager.isGameActive()) {
            return;
        }

        if (event.getVehicle() instanceof Minecart minecart) {
            if (gameManager.getPlayerMinecart().containsValue(minecart)) {
                event.setCancelled(true);

                // If the attacker is a player, notify them
                if (event.getAttacker() instanceof Player player) {
                    MessageUtils.sendActionBar(player, "<red>No puedes romper vagonetas</red>");
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Prevent damage to minecarts from any entity
        if (!gameManager.isGameActive()) {
            return;
        }

        if (event.getEntity() instanceof Minecart minecart) {
            if (gameManager.getPlayerMinecart().containsValue(minecart)) {
                event.setCancelled(true);

                // If the damager is a player, notify them
                if (event.getDamager() instanceof Player player) {
                    MessageUtils.sendActionBar(player, "<red>¡Las vagonetas son indestructibles!</red>");
                }
                // If the damager is an arrow shot by a player
                else if (event.getDamager() instanceof Arrow arrow && arrow.getShooter() instanceof Player player) {
                    MessageUtils.sendActionBar(player, "<red>¡Las vagonetas son indestructibles!</red>");
                }
            }
        }
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        // Verificar si el juego está activo
        if (!gameManager.isGameActive()) {
            return;
        }

        // Verificar si el vehículo es un minecart
        if (!(event.getVehicle() instanceof Minecart minecart)) {
            return;
        }

        // Verificar si hay un pasajero y es un jugador
        if (minecart.getPassengers().isEmpty() || !(minecart.getPassengers().get(0) instanceof Player player)) {
            return;
        }

        // Verificar si este minecart pertenece a un jugador en el juego
        if (!gameManager.getPlayerMinecart().containsValue(minecart)) {
            return;
        }

        // Verificar si el jugador tiene permiso de bypass
        if (player.hasPermission("game.bypass")) {
            return;
        }

        // Verificar si hay un bloque de lana NEGRA debajo del minecart
        Block blockBelow = minecart.getLocation().subtract(0, 1, 0).getBlock();
        if (blockBelow.getType() == Material.BLACK_WOOL) {
            // El jugador ha llegado al final

            // Remover el minecart de la asignación del jugador
            gameManager.getPlayerMinecart().remove(player.getUniqueId());

            // Remover al jugador del minecart
            minecart.eject();

            // Eliminar el minecart
            minecart.remove();

            // Cambiar el modo de juego del jugador a espectador
            player.setGameMode(org.bukkit.GameMode.SPECTATOR);

            // Enviar mensaje al jugador
            MessageUtils.sendActionBar(player, "<green>¡Has terminado!</green>");

            // Reproducir sonido de victoria
            SoundUtils.playerSound(player, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }
}