package de.varilx.discordIntegration.listener;

import de.varilx.BaseAPI;
import de.varilx.discordIntegration.bot.DiscordBot;
import de.varilx.discordIntegration.webhook.DiscordWebhook;
import de.varilx.utils.language.LanguageUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class MinecraftListener implements Listener {

    private final DiscordBot bot;
    private final String webhookUrl;
    private final YamlConfiguration configuration;

    public MinecraftListener(DiscordBot bot, String webhook) {
        this.bot = bot;
        this.webhookUrl = webhook;
        this.configuration = BaseAPI.getBaseAPI().getConfiguration().getConfig();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncChat(AsyncChatEvent event) {
        if (webhookUrl != null) {
            DiscordWebhook webhook = new DiscordWebhook(this.webhookUrl);
            webhook.setAvatarUrl("https://mc-heads.net/avatar/" + event.getPlayer().getUniqueId());
            webhook.setUserName(event.getPlayer().getName());
            webhook.setContent(PlainTextComponentSerializer.plainText().serialize(event.message()));
            try {
                webhook.execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (bot == null) return;
        CompletableFuture.runAsync(() -> {
            try {
                bot.getChannel().createWebhook(event.getPlayer().getName())
                        .setAvatar(Icon.from(URI.create("https://mc-heads.net/avatar/" + event.getPlayer().getUniqueId()).toURL().openStream(), Icon.IconType.PNG))
                        .setName(event.getPlayer().getName())
                        .queue(webhook -> {
                            webhook.sendMessage(PlainTextComponentSerializer.plainText().serialize(event.message())).queue();
                            webhook.delete().queue();
                        });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (webhookUrl != null) {
            DiscordWebhook webhook = new DiscordWebhook(this.webhookUrl);
            webhook.addEmbedObjects(new DiscordWebhook.EmbedObject()
                    .setColor(new DiscordWebhook.Color(Color.decode(LanguageUtils.getMessageString("chatbridge.quit.color"))))
                    .setDescription(LanguageUtils.getMessageString("chatbridge.quit.message").replace("<player>", event.getPlayer().getName()))
                    .setAuthor(LanguageUtils.getMessageString("chatbridge.quit.title").replace("<player>", event.getPlayer().getName()),
                            null,
                            "https://mc-heads.net/avatar/" + event.getPlayer().getUniqueId()));
            webhook.setUserName(this.configuration.getString("chatbridge.webhook.name"));
            webhook.setAvatarUrl(this.configuration.getString("chatbridge.webhook.avatar"));
            try {
                webhook.execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (bot == null) return;
        bot.getChannel().sendMessageEmbeds(new EmbedBuilder()
                .setColor(Color.decode(LanguageUtils.getMessageString("chatbridge.quit.color")))
                .setDescription(LanguageUtils.getMessageString("chatbridge.quit.message").replace("<player>", event.getPlayer().getName()))
                .setAuthor(LanguageUtils.getMessageString("chatbridge.quit.title").replace("<player>", event.getPlayer().getName()),
                        null,
                        "https://mc-heads.net/avatar/" + event.getPlayer().getUniqueId())
                .build()).queue();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (webhookUrl != null) {
            DiscordWebhook webhook = new DiscordWebhook(this.webhookUrl);
            webhook.addEmbedObjects(new DiscordWebhook.EmbedObject()
                    .setColor(new DiscordWebhook.Color(Color.decode(LanguageUtils.getMessageString("chatbridge.join.color"))))
                    .setDescription(LanguageUtils.getMessageString("chatbridge.join.message").replace("<player>", event.getPlayer().getName()))
                    .setAuthor(LanguageUtils.getMessageString("chatbridge.join.title").replace("<player>", event.getPlayer().getName()),
                            null,
                            "https://mc-heads.net/avatar/" + event.getPlayer().getUniqueId()));
            webhook.setUserName(this.configuration.getString("chatbridge.webhook.name"));
            webhook.setAvatarUrl(this.configuration.getString("chatbridge.webhook.avatar"));
            try {
                webhook.execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (bot == null) return;
        bot.getChannel().sendMessageEmbeds(new EmbedBuilder()
                .setColor(Color.decode(LanguageUtils.getMessageString("chatbridge.join.color")))
                .setAuthor(LanguageUtils.getMessageString("chatbridge.join.title").replace("<player>", event.getPlayer().getName()),
                        null,
                        "https://mc-heads.net/avatar/" + event.getPlayer().getUniqueId())
                .setDescription(LanguageUtils.getMessageString("chatbridge.join.message").replace("<player>", event.getPlayer().getName()))
                .build()).queue();
    }


}
