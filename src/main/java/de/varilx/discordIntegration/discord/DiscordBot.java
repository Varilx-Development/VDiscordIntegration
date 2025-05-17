package de.varilx.discordIntegration.discord;

import de.varilx.BaseAPI;
import de.varilx.configuration.VaxConfiguration;
import de.varilx.database.Service;
import de.varilx.database.repository.Repository;
import de.varilx.discordIntegration.VDiscordIntegration;
import de.varilx.discordIntegration.entity.LinkCode;
import de.varilx.discordIntegration.entity.LinkedUser;
import de.varilx.utils.language.LanguageUtils;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.JDALogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static net.dv8tion.jda.api.requests.GatewayIntent.*;
import static org.apache.commons.lang3.math.NumberUtils.toLong;

public class DiscordBot extends ListenerAdapter implements DiscordHandler {

    @Getter
    private JDA jda;
    private final VaxConfiguration configuration;
    private final Service databaseService;
    private final Repository<LinkedUser, Long> linkedUserRepository;
    private final Repository<LinkCode, UUID> linkCodeRepository;
    private final VDiscordIntegration plugin;

    @Getter
    private Guild guild;

    @Getter
    private TextChannel channel;


    public DiscordBot(VDiscordIntegration plugin, VaxConfiguration configuration, Service databaseService) {
        this.configuration = configuration;
        this.databaseService = databaseService;
        this.plugin = plugin;
        this.linkedUserRepository = (Repository<LinkedUser, Long>) this.databaseService.getRepository(LinkedUser.class);
        this.linkCodeRepository = (Repository<LinkCode, UUID>) this.databaseService.getRepository(LinkCode.class);

        if (configuration.getString("chatbridge.token").equalsIgnoreCase("discord_bot_token")) {
            plugin.getLogger().severe("No discord bot token provided, disabling...");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }

        JDALogger.setFallbackLoggerEnabled(false);

        JDABuilder jdaBuilder = JDABuilder
                .create(configuration.getString("chatbridge.token"), EnumSet.of(GUILD_MEMBERS, GUILD_MESSAGES, MESSAGE_CONTENT, DIRECT_MESSAGES));
        this.jda = jdaBuilder
                .setStatus(OnlineStatus.ONLINE)
                .addEventListeners(this)
                .build();


        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        this.guild = Optional.ofNullable(jda.getGuildById(configuration.getString("chatbridge.guild"))).orElse(this.jda.getGuilds().getFirst());
        this.channel = this.guild.getTextChannelById(configuration.getString("chatbridge.channel"));

        this.manageLifecyle("startup");
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.getChannel().getType().isGuild()) {
            System.out.println(event.getMessage().getContentRaw());
            if (!configuration.getBoolean("discord-link.enabled")) return;
            System.out.println(linkCodeRepository.findAll().join());
            this.linkCodeRepository.findByFieldName("code", toLong(event.getMessage().getContentRaw())).thenAccept(linkCode -> {
                System.out.println(linkCode);
                if (linkCode == null) return;
                event.getMessage().reply(LanguageUtils.getMessageString("commands.link.linked").replace("<name>", linkCode.getUsername())).queue();

                for (String command : BaseAPI.get().getConfiguration().getStringList("discord-link.commands")) {
                    Bukkit.getScheduler().runTask(this.plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("<name>", linkCode.getUsername())));
                }
                for (String roleId : BaseAPI.get().getConfiguration().getStringList("discord-link.gets-roles")) {
                    this.guild.addRoleToMember(event.getAuthor(), this.guild.getRoleById(roleId)).queue();
                }


                this.linkCodeRepository.deleteById(linkCode.get_id());
                this.linkedUserRepository.insert(new LinkedUser(UUID.randomUUID(), event.getAuthor().getIdLong(), linkCode.get_id(), linkCode.getUsername()));
            });
            return;
        }

        if (!event.getChannel().getType().isGuild() || !event.getChannel().getType().isMessage()) return;
        if (event.getAuthor().isBot()) return;
        if (!event.getChannel().getId().equalsIgnoreCase(configuration.getString("chatbridge.channel"))) return;
        if (!LanguageUtils.getMessageString("chatbridge.discord-message.enabled").equalsIgnoreCase("true")) return;
        this.linkedUserRepository.findFirstById(event.getAuthor().getIdLong()).thenAcceptAsync(user -> {
            if (user == null && BaseAPI.get().getConfiguration().getBoolean("discord-link.enforce")) {
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getServer().sendMessage(LanguageUtils.getMessage("chatbridge.discord-message.message",
                        Placeholder.parsed("name", user != null ? user.getIngameName() : "<discordname>"),
                        Placeholder.parsed("discordname", event.getAuthor().getEffectiveName()),
                        Placeholder.parsed("message", event.getMessage().getContentDisplay())
                ));
            });
        });
    }

    @Override
    public void sendMessage(Player player, Component message) {
        CompletableFuture.runAsync(() -> {
            try {
                this.channel.createWebhook(player.getName())
                        .setAvatar(Icon.from(URI.create("https://mc-heads.net/avatar/" + player.getUniqueId()).toURL().openStream(), Icon.IconType.PNG))
                        .setName(this.plugin.getLuckPermsService().getDisplayName(player).get())
                        .queue(webhook -> {
                            webhook.sendMessage(PlainTextComponentSerializer.plainText().serialize(message)).queue();
                            webhook.delete().delay(3, TimeUnit.SECONDS).queue();
                        });
            } catch (IOException | ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void onConnection(@NotNull Player player, String type) {
        CompletableFuture.runAsync(() -> {
            String displayName;
            try {
                displayName = this.plugin.getLuckPermsService().getDisplayName(player).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

            this.channel.sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.decode(LanguageUtils.getMessageString("chatbridge." + type + ".color")))
                    .setDescription(LanguageUtils.getMessageString("chatbridge." + type + ".message").replace("<player>", displayName))
                    .setAuthor(LanguageUtils.getMessageString("chatbridge." + type + ".title").replace("<player>", displayName),
                            null,
                            "https://mc-heads.net/avatar/" + player.getUniqueId())
                    .build()).queue();
        });
    }

    @Override
    public void manageLifecyle(String type) {
        if (!configuration.getString("chatbridge.type").equalsIgnoreCase("bot")) return;
        if (!LanguageUtils.getMessageString("chatbridge." + type + ".enabled").equalsIgnoreCase("true")) return;
        this.channel.sendMessageEmbeds(new EmbedBuilder()
                .setDescription(LanguageUtils.getMessageString("chatbridge." + type + ".message"))
                .setTitle(LanguageUtils.getMessageString("chatbridge." + type + ".title"))
                .setColor(Color.decode(LanguageUtils.getMessageString("chatbridge." + type + ".color")))
                .build()).queue();
    }
}
