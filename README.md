<div align="center">

# 🏘️ Realistic Villagers
**Transform your static villages into a living, breathing civilization.**

---

[ ✨ Features ](#-features) • [ 🚀 Installation ](#-installation) • [ 💻 Commands ](#-commands) • [ 🤝 Compatibility ](#-compatibility)

</div>

## 📖 The Vision
Forget the days of "Hmmmm" and blank stares. **Realistic Villagers** is the ultimate overhaul for Minecraft villagers. By replacing standard entities with dynamic, skin-aware NPCs, we bring a level of depth and immersion previously unseen. Whether it's building a family legacy, managing complex trades, or defending your home, every villager has a story.

---

## ✨ Features

### 🧬 The Human Factor
*   **Dynamic Identity:** Every NPC is uniquely gendered (Male/Female) with a personality-driven skin.
*   **Professional Names:** No more "Villager #432". Meet *John the Farmer* or *Raquel the Cleric*.
*   **Family Legacies:** A deep genealogy system. NPCs remember their parents, children, and spouses.
*   **Marriage & Procreation:** Use Wedding Rings to form bonds. Watch families grow over generations.

### 🧠 Advanced Artificial Intelligence
*   **Self-Defense:** NPCs aren't helpless. They will fight back or call their family for help.
*   **Social Interaction:** Talk, joke, flirt, or insult. Your reputation changes based on every word.
*   **Interactive Needs:** Villagers have hunger, inventory needs, and even specific activities like looting or fishing.
*   **Revive Rituals:** Lose a beloved villager? Bring them back with a sacred cross and a midnight ritual.

### 🎨 Visual & Technical Excellence
*   **MineSkin Integration:** Fetches high-quality skins automatically.
*   **Trade Level-up Visuals:** Real-time nametag updates as your villagers grow in experience.
*   **Optimized Performance:** Built on **PacketEvents** for minimal server impact and high stability.

---

## 🚀 Installation

1.  **Dependencies:** Ensure you have **[PacketEvents](https://github.com/retrooper/packetevents)** installed.
2.  **Plugin:** Drop `RealisticVillagers.jar` into your `/plugins/` folder.
3.  **Setup:** Restart the server to generate the default configuration and skin cache.
4.  **Go!** Witness the transformation of every village in your world.

---

## 💻 Commands

| Command | Description |
| :--- | :--- |
| `/gender <male\|female>` | **Mandatory** setup for players to interact with the world. |
| `/rv reload` | Hot-reload all configurations and messages. |
| `/rv skins` | Manage and preview NPC skin categories. |
| `/rv genderset <p> <g>` | Admin command to override player gender. |

---

## ⚙️ Configuration

The plugin is highly customizable. You can toggle almost every feature in `config.yml`:

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

## 🤝 Compatibility
We play nice with others:
*   **EliteMobs:** NPCs recognize and react to elite threats.
*   **ProtocolLib:** Enhanced packet handling for smooth skin transitions.
*   **ViaVersion:** Full support from 1.18 to 1.21.1.
*   **ItemsAdder:** Seamless custom item integration.

---

<div align="center">
<i>Realistic Villagers — Because every village deserves a soul.</i>

[GPL-3.0 License]
</div>
