package de.varilx.discordIntegration.listener;

import de.varilx.BaseAPI;
import de.varilx.configuration.VaxConfiguration;
import de.varilx.configuration.file.YamlConfiguration;
import de.varilx.discordIntegration.discord.DiscordHandler;
import de.varilx.utils.language.LanguageUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;

public class MinecraftListener implements Listener {


    private final DiscordHandler manager;

    public MinecraftListener(DiscordHandler manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncChat(AsyncChatEvent event) {
        if (!LanguageUtils.getMessageString("chatbridge.ingame-message.enabled").equalsIgnoreCase("true")) return;
        manager.sendMessage(event.getPlayer(),event.message());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!LanguageUtils.getMessageString("chatbridge.join.enabled").equalsIgnoreCase("true")) return;
        this.manager.onConnection(event.getPlayer(), "quit");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (!LanguageUtils.getMessageString("chatbridge.join.enabled").equalsIgnoreCase("true")) return;
        this.manager.onConnection(event.getPlayer(), "join");
    }


}
