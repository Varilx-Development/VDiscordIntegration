package de.varilx.discordIntegration;

import de.varilx.BaseAPI;
import de.varilx.database.Service;
import de.varilx.discordIntegration.bot.DiscordBot;
import de.varilx.discordIntegration.commands.DiscordCommand;
import de.varilx.discordIntegration.entity.LinkCode;
import de.varilx.discordIntegration.entity.LinkedUser;
import de.varilx.discordIntegration.listener.MinecraftListener;
import de.varilx.discordIntegration.webhook.DiscordWebhook;
import de.varilx.utils.language.LanguageUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import sun.misc.Unsafe;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.UUID;

public final class VDiscordIntegration extends JavaPlugin {

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        // Discble Reflections logger
        try {
            Reflections.log.getClass();
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);

            Field field = Reflections.class.getDeclaredField("log");

            Object staticFieldBase = unsafe.staticFieldBase(field);

            long staticFieldOffset = unsafe.staticFieldOffset(field);
            unsafe.putObject(staticFieldBase, staticFieldOffset, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        new BaseAPI(this).enable();

        Service service = Service.load(BaseAPI.getBaseAPI().getDatabaseConfiguration().getConfig(), getClassLoader());

        service.create(LinkedUser.class, Long.class);
        service.create(LinkCode.class, UUID.class);

        DiscordBot bot = null;
        String webhookUrl = null;
        YamlConfiguration config = BaseAPI.getBaseAPI().getConfiguration().getConfig();

        switch (config.getString("chatbridge.type").toLowerCase()) {
            case "bot" -> {
                bot = new DiscordBot(this, BaseAPI.getBaseAPI().getConfiguration(), service);
            }
            case "webhook" -> {
                webhookUrl = config.getString("chatbridge.webhook.url");
                DiscordWebhook webhook = new DiscordWebhook(webhookUrl);
                webhook.addEmbedObjects(new DiscordWebhook.EmbedObject()
                        .setDescription(LanguageUtils.getMessageString("chatbridge.startup.message"))
                        .setTitle(LanguageUtils.getMessageString("chatbridge.startup.title"))
                        .setColor(new DiscordWebhook.Color(Color.decode(LanguageUtils.getMessageString("chatbridge.startup.color"))))
                );
                webhook.setUserName(config.getString("chatbridge.webhook.name"));
                webhook.setAvatarUrl(config.getString("chatbridge.webhook.avatar"));
                try {
                    webhook.execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        new DiscordCommand(this, service);

        Bukkit.getPluginManager().registerEvents(new MinecraftListener(bot, webhookUrl), this);

        Bukkit.getServer().sendMessage(LanguageUtils.getMessage("startup"));
    }

}