package com.spectrasonic.CorrePorLaMina.Game;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMovementLock implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Si el juego no está activo o el jugador no está participando, no se cancela el movimiento
        Player player = event.getPlayer();
        if (!GameManager.getInstance().isGameActive())
            return;
        if (!GameManager.getInstance().getPlayerMinecart().containsKey(player.getUniqueId()))
            return;

        // Permitir cambios únicamente en rotación: si la ubicación (x, y, z) varía se fuerza volver a la posición original,
        // conservando el yaw y pitch nuevos.
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            Location fixed = from.clone();
            fixed.setYaw(to.getYaw());
            fixed.setPitch(to.getPitch());
            event.setTo(fixed);
        }
    }
}
