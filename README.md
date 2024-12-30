![V-Discord-Intergration](https://cdn.modrinth.com/data/cached_images/6eddb7b31cf0eeed5156ae4c6ac961da2f02aa7a_0.webp)
![Discord](https://img.shields.io/discord/1322873747535040512)
![Build Status](https://img.shields.io/github/actions/workflow/status/Varilx-Development/VDiscordIntegration/build.yml?branch=main)
![Release](https://img.shields.io/github/v/release/Varilx-Development/VDiscordIntegration)

# VDiscord Integration Configuration

A discord chat bridge between minecraft and discord and vice versa

---


## Configuration Overview

### 1. **Database**
Define the database type and connection details. The plugin supports:
- **MongoDB**: Specify a connection string and database name.
- **SQL**: Provide a JDBC connection string. (No username/password is required for SQLite.)

### 2. **Chat Bridge**
Select the bridge type:
- **BOT**: Requires a Discord bot token, guild ID, and channel ID.
- **WEBHOOK**: Requires a webhook URL.

### 3. **Discord Linking**
Enable or disable account linking. When enabled, you can enforce linking for chat participation and assign roles or execute commands upon successful linking.

### 4. **Custom Messages**
Customize messages for server startup, player join/quit, and Discord chat using the MiniMessage format.
We currently support: `de` and `en`

### 5. **Commands**
Configure responses for commands like `/discord link` and `/discord reload`.

---

## Setup Instructions

1. Download and install the plugin on your Minecraft server.
2. Configure the `config.yml` file with your preferred settings:
    - Set the database type and connection details.
    - Configure the chat bridge (BOT or WEBHOOK).
    - Enable and customize account linking if needed.
    - Define custom messages using MiniMessage.
3. Restart the server to apply the changes.

---

## Example config

```yaml
language: "en"

chatbridge:
   type: BOT # Options: BOT (mc -> discord, discord -> mc), WEBHOOK (mc -> discord)

   # This is only necessary if you picked: WEBHOOK
   webhook:
      url: "webhook_url"
      avatar: "some_avatar_url" # Not necessary
      name: "ChatBridge" # Not necessary

   # This is only necessary if you picked: BOT
   guild: 1322873747535040512 # The Guild id
   channel: 1323049958911381515 # Channel id of the synced chat
   token: "discord_bot_token"


discord-link: # Only possbile if type is BOT
   enabled: true
   enforce: false # If set to true, not linked users won't be able to send messages in the discord chat
   gets-roles: # These roles will be added to the discord user, when they link their discord account
      - 1323067372214419526
   commands: # These will be executed when they link
      - "lp user <name> parent set linked"

# Only works if LuckPerms is on the server


luckperms:
   prefix: true # Displays the prefix of the current user in the <name>, of a join, quit and message (e.g. <group> | <name>)

# This will give discord users every rank they have ingame also in the discord
role-sync:
   enabled: true
   delay: 30000 # Every 30 seconds
   roles: # Makes links between "minecraft_role":"discord_role_id"
      admin: 1323313717336608808

```

---

## Example Message Configuration

```yaml
# Using Minimessage https://docs.advntr.dev/minimessage/format.html

prefix: "<b><gradient:#08FB22:#BBFDAD>[VDiscord]</gradient></b><reset><!i><gray> " # This prefix can be used anywhere as "<prefix>"
startup: "<prefix>Discord Integration has started up!"

chatbridge:
   join:
      enabled: true
      color: "#00FF00"
      message: ""
      title: "<player> has joined the game"
      name: "Chatbridge" # The name of the webhook sender defaults to config.yml "chatbridge.webhook.name"
      avatar: "some_custom_avatar_url" # defaults to config.yml "chatbridge.webhook.avatar"
   quit:
      enable: true
      color: "#FF0000"
      message: ""
      title: "<player> has left the game"
      name: "Chatbridge" # defaults to config.yml "chatbridge.webhook.name"
      avatar: "some_custom_avatar_url" # defaults to config.yml "chatbridge.webhook.avatar"
   startup:
      enabled: true
      color: "#00FF00"
      message: "The Server has started"
      title: "Startup"
   shutdown:
      enabled: true
      color: "#FF0000"
      message: "The Server has stopped"
      title: "Shutdown"
   ingame-message: # Ingame -> Discord
      enabled: true
      message: "<message>"
      name: "<name>"  # You can also use <name>, but thats only returns the ign
   discord-message: # Discord -> Ingame, only with Bot available
      enabled: true
      message: "<dark_gray>[<blue><b>Discord</b></blue>] <gray><discordname> <dark_gray>»  <yellow><message>"

commands:
   reload:
      reloaded: "<prefix>The config has been reloaded"
   link:
      linked: "You have been connected to the account <name>"
      already-linked: "<prefix><red>You are already linked, you cant link again"
      disabled: "<prefix><red>This feature is disabled"
      format: "<prefix>This command doesnt exists, use the /discord link command"
      code-sent: "<prefix>To link your discord account, send the following code to the DiscordBot: <click:copy_to_clipboard:<code>><hover:show_text:Click here to copy><yellow><code></yellow> (click to copy)"
```

---

## Example Database Configuration

```yaml
type: Sqlite # Avaiable types: mongo, mysql, sqlite


# MONGO
Mongo:
  connection-string: "mongodb://<username>:<password>@<host>:<port>/"
  database: "db"


# SQL
SQL:
  connection-string: "jdbc:sqlite:plugins/VDiscordIntegration/database.db"
  username: "username" # Not required for sqlite
  password: "password" # Not required for sqlite
```


---

## Notes

- The MiniMessage format is highly flexible for styling and formatting messages. Refer to the [MiniMessage documentation](https://docs.advntr.dev/minimessage/format.html) for more details.
- Ensure your Discord bot token and IDs are correctly configured for the BOT mode.
- SQLite is the simplest database option as it doesn’t require additional setup.
