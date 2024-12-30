package de.varilx.discordIntegration.luckperms;

import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public interface LuckPermsServiceAPI {

    CompletableFuture<String> getDisplayName(Player player);

}
