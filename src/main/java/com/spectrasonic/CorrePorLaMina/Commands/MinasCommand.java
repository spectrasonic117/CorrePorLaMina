package com.spectrasonic.CorrePorLaMina.Commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import com.spectrasonic.CorrePorLaMina.Game.GameManager;
import com.spectrasonic.CorrePorLaMina.Utils.MessageUtils;
import lombok.NoArgsConstructor;

@CommandAlias("minas")
@CommandPermission("correporlamina.admin")
@NoArgsConstructor
public class MinasCommand extends BaseCommand {

    private final GameManager gameManager = GameManager.getInstance();

    @Subcommand("game")
    @Syntax("<start [round]|stop>")
    @CommandCompletion("start:1|2|3|stop")
    public void onGame(CommandSender sender, @Optional String action, @Optional Integer round) {
        Player player = (Player) sender;
        if (action == null) {
            MessageUtils.sendMessage(sender, "<red>Uso: /minas game <start [round]|stop></red>");
            return;
        }
        if (action.equalsIgnoreCase("start")) {
            if (gameManager.isGameActive()) {
                MessageUtils.sendMessage(sender, "<red>El juego ya está en marcha.</red>");
            } else {
                int selectedRound = (round == null || round < 1 || round > 3) ? 1 : round;
                if (round != null && (round < 1 || round > 3)) {
                    MessageUtils.sendMessage(sender, "<yellow>Ronda inválida. Iniciando ronda 1 por defecto.</yellow>");
                }
                player.performCommand("id false");
                gameManager.startGame(GameMode.ADVENTURE, selectedRound);
                MessageUtils.sendMessage(sender, "<green>Juego Iniciado (Ronda " + selectedRound + ").");
            }
        } else if (action.equalsIgnoreCase("stop")) {
            if (!gameManager.isGameActive()) {
                MessageUtils.sendMessage(sender, "<red>No hay juego en marcha.</red>");
            } else {
                player.performCommand("id true");
                gameManager.stopGame(GameMode.ADVENTURE);
                MessageUtils.sendMessage(sender, "<red>Juego Terminado.");
            }
        } else {
            MessageUtils.sendMessage(sender, "<red>Uso: /minas game <start [round]|stop></red>");
        }
    }

    @Subcommand("minecart")
    @Syntax("<add|remove>")
    @CommandCompletion("add|remove")
    public void onMinecart(Player player, @Optional String arg) {
        // Si no se proporciona argumento o es inválido, mostrar mensaje de uso
        if (arg == null || (!arg.equalsIgnoreCase("add") && !arg.equalsIgnoreCase("remove"))) {
            MessageUtils.sendMessage(player, "<red>Uso: /minas minecart <add|remove></red>");
            return;
        }

        if (arg.equalsIgnoreCase("remove")) {
            boolean removed = gameManager.removeLastMinecartLocation();
            if (removed) {
                MessageUtils.sendMessage(player, "<green>Se ha borrado la última entrada de minecart.</green>");
            } else {
                MessageUtils.sendMessage(player, "<red>No hay entradas para borrar.</red>");
            }
        } else if (arg.equalsIgnoreCase("add")) {
            gameManager.addMinecartLocation(player.getLocation());
            MessageUtils.sendMessage(player, "<green>Se ha agregado la ubicación del minecart.</green>");
        }
    }

    @Subcommand("reload")
    public void onReload(CommandSender sender) {
        gameManager.reload();
        MessageUtils.sendMessage(sender, "<green>La configuración ha sido recargada.</green>");
    }
}
