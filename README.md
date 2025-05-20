![V-Discord-Integration](https://cdn.varilx.de/raw/micMsI.png)

<div align="center">

![Build Status](https://img.shields.io/github/actions/workflow/status/Varilx-Development/VDiscordIntegration/build.yml?branch=main)
![Release](https://img.shields.io/github/v/release/Varilx-Development/VDiscordIntegration)
[![Available on Modrinth](https://raw.githubusercontent.com/vLuckyyy/badges/main/avaiable-on-modrinth.svg)](https://modrinth.com/plugin/vdiscord-intergration)

</div>

# V-Discord Integration | Simple Discord Chatbridge

V-Discord Integration is a powerful plugin that creates a seamless chat bridge between Minecraft and Discord. Enhance your server's communication and keep your community connected across platforms!

## 🌟 Features

- **Chat Bridge**: Real-time message syncing between Minecraft and Discord
- **Flexible Configuration**: Choose between BOT and WEBHOOK modes
- **Account Linking**: Optional Discord-Minecraft account linking
- **Custom Messages**: Fully customizable messages using MiniMessage format
- **Role Sync**: Synchronize Minecraft roles with Discord roles
- **Multi-language Support**: Currently supports `en` and `de`

## 📸 Preview

<div style="display: flex; justify-content: center; gap: 20px; flex-wrap: wrap;">
  <img src="https://github.com/user-attachments/assets/29662231-e8b8-4e93-a178-73dbf3b7d3ed" alt="In-game Preview" style="width: 45%; border: 2px solid #ccc; border-radius: 10px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);">
  <img src="https://github.com/user-attachments/assets/5aace62e-ecb9-429c-80fa-365d652d2496" alt="Discord Preview" style="width: 45%; border: 2px solid #ccc; border-radius: 10px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);">
</div>

## 🚀 Quick Start

1. Download the plugin from [Modrinth](https://modrinth.com/plugin/vdiscord-intergration)
2. Place the JAR file in your server's `plugins` folder
3. Start your server to generate the configuration files
4. Configure the `config.yml` file (see Configuration section)
5. Restart your server

## ⚙️ Configuration

The plugin is highly configurable. Here's a quick overview of the main sections:

1. **Database**: Choose between *SQLite, or MySQL*
2. **Chat Bridge**: Set up BOT or WEBHOOK mode
3. **Discord Linking**: Enable/disable account linking
4. **Custom Messages**: Customize all plugin messages
5. **Permissions**: Manage user permissions

## 📚 Example Configurations

<details>
<summary>Click to view example config.yml</summary>

```yaml
language: "en"

chatbridge:
   type: BOT
   webhook:
      url: "webhook_url"
      avatar: "some_avatar_url"
      name: "ChatBridge"
   guild: 1322873747535040512
   channel: 1323049958911381515
   token: "discord_bot_token"

discord-link:
   enabled: true
   enforce: false
   gets-roles:
      - 1323067372214419526
   commands:
      - "lp user <name> parent set linked"

luckperms:
   prefix: true

role-sync:
   enabled: true
   delay: 30000
   roles:
      admin: 1323313717336608808
```
</details> <details> <summary>Click to view example messages.yml</summary>

```yaml
Apply
prefix: "<b><gradient:#08FB22:#BBFDAD>[VDiscord]</gradient></b><reset><!i><gray> "
startup: "<prefix>Discord Integration has started up!"

chatbridge:
   join:
      enabled: true
      color: "#00FF00"
      title: "<player> has joined the game"
   quit:
      enable: true
      color: "#FF0000"
      title: "<player> has left the game"
   ingame-message:
      enabled: true
      message: "<message>"
      name: "<name>"
   discord-message:
      enabled: true
      message: "<dark_gray>[<blue><b>Discord</b></blue>] <gray><discordname> <dark_gray>»  <yellow><message>"

commands:
   reload:
      reloaded: "<prefix>The config has been reloaded"
   link:
      linked: "You have been connected to the account <name>"
      code-sent: "<prefix>To link your discord account, send the following code to the DiscordBot: <click:copy_to_clipboard:<code>><hover:show_text:Click here to copy><yellow><code></yellow> (click to copy)"
```
</details>

## 🔧 Permissions
```discord.link```: Allows users to link their Discord and Minecraft accounts

```discord.reload```: Allows users to reload the plugin configuration


## 🆘 Support

<a href="https://discord.gg/ZPyb9g6Gs4"> <img src="https://cdn.varilx.de/raw/Zm9inS.png" alt="Join our Discord for help" width="300"> </a>