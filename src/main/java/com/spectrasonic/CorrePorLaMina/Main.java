package com.spectrasonic.CorrePorLaMina;

import co.aikar.commands.PaperCommandManager;
import com.spectrasonic.CorrePorLaMina.Commands.MinasCommand;
import com.spectrasonic.CorrePorLaMina.Utils.PointsManager;
import com.spectrasonic.CorrePorLaMina.Game.GameListener;
import com.spectrasonic.CorrePorLaMina.Game.PlayerMovementLock;
import com.spectrasonic.CorrePorLaMina.Utils.MessageUtils;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private PaperCommandManager commandManager;
    private PointsManager pointsManager;

    @Override
    public void onEnable() {
        pointsManager = new PointsManager(this);

        registerCommands();
        registerEvents();
        MessageUtils.sendStartupMessage(this);

    }

    @Override
    public void onDisable() {
        MessageUtils.sendShutdownMessage(this);
    }

    public void registerCommands() {
        commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new MinasCommand());
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new GameListener(pointsManager), this);
        getServer().getPluginManager().registerEvents(new PlayerMovementLock(), this);

    }

    public static Main getInstance() {
        return JavaPlugin.getPlugin(Main.class);
    }
}
