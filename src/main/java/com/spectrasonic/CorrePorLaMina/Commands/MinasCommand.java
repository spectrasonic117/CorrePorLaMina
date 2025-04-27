package com.spectrasonic.CorrePorLaMina.Commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import com.spectrasonic.CorrePorLaMina.Game.GameManager;
import com.spectrasonic.CorrePorLaMina.Utils.MessageUtils;
import lombok.NoArgsConstructor;

@CommandAlias("minas")
@CommandPermission("correporlamina.admin")
@NoArgsConstructor // Assuming you need this based on original code
public class MinasCommand extends BaseCommand {

    // Assuming GameManager is a Singleton or has a static getInstance()
    private final GameManager gameManager = GameManager.getInstance();

    // Removed the old onGame method

    // New method for starting the game
    @Subcommand("game start")
    @Syntax("[round]") // Optional round argument
    @CommandCompletion("1|2|3") // Provide completions for the round
    @Description("Starts the CorrePorLaMina game for a specific round (1-3). Defaults to round 1.")
    public void onGameStart(CommandSender sender, @Optional Integer round) {
        // It's generally safer to check if the sender is a Player if you need player-specific actions
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser ejecutado por un jugador.</red>");
            return;
        }
        Player player = (Player) sender;

        if (gameManager.isGameActive()) {
            MessageUtils.sendMessage(sender, "<red>El juego ya está en marcha.</red>");
        } else {
            int selectedRound = (round == null || round < 1 || round > 3) ? 1 : round;
            if (round != null && (round < 1 || round > 3)) {
                MessageUtils.sendMessage(sender, "<yellow>Ronda inválida (" + round + "). Iniciando ronda 1 por defecto.</yellow>");
            }
            // Consider if this command needs to be run async or handled differently
            player.performCommand("id false");
            // Assuming GameMode.ADVENTURE is the intended mode for starting
            gameManager.startGame(GameMode.ADVENTURE, selectedRound);
            MessageUtils.sendMessage(sender, "<green>Juego Iniciado (Ronda " + selectedRound + ").");
        }
    }

    // New method for stopping the game
    @Subcommand("game stop")
    @Syntax("") // No arguments needed
    @Description("Stops the currently running CorrePorLaMina game.")
    public void onGameStop(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser ejecutado por un jugador.</red>");
            return;
        }
        Player player = (Player) sender;

        if (!gameManager.isGameActive()) {
            MessageUtils.sendMessage(sender, "<red>No hay juego en marcha.</red>");
        } else {
            // Consider if this command needs to be run async or handled differently
            player.performCommand("id true");
            // Assuming GameMode.ADVENTURE is the intended mode for stopping, adjust if needed
            gameManager.stopGame(GameMode.ADVENTURE);
            MessageUtils.sendMessage(sender, "<red>Juego Terminado.");
        }
    }

    // Removed the old onMinecart method

    // Separated minecart add command
    @Subcommand("minecart add")
    @Syntax("") // No arguments needed, uses player's location
    @Description("Adds the player's current location as a minecart spawn point.")
    public void onMinecartAdd(Player player) { // Directly accept Player
        gameManager.addMinecartLocation(player.getLocation());
        MessageUtils.sendMessage(player, "<green>Se ha agregado la ubicación del minecart.</green>");
    }

    // Separated minecart remove command
    @Subcommand("minecart remove")
    @Syntax("") // No arguments needed
    @Description("Removes the last added minecart spawn point.")
    public void onMinecartRemove(Player player) { // Accept Player for sending feedback
        boolean removed = gameManager.removeLastMinecartLocation();
        if (removed) {
            MessageUtils.sendMessage(player, "<green>Se ha borrado la última entrada de minecart.</green>");
        } else {
            MessageUtils.sendMessage(player, "<red>No hay entradas para borrar.</red>");
        }
    }

    @Subcommand("reload")
    @Syntax("") // No arguments needed
    @Description("Reloads the CorrePorLaMina configuration.")
    public void onReload(CommandSender sender) {
        gameManager.reload();
        MessageUtils.sendMessage(sender, "<green>La configuración ha sido recargada.</green>");
    }
}