# 🧭 Loadstone – Chunk Loader

**Turn Lodestones into persistent chunk loaders. Keep farms, machines, and redstone active while you’re away.**

---

## 🎯 What This Mod Does

Tired of your farms, machines, or redstone contraptions stopping when you walk away?  

**Loadstone** turns vanilla **Lodestones** into simple, reliable **chunk loaders** — activate a Lodestone with a material and the **targeted chunks** stay loaded so your builds keep running.

---

## ✨ Features

- **Tiered Chunk Loading:** Switch tiers anytime by using different materials.
    - ⚒️ Iron → **1×1** chunk
    - 💎 Diamond → **3×3** chunks
    - 🔥 Netherite → **5×5** chunks (reduced from 7×7 in v1.0 for balance & performance)

- **Persistent Loader State:** Loaders are saved to disk and manage chunks properly.
- **Smart Material System:** Items are consumed on activation and returned on deactivation.
- **Visual Feedback:** Unique particle effects per tier.
- **Real-Time HUD:** Compact overlay showing loader status, tier, and coverage.
- **No Overlaps:** Loaders cannot overlap, preventing multiple loaders from forcing the same chunks.

---



---

## ⚠️ Notes

- Loaders keep areas active **24/7**. Use responsibly to avoid server performance issues.
- **Lodestones only manage chunks they are attached to.** Chunks already loaded by external means (commands, other mods, server tools) are **not automatically owned**. Activating a Lodestone in such a chunk attaches it, allowing the loader to manage it.

---

## 📌 Migration from v1.0 (IMPORTANT)

> ⚠️ **Only relevant if a world previously used Loadstone v1.0.** New worlds do **not** require migration.  
> Skipping migration may leave **ghost-loaded chunks** (chunks forced by Minecraft but not owned by any loader), causing lag or unexpected loader behavior.

- **v1.0 limitation:** Loaders were stored only in memory. The mod **forgot which loaders were active**, while Minecraft could still keep chunks **force-loaded**, producing **ghost-loaded chunks**.
- **v2.0 fix:** Loader state is now **saved and managed properly**. Chunks stay loaded **only while managed by a valid loader**, and unload automatically if the loader becomes **invalid**.
- **Overlap update:** Loaders cannot overlap, preventing conflicting forced chunks.
- **Tier change:** Netherite loaders now cover **5×5** chunks for better balance and performance.

**Migration steps (do this once if you used v1.0):**
1. Run `/forceload remove all` (**cheats/admin required**) to clear old forced chunks.
    - Singleplayer: Pause → *Open to LAN* → enable *Allow Cheats* → run the command (cheats are temporary).
    - Server: Use the server console or ask an admin to run the command.
2. Walk to each Lodestone and activate it with **Iron, Diamond, or Netherite** to register it in v2.0.

> ⚠️ If skipped, leftover forced chunks from v1.0 may remain, causing lag or inconsistent behavior.

---

## 🎮 How to Use

### Activation
1. Place a **Lodestone** anywhere in your world.
2. Hold the material for the tier:
    - ⚒️ **Iron Ingot** → 1×1
    - 💎 **Diamond** → 3×3
    - 🔥 **Netherite Ingot** → 5×5
3. **Right-click** the Lodestone to activate.

### Deactivation / Change Tier
- Right-click an active Lodestone with any item (or empty hand) to deactivate — the activation item is returned.
- Right-click with a different valid material to change tier (old material returned).

---

## 🔗 Links & Credits

- [💝 Support Me](https://ko-fi.com/ilyrac)
- [📦 CurseForge](https://www.curseforge.com/members/ilyrac/projects)
- [📦 Modrinth](https://modrinth.com/user/ilyrac)
- [🐛 Report Issues](https://github.com/IlyRac/Loadstone/issues)
- [🔗 Source](https://github.com/IlyRac/Loadstone)

*Fabric API • IlyRac License*