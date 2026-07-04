<div align="center">

# 🏘️ Realistic Villagers
**Turn your static villages into a living, breathing civilization.**

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.18--26.2-green)](https://modrinth.com)
[![Requires: PacketEvents](https://img.shields.io/badge/Requires-PacketEvents-orange)](https://modrinth.com/plugin/packetevents)

[ ✨ Features ](#features) • [ 🌐 Config Website ](#config-website) • [ 🚀 Installation ](#installation) • [ ⚙️ Configuration ](#configuration) • [ 💻 Commands ](#commands) • [ 🤝 Compatibility ](#compatibility)

</div>

---

## 📖 The Vision

Forget the days of "Hmmmm" and blank stares. **Realistic Villagers** is the ultimate overhaul for Minecraft villagers. By replacing standard entities with dynamic, skin-aware NPCs, we bring a level of depth and immersion previously unseen — from genealogy and marriage to region-aware personalities and a brand-new hologram-based interface. Whether it's building a family legacy, managing complex trades, or defending your home, every villager has a story.

---

<a id="config-website"></a>
## 🌐 Visual Config Website

<p align="center">
  <a href="https://wolfcode.pl/projekty/rv-config/">
    <img src="https://img.shields.io/badge/%F0%9F%8C%90%20Open%20the%20Visual%20Config%20Editor-Click%20here-6366f1?style=for-the-badge" alt="Visual Config Website" />
  </a>
</p>

<div align="center">

No more digging through raw YAML — generate and tweak your `config.yml`, holograms, GUI layout and messages visually, then drop the result straight into your server.

</div>

> ⚠️ **Heads up:** The config editor is only guaranteed to work with the **latest** plugin version — configs generated for older releases may be missing options or out of sync with newly added settings.

---

<a id="features"></a>
## ✨ Features

### 🧬 The Human Factor
- **Dynamic Identity:** Every NPC is uniquely gendered (Male/Female) with a personality-driven skin via **MineSkin**.
- **Professional Names:** No more "Villager #432". Meet *John the Farmer* or *Raquel the Cleric*.
- **Family Legacies:** A deep genealogy system. NPCs remember their parents, children, and spouses across generations.
- **Marriage & Procreation:** Use Wedding Rings to form bonds and watch families grow over time. Child variants grow into adults.
- **Gender Freedom:** Disable the gender system entirely, or assign a custom gender to each player individually.

### 🌍 Regionality System
- Villagers are shaped by **where they live**. A desert biome breeds villagers with darker skin tones, Arabic names, and conversations typical of desert dwellers.
- Snowy biomes dress villagers in warm robes, give them Nordic names, an accent, and a habit of complaining about the cold.
- Every part of this system — skins, names, dialogue — is **fully customizable or can be disabled** if you prefer vanilla-style consistency.

### 🧠 Advanced Artificial Intelligence
- **Strategic Combat:** Villagers weigh the odds before engaging — outnumbered NPCs retreat instead of dying pointlessly, and unarmed villagers panic and flee.
- **Armed & Dangerous:** NPCs can wield maces and spears, with custom melee weapons configurable per profession.
- **Calls for Backup:** Villagers call family members for help when threatened.
- **Social Interaction:** Talk, joke, flirt, or insult — your reputation changes based on every word.
- **Interactive Needs:** Hunger, inventory management, and daily activities like looting and fishing.
- **Atmospheric Revive Ritual:** Lose a beloved villager? A lightning bolt striking within 2 blocks of the head begins the sacred revival ritual.

### 🐫 Life on the Move
- Villagers can now **ride camels and llamas**, wandering and traveling the world instead of standing still.

### 🏠 Villager Housing
- Turn any chest or barrel into a **villager's personal storage** by placing a sign with `[Villager]` on it.
- Assign specific items the villager is allowed to take from it — no more raiding the wrong chest.

### 🎨 Visual & Technical Excellence
- **All-New Hologram Menu:** A completely redesigned interactive villager menu built on holograms — prefer the classic look? Switch back to the old chest GUI with a single config toggle.
- **Real-Time Nametags:** Live updates as your villagers level up their trades.
- **Modular Configuration:** The configuration file has been split into focused parts, making it far easier to navigate and customize.
- **Optional Recipes:** Disable the plugin's custom trade recipes entirely if you only want the NPC/behavior systems.
- **Optimized Performance:** Built on **PacketEvents** for minimal server impact and high stability.

---

<a id="installation"></a>
## 🚀 Installation

1. **Dependencies:** Ensure you have **[PacketEvents](https://modrinth.com/plugin/packetevents)** installed.
2. **Plugin:** Drop `RealisticVillagers.jar` into your `/plugins/` folder.
3. **Setup:** Restart the server to generate the default configuration and skin cache.
4. **Go!** Witness the transformation of every village in your world.

---

<a id="configuration"></a>
## ⚙️ Configuration

The plugin is highly customizable, and the config has been split into multiple files (`config.yml`, `gui.yml`, `holograms.yml`, `loot.yml`, `variable-text.yml`, and more) so each system is easy to find and tweak. Prefer a visual approach? Check out the [Visual Config Website](https://wolfcode.pl/projekty/rv-config/) above.

```yaml
# procreation settings
procreation-cooldown: 6000
baby-grow-cooldown: 12000

# Social settings
villager-farm:
  allow-procreation-between-family-members: false
  ignore-sex-when-procreating: false
```

---

<a id="commands"></a>
## 💻 Commands

| Command | Description |
| :--- | :--- |
| `/gender <male\|female>` | Set your player gender (mandatory unless disabled in config). |
| `/rv reload` | Hot-reload all configurations and messages. |
| `/rv skins` | Manage and preview NPC skin categories. |
| `/rv genderset <player> <gender>` | Admin command to override a player's gender. |

---

<a id="compatibility"></a>
## 🤝 Compatibility

We play nice with others:
- **PacketEvents:** ✅ Required.
- **ViaVersion / ViaBackwards / ViaRewind:** ✅ Full support from 1.18 to 1.21.11.
- **Geyser-Spigot:** ✅ Bedrock players supported.
- **EliteMobs:** ✅ NPCs recognize and react to elite threats.
- **ItemsAdder:** ✅ Seamless custom item integration.
- **ProtocolLib / ProtocolSupport:** ✅ Enhanced packet handling for smooth skin transitions.

---

<div align="center">
<i>Realistic Villagers — Because every village deserves a soul.</i>

[GPL-3.0 License](LICENSE)
</div>
