package de.varilx.discordIntegration.discord.webhook;

import de.varilx.BaseAPI;
import de.varilx.config.Configuration;
import de.varilx.discordIntegration.VDiscordIntegration;
import de.varilx.discordIntegration.discord.DiscordHandler;
import de.varilx.discordIntegration.webhook.DiscordWebhook;
import de.varilx.utils.language.LanguageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.concurrent.CompletableFuture;

public class WebhookManager implements DiscordHandler {

    private final Configuration configuration;
    private final VDiscordIntegration plugin;

    public WebhookManager(VDiscordIntegration plugin) {
        this.plugin = plugin;
        this.configuration = BaseAPI.getBaseAPI().getConfiguration();
        this.manageLifecyle("startup");
    }

    @Override
    public void manageLifecyle(String type) {
        if (!configuration.getConfig().getString("chatbridge.type").equalsIgnoreCase("webhook")) return;
        if (!LanguageUtils.getMessageString("chatbridge." + type + ".enabled").equalsIgnoreCase("true")) return;
        this.create(webhook -> {
            webhook.addEmbedObjects(new DiscordWebhook.EmbedObject()
                    .setColor(new DiscordWebhook.Color(Color.decode(LanguageUtils.getMessageString("chatbridge." + type + ".color"))))
                    .setDescription(LanguageUtils.getMessageString("chatbridge." + type + ".message"))
                    .setTitle(LanguageUtils.getMessageString("chatbridge." + type + ".title")));
        });
    }

    @Override
    public void sendMessage(Player player, Component message) {
        if (!configuration.getConfig().getString("chatbridge.type").equalsIgnoreCase("webhook")) return;
        if (!LanguageUtils.getMessageString("chatbridge.ingame-message.enabled").equalsIgnoreCase("true")) return;
        this.create(webhook -> {
            webhook.setContent(PlainTextComponentSerializer.plainText().serialize(message));
        });
    }

    @Override
    public void onConnection(final Player player, String type) {
        if (!configuration.getConfig().getString("chatbridge.type").equalsIgnoreCase("webhook")) return;
        if (!LanguageUtils.getMessageString("chatbridge." + type + ".enabled").equalsIgnoreCase("true")) return;
        this.create(webhook -> {
            String displayName = this.plugin.getLuckPermsService().getDisplayName(player).get();

            webhook.addEmbedObjects(new DiscordWebhook.EmbedObject()
                    .setColor(new DiscordWebhook.Color(Color.decode(LanguageUtils.getMessageString("chatbridge." + type + ".color"))))
                    .setDescription(LanguageUtils.getMessageString("chatbridge." + type + ".message").replace("<player>", displayName))
                    .setAuthor(LanguageUtils.getMessageString("chatbridge." + type + ".title").replace("<player>", displayName),
                            null,
                            "https://mc-heads.net/avatar/" + player.getUniqueId()));
        });
    }

    private void create(ErrorConsumer<DiscordWebhook> consumer) {
        if (this.configuration.getConfig().getString("chatbridge.webhook.url") == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                DiscordWebhook webhook = new DiscordWebhook(this.configuration.getConfig().getString("chatbridge.webhook.url"));
                consumer.accept(webhook);

                if (!webhook.hasUsername()) {
                    webhook.setUserName(this.configuration.getConfig().getString("chatbridge.webhook.name"));
                }
                if (!webhook.hasAvatar()) {
                    webhook.setAvatarUrl(this.configuration.getConfig().getString("chatbridge.webhook.avatar"));
                }
                webhook.execute();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            throw new RuntimeException(e);
        });
    }


    @FunctionalInterface
    private interface ErrorConsumer<T> {

        void accept(T t) throws Exception;

    }

}
