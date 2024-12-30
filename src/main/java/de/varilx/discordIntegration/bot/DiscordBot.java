package de.varilx.discordIntegration.bot;

import de.varilx.BaseAPI;
import de.varilx.config.Configuration;
import de.varilx.database.Repository;
import de.varilx.database.Service;
import de.varilx.discordIntegration.entity.LinkCode;
import de.varilx.discordIntegration.entity.LinkedUser;
import de.varilx.utils.language.LanguageUtils;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.JDALogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.EnumSet;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static net.dv8tion.jda.api.requests.GatewayIntent.*;

public class DiscordBot extends ListenerAdapter {

    @Getter
    private final JDA jda;
    private final Configuration configuration;
    private final Service databaseService;
    private final Repository<LinkedUser, Long> linkedUserRepository;
    private final Repository<LinkCode, UUID> linkCodeRepository;
    private final JavaPlugin plugin;

    @Getter
    private Guild guild;

    @Getter
    private TextChannel channel;


    public DiscordBot(JavaPlugin plugin, Configuration configuration, Service databaseService) {
        this.configuration = configuration;
        this.databaseService = databaseService;
        this.plugin = plugin;
        this.linkedUserRepository = (Repository<LinkedUser, Long>) this.databaseService.getRepositoryMap().get(LinkedUser.class);
        this.linkCodeRepository = (Repository<LinkCode, UUID>) this.databaseService.getRepositoryMap().get(LinkCode.class);

        JDALogger.setFallbackLoggerEnabled(false);

        JDABuilder jdaBuilder = JDABuilder
                .create(configuration.getConfig().getString("chatbridge.token"), EnumSet.of(GUILD_MEMBERS, GUILD_MESSAGES, GUILD_VOICE_STATES, MESSAGE_CONTENT, DIRECT_MESSAGES));
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
        this.guild = jda.getGuildById(configuration.getConfig().getString("chatbridge.guild"));
        this.channel = this.guild.getTextChannelById(configuration.getConfig().getString("chatbridge.channel"));

        this.channel.sendMessageEmbeds(new EmbedBuilder()
                .setDescription(LanguageUtils.getMessageString("chatbridge.startup.message"))
                .setTitle(LanguageUtils.getMessageString("chatbridge.startup.title"))
                .setColor(Color.decode(LanguageUtils.getMessageString("chatbridge.startup.color")))
                .build()).queue();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.getChannel().getType().isGuild()) {
            this.linkCodeRepository.findByFieldName("code", event.getMessage().getContentRaw()).thenAccept(linkCode -> {
                if (linkCode == null) return;
                event.getMessage().reply(LanguageUtils.getMessageString("commands.link.linked").replace("<name>", linkCode.getUsername())).queue();

                for (String command : BaseAPI.getBaseAPI().getConfiguration().getConfig().getStringList("discord-link.commands")) {
                    Bukkit.getScheduler().runTask(this.plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("<name>", linkCode.getUsername())));
                }
                for (String roleId : BaseAPI.getBaseAPI().getConfiguration().getConfig().getStringList("discord-link.gets-roles")) {
                    this.guild.addRoleToMember(event.getAuthor(), this.guild.getRoleById(roleId)).queue();
                }


                this.linkCodeRepository.deleteById(linkCode.getLink());
                this.linkedUserRepository.insert(new LinkedUser(event.getAuthor().getIdLong(), linkCode.getLink(), linkCode.getUsername()));
            });
            return;
        }

        if (!event.getChannel().getType().isGuild() || !event.getChannel().getType().isMessage()) return;
        if (event.getAuthor().isBot()) return;
        this.linkedUserRepository.findFirstById(event.getAuthor().getIdLong()).thenAcceptAsync(user -> {
            if (user == null && BaseAPI.getBaseAPI().getConfiguration().getConfig().getBoolean("discord-link.enforce")) {
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getServer().sendMessage(LanguageUtils.getMessage("chatbridge.sent-message",
                        Placeholder.parsed("name", user != null ? user.getIngameName() : "<discordname>"),
                        Placeholder.parsed("discordname", event.getAuthor().getEffectiveName()),
                        Placeholder.parsed("message", event.getMessage().getContentRaw())
                ));
            });
        });
    }
}
