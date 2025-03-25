package com.spectrasonic.CorrePorLaMina.Game;

import com.spectrasonic.CorrePorLaMina.Utils.MessageUtils;
import com.spectrasonic.CorrePorLaMina.Utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class GameListener implements Listener {

    private final GameManager gameManager = GameManager.getInstance();

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!gameManager.isGameActive())
            return;
        if (!(event.getEntity() instanceof Arrow arrow))
            return;
        if (!(arrow.getShooter() instanceof Player player))
            return;
        Block hitBlock = event.getHitBlock();
        if (hitBlock == null)
            return;
        Material hitType = hitBlock.getType();
        // Se verifica que el bloque impactado sea de lana y que no sea WHITE_WOOL ni
        // LIGHT_GRAY_WOOL.
        if (hitType.toString().endsWith("_WOOL") &&
                !hitType.equals(Material.WHITE_WOOL) &&
                !hitType.equals(Material.LIGHT_GRAY_WOOL)) {
            Material assigned = gameManager.getPlayerWoolAssignment().get(player.getUniqueId());
            if (assigned != null && hitType.equals(assigned)) {
                // Aumenta la velocidad del minecart del jugador.
                gameManager.incrementCartSpeed(player);
                MessageUtils.sendActionBar(player, "<green>¡Has puntuado!</green>");
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
}
