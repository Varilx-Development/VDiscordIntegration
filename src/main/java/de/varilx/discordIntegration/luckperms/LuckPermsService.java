package de.varilx.discordIntegration.luckperms;

import de.varilx.BaseAPI;
import de.varilx.configuration.file.YamlConfiguration;
import de.varilx.database.Service;
import de.varilx.database.repository.Repository;
import de.varilx.discordIntegration.discord.DiscordBot;
import de.varilx.discordIntegration.discord.DiscordHandler;
import de.varilx.discordIntegration.entity.LinkedUser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedDataManager;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class LuckPermsService implements LuckPermsServiceAPI {

    private final JavaPlugin plugin;

    private final Service database;

    private final DiscordHandler discordHandler;

    @Nullable
    private LuckPerms luckPerms;



    public LuckPermsService(JavaPlugin plugin, Service database, DiscordHandler discordHandler) {
        this.plugin = plugin;
        this.database = database;
        this.discordHandler = discordHandler;
        this.luckPerms = LuckPermsProvider.get();

        this.syncRoles((int) (BaseAPI.getBaseAPI().getConfiguration().getConfig().getInt("role-sync.delay") * 0.02));
    }

    private void syncRoles(int delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            YamlConfiguration config = BaseAPI.getBaseAPI().getConfiguration().getConfig();
            if (config.getBoolean("role-sync.enabled") && discordHandler instanceof DiscordBot bot) {
                ((Repository<LinkedUser, Long>) database.getRepository(LinkedUser.class)).findAll().thenAccept(users -> {
                    for (LinkedUser user : users) {
                        net.dv8tion.jda.api.entities.User discordUser = bot.getJda().retrieveUserById(user.getDiscordId()).complete();
                        if (!bot.getGuild().isMember(discordUser)) continue;
                        Member member = bot.getGuild().getMember(discordUser);

                        for (String roleId : config.getConfigurationSection("role-sync.discord").getKeys(false)) {
                            Role roleById = bot.getGuild().getRoleById(roleId);
                            if (roleById == null) {
                                plugin.getLogger().warning("The discord role of " + roleId + " doesnt exists");
                                continue;
                            }
                            Group group = getGroupByName(config.getString("role-sync.discord." + roleId));
                            if (member.getRoles().contains(roleById)) {
                                if (hasGroup(user.getUuid(), group)) continue;
                                addGroup(user.getUuid(), group);
                            } else {
                                if (!hasGroup(user.getUuid(), group)) continue;
                                removeGroup(user.getUuid(), group);
                            }
                        }
                        for (String roleName : config.getConfigurationSection("role-sync.luckperms").getKeys(false)) {
                            Role roleById = bot.getGuild().getRoleById(config.getString("role-sync.luckperms." + roleName));
                            if (roleById == null) {
                                plugin.getLogger().warning("The discord role of " + roleName + " doesnt exists");
                                continue;
                            }
                            Group group = getGroupByName(roleName);
                            if (hasGroup(user.getUuid(), group)) {
                                bot.getGuild().addRoleToMember(discordUser, roleById).queue();
                            } else {
                                bot.getGuild().removeRoleFromMember(discordUser, roleById).queue();
                            }
                        }
                    }
                });

            }
            this.syncRoles((int) (config.getInt("role-sync.delay") * 0.02));
        }, delay);
    }

    @Override
    public CompletableFuture<String> getDisplayName(Player player) {
        return CompletableFuture.supplyAsync(() -> {
            if (!BaseAPI.getBaseAPI().getConfiguration().getConfig().getBoolean("luckperms.prefix")) return player.getName();
            String prefix = getGroupPrefixById(player.getUniqueId());
            if (prefix == null) return player.getName();
            return prefix + " | " + player.getName();
        });
    }

    public void addGroup(UUID uuid, Group group) {
        getUserByIdAsync(uuid).thenAcceptAsync(user -> {
            InheritanceNode node = InheritanceNode.builder(group).value(true).build();
            DataMutateResult result = user.data().add(node);
            luckPerms.getUserManager().saveUser(user);
        });
    }
    public void removeGroup(UUID uuid, Group group) {
        getUserByIdAsync(uuid).thenAcceptAsync(user -> {
            InheritanceNode node = InheritanceNode.builder(group).value(true).build();
            DataMutateResult result = user.data().remove(node);
            luckPerms.getUserManager().saveUser(user);
        });
    }

    public User getUserById(UUID uniqueId) {
        if (luckPerms.getUserManager().getUser(uniqueId) == null) {
            try {
                return luckPerms.getUserManager().loadUser(uniqueId).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return luckPerms.getUserManager().getUser(uniqueId);
    }

    public CompletableFuture<User> getUserByIdAsync(UUID uniqueId) {
        return CompletableFuture.supplyAsync(() -> getUserById(uniqueId));
    }

    public boolean hasGroup(UUID uuid, Group group) {
        User user = getUserById(uuid);
        if (user == null) {
            return false;
        }
        return user.getInheritedGroups(QueryOptions.nonContextual()).contains(group);
    }

    public Group getGroupById(UUID uniqueId) {
        User user = getUserById(uniqueId);
        if (user == null) {
            return null;
        }
        String primaryGroup = user.getPrimaryGroup();
        return luckPerms.getGroupManager().getGroup(primaryGroup);
    }

    public Group getGroupByName(String groupName) {
        return luckPerms.getGroupManager().getGroup(groupName);
    }

    public String getGroupPrefixById(UUID uniqueId) {
        Group group = getGroupById(uniqueId);
        if (group == null) {
            return null;
        }
        CachedDataManager cachedData = group.getCachedData();
        String prefix = cachedData.getMetaData().getPrefix();
        if (prefix == null) return null;

        String colorCodePattern = "&[0-9a-fk-orA-FK-OR]";
        String miniMessageTagPattern = "<[^>]*>";
        String combinedPattern = colorCodePattern + "|" + miniMessageTagPattern;

        return Pattern.compile(combinedPattern).matcher(prefix).replaceAll("");
    }




}
