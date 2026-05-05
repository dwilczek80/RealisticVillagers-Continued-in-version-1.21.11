# 🏘️ RealisticVillagers 

![License](https://img.shields.io/github/license/dwilczek80/RealisticVillagers-Continued-in-version-1.21.11?style=for-the-badge)
![Version](https://img.shields.io/badge/Version-3.3.6.7-blue?style=for-the-badge)
![Minecraft](https://img.shields.io/badge/Minecraft-1.18%20--%201.21.1-green?style=for-the-badge)

Forget about those boring, silent villagers! **RealisticVillagers** transforms your Minecraft world by replacing vanilla villagers with interactive, human-like NPCs. They have names, genders, skins, families, and a complex social system that makes your villages feel truly alive.

---

## ✨ Key Features

### 👨‍👩‍👧‍👦 Complex Social System
*   **Family Trees:** Villagers have parents, children, and partners. 
*   **Marriage & Divorce:** Witness villagers getting married (using Wedding Rings) or going through messy divorces.
*   **Procreation:** Villagers can have children that inherit traits from their parents.
*   **Auto-Assign Family:** New villagers can be automatically assigned to nearby families to populate your world naturally.

### 🎭 Realistic Interactions
*   **Chat System:** Interact with villagers through a custom GUI. Talk, joke, flirt, or even insult them!
*   **Reputation Matters:** Your actions affect how villagers perceive you. High reputation unlocks better trades and special interactions.
*   **Dynamic Skins:** Every villager has a unique human skin, with support for **MineSkin** to fetch thousands of variations.
*   **Gender System:** Both villagers and players can have defined genders (Male/Female), affecting interactions and family roles.

### 🛡️ Combat & AI Enhancements
*   **Self Defense:** Villagers can defend themselves or their family members when attacked.
*   **Following & Staying:** Ask your favorite villager to follow you on an adventure or stay at a specific location.
*   **Looting:** Some villagers (like Nitwits) might try to "borrow" items from nearby chests!
*   **Revive System:** Don't let your favorite NPC stay dead! Use a special ritual to bring them back to life.

---

## 🛠️ Installation

1.  Download the latest `RealisticVillagers.jar`.
2.  Install the required dependency: **[PacketEvents](https://github.com/retrooper/packetevents)**.
3.  (Optional but Recommended) Install **[ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)** for better packet handling.
4.  Drop the plugin into your `plugins` folder.
5.  Restart your server and enjoy!

---

## 💻 Commands

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/rv reload` | Reloads the configuration and messages. | `realisticvillagers.admin` |
| `/rv skins` | Opens the skin management GUI. | `realisticvillagers.admin` |
| `/gender <male\|female>` | Set your character's gender (Mandatory to play). | *None* |
| `/rv genderset <player> <gender>` | Forcibly set a player's gender. | `realisticvillagers.genderset` |

---

## ⚙️ Configuration Snippets

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
RealisticVillagers is designed to work seamlessly with:
*   **EliteMobs:** NPCs correctly interact with custom mobs.
*   **AuthMe / Login Plugins:** The gender selection system is designed not to interfere with login processes.
*   **ItemsAdder:** Custom item support for rings and whistles.
*   **ViaVersion:** Multi-version support from 1.18 up to 1.21.1.

---

## 📜 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

*Developed with ❤️ to make Minecraft villages feel like home.*
