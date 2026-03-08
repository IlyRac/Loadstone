<!-- Centered Ko-fi banner + platform buttons -->
<p align="center">
  <a href="https://ko-fi.com/ilyrac" target="_blank">
    <img src="https://cdn.modrinth.com/data/cached_images/f69e87c0cfd3306fa11c3da477e51544c8d380e2_0.webp" alt="Support Me" style="margin-bottom: 10px;">
  </a>
  <a href="https://www.curseforge.com/members/ilyrac/projects" target="_blank">
    <img src="https://i.imgur.com/rpoyjVG.png" alt="CurseForge" style="margin: 0 5px;">
  </a>
  <a href="https://modrinth.com/user/ilyrac" target="_blank">
    <img src="https://i.imgur.com/7fioiRx.png" alt="Modrinth" style="margin: 0 5px;">
  </a>
  <a href="https://github.com/IlyRac/FarBlockEntityRendering" target="_blank">
    <img src="https://i.imgur.com/EYNx6oL.png" alt="GitHub" style="margin: 0 5px;">
  </a>
</p>

<hr>

<!-- Mod title -->
<h1>🧭 Loadstone – Chunk Loader</h1>

<p><strong>Turn vanilla Lodestones into reliable, persistent, tiered chunk loaders to keep farms, machines, and redstone active while you’re away.</strong></p>

<img src="https://cdn.modrinth.com/data/cached_images/76fb377ac98223ec58a1257021e11cb4f1198da6.gif" alt="Interacting" style="max-width:100%;">

<hr>

<!-- What This Mod Does -->
<details>
  <summary>🎯 What This Mod Does</summary>
  <p>Tired of your farms, machines, or redstone contraptions stopping when you walk away?</p>
  <p><strong>Loadstone</strong> turns vanilla <strong>Lodestones</strong> into simple, reliable <strong>chunk loaders</strong> — activate a Lodestone with a material and the <strong>targeted chunks</strong> stay loaded, so everything in range continues running automatically.</p>
</details>

<!-- Features -->
<details>
  <summary>✨ Features</summary>
  <ul>
    <li><strong>Tiered Chunk Loading:</strong> Switch tiers anytime by using different materials.
      <ul>
        <li>⚒️ Iron → <strong>1×1</strong> chunk</li>
        <li>💎 Diamond → <strong>3×3</strong> chunks</li>
        <li>🔥 Netherite → <strong>5×5</strong> chunks</li>
      </ul>
    </li>
    <li><strong>Persistent Loader State:</strong> Loaders are saved to disk and manage chunks properly.</li>
    <li><strong>Smart Material System:</strong> Items are consumed on activation and returned on deactivation.</li>
    <li><strong>Visual Feedback:</strong> Unique particle effects per tier.</li>
    <li><strong>Real-Time HUD:</strong> Compact overlay showing loader status, tier, and coverage.</li>
    <li><strong>No Overlaps:</strong> Loaders cannot overlap, preventing multiple loaders from forcing the same chunks.</li>
  </ul>
</details>

<!-- How to Use -->
<details>
  <summary>🎮 How to Use</summary>
  <h3>Activation</h3>
  <ol>
    <li>Place a <strong>Lodestone</strong> anywhere in your world.</li>
    <li>Hold the material for the tier:
      <ul>
        <li>⚒️ <strong>Iron Ingot</strong> → 1×1</li>
        <li>💎 <strong>Diamond</strong> → 3×3</li>
        <li>🔥 <strong>Netherite Ingot</strong> → 5×5</li>
      </ul>
    </li>
    <li><strong>Right-click</strong> the Lodestone to activate.</li>
  </ol>

  <h3>Deactivation / Change Tier</h3>
  <ul>
    <li>Right-click an active Lodestone with empty hand to deactivate — the activation item is returned.</li>
    <li>Right-click with a different valid material to change tier (old material returned).</li>
  </ul>
</details>

<!-- Gallery -->
<details>
  <summary>🖼️ Gallery</summary>
  <img src="https://cdn.modrinth.com/data/cached_images/a3747ff5ca50ecd676677b55cbbbbfaed73e4ab3.png" alt="HUD" style="max-width:100%; margin-bottom:10px;">
  <img src="https://cdn.modrinth.com/data/cached_images/f5ff46311dc6113f7ee98bbb6741aeb419626ccd.png" alt="Tiers" style="max-width:100%;">
</details>

<!-- Migration -->
<details>
  <summary>✈️ Migration from v1.0 !!! ⚠️ VERY IMPORTANT ⚠️ !!!</summary>
  <p><strong>🚩 Only required for worlds that previously used Loadstone v1.0.</strong></p>

  <ul>
    <li><strong>Loadstone v1.0</strong> stored loaders only in memory. Because of this, the mod could lose track of active loaders while Minecraft continued keeping the chunks force-loaded.
      This may create <strong>ghost-loaded chunks</strong> that keep running and may cause lag.</li>
  </ul>

  <p><strong>🔧 One-time Fix (for each world that used v1.0):</strong></p>
  <ol>
    <li>Run <code>/forceload remove all</code> (<strong>cheats/admin required</strong>) to clear old forced chunks.
      <ul>
        <li>Singleplayer: Pause → <em>Open to LAN</em> → enable <em>Allow Cheats</em> → run the command (cheats are temporary).</li>
        <li>Server: Use the server console or ask an admin to run the command.</li>
      </ul>
    </li>
    <li>Reactivate your loaders to <strong>register them in Loadstone v2.0</strong>.</li>
  </ol>

  <p>🚩 Skipping migration may leave <strong>ghost-loaded chunks</strong> active in your world.</p>
</details>

<hr>

<!-- Notes -->
<h2>📌 Notes</h2>
<ul>
  <li>Loaders keep areas active <strong>24/7</strong>. Use responsibly to avoid performance issues.</li>
  <li>Using <strong>Loadstone v1.0</strong> may result in unexpected behavior or performance issues. Upgrading or using <strong>Loadstone  v2.0</strong> is recommended for a stable and consistent experience.</li>
  <li><strong>Lodestones only manage chunks they are attached to.</strong> Chunks already loaded by external means (commands, other mods, server tools) are <strong>not automatically owned</strong>. Activating a Lodestone in such a chunk attaches it, allowing the loader to manage it.</li>
</ul>
