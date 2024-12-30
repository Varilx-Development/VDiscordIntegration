package de.varilx.discordIntegration;

import de.varilx.BaseAPI;
import de.varilx.database.Service;
import de.varilx.discordIntegration.discord.DiscordBot;
import de.varilx.discordIntegration.commands.DiscordCommand;
import de.varilx.discordIntegration.discord.DiscordHandler;
import de.varilx.discordIntegration.discord.webhook.WebhookManager;
import de.varilx.discordIntegration.entity.LinkCode;
import de.varilx.discordIntegration.entity.LinkedUser;
import de.varilx.discordIntegration.listener.MinecraftListener;
import de.varilx.discordIntegration.luckperms.LuckPermsService;
import de.varilx.discordIntegration.luckperms.LuckPermsServiceAPI;
import de.varilx.discordIntegration.webhook.DiscordWebhook;
import de.varilx.utils.language.LanguageUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class VDiscordIntegration extends JavaPlugin {

    @Getter
    LuckPermsServiceAPI luckPermsService;

    DiscordHandler manager;

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        new BaseAPI(this).enable();

        Service service = Service.load(BaseAPI.getBaseAPI().getDatabaseConfiguration().getConfig(), getClassLoader());

        service.create(LinkedUser.class, Long.class);
        service.create(LinkCode.class, UUID.class);

        YamlConfiguration config = BaseAPI.getBaseAPI().getConfiguration().getConfig();

        this.manager = switch (config.getString("chatbridge.type").toLowerCase()) {
            case "bot" -> new DiscordBot(this, BaseAPI.getBaseAPI().getConfiguration(), service);
            default -> new WebhookManager(this);
        };

        try {
            Class.forName("net.luckperms.api.LuckPerms");
            this.luckPermsService = new LuckPermsService(this, service, this.manager);
        } catch (Throwable throwable) {
            getLogger().warning("\n--------\nCould not find Luckperms, enabling without LuckPerms\n-------");
            this.luckPermsService = player -> CompletableFuture.supplyAsync(() -> player.getName());
        }

        new DiscordCommand(this, service);

        Bukkit.getPluginManager().registerEvents(new MinecraftListener(manager), this);

        Bukkit.getServer().sendMessage(LanguageUtils.getMessage("startup"));
    }

    @Override
    public void onDisable() {
        this.manager.manageLifecyle("shutdown");
    }
}