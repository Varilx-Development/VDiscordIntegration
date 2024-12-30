package de.varilx.discordIntegration.discord;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface DiscordHandler {

    void sendMessage(Player player, Component message);

    void onConnection(Player player, String type);

    void manageLifecyle(String type);

}
