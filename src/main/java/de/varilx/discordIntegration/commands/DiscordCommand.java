package de.varilx.discordIntegration.commands;

import com.mojang.brigadier.Command;
import de.varilx.BaseAPI;
import de.varilx.configuration.VaxConfiguration;
import de.varilx.database.Service;
import de.varilx.database.repository.Repository;
import de.varilx.discordIntegration.entity.LinkCode;
import de.varilx.discordIntegration.entity.LinkedUser;
import de.varilx.utils.language.LanguageUtils;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class DiscordCommand {

    public DiscordCommand(JavaPlugin plugin, Service database) {
        Repository<LinkCode, UUID> repo = (Repository<LinkCode, UUID>) database.getRepository(LinkCode.class);
        LifecycleEventManager<Plugin> manager = plugin.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                    Commands.literal("discord")
                            .executes(ctx -> {
                                ctx.getSource().getSender().sendMessage(LanguageUtils.getMessage("commands.link.format"));
                                return Command.SINGLE_SUCCESS;
                            })
                            .then(Commands.literal("reload")
                                    .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("discord.reload"))
                                    .executes(context -> {
                                        CompletableFuture.runAsync(() -> {
                                            BaseAPI.get().getConfiguration().reload();
                                            BaseAPI.get().getDatabaseConfiguration().reload();
                                            BaseAPI.get().getCurrentLanguageConfiguration().reload();
                                            context.getSource().getSender().sendMessage(LanguageUtils.getMessage("commands.reload.reloaded"));
                                        });
                                        return 1;
                                    })
                            )
                            .then(Commands.literal("link")
                                    .requires(commandSourceStack -> commandSourceStack.getSender() instanceof Player)
                                    .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("discord.link"))
                                    .executes(context -> {
                                        Player player = (Player) context.getSource().getSender();
                                        if (!BaseAPI.get().getConfiguration().getBoolean("discord-link.enabled")) {
                                            player.sendMessage(LanguageUtils.getMessage("commands.link.disabled"));
                                            return Command.SINGLE_SUCCESS;
                                        }
                                        Repository<LinkedUser, Long> userRepo = (Repository<LinkedUser, Long>) database.getRepository(LinkedUser.class);
                                        userRepo.findByFieldName("uuid", player.getUniqueId()).thenApply(linkedUser -> {
                                            if (linkedUser != null) {
                                                player.sendMessage(LanguageUtils.getMessage("commands.link.already-linked"));
                                                return true;
                                            }
                                            return false;
                                        }).thenAccept(used -> {
                                            if (used) return;
                                            long code = ThreadLocalRandom.current().nextLong(99999);
                                            repo.findFirstById(player.getUniqueId()).thenAccept(linkCode -> {
                                                if (linkCode == null) {
                                                    linkCode = new LinkCode(player.getUniqueId(), code, player.getName(), System.currentTimeMillis());
                                                    repo.insert(linkCode);
                                                }
                                                player.sendMessage(LanguageUtils.getMessage("commands.link.code-sent", Placeholder.parsed("code", String.valueOf(linkCode.getCode()))));
                                            });
                                        });
                                        return Command.SINGLE_SUCCESS;
                                    }))
                            .build(),
                    "The Discord Command from VDiscord",
                    List.of("vdiscord")
            );
        });
    }

}
