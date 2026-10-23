# Create: Interval Filter

A small addon for [Create](https://modrinth.com/mod/create) that adds the **Interval Filter** — a filter item that configures a Deployer to
activate at set distance intervals as a contraption moves.

<p style="text-align:center;">
  <img src=".github/images/icon.png" alt="Create: Interval Filter">
</p>

<p style="text-align:center;">
  <!-- a href="https://modrinth.com/mod/create-interval-filter"><img src="https://img.shields.io/modrinth/dt/create-interval-filter?logo=modrinth&label=Modrinth&color=00af5c" alt="Modrinth"></a -->
  <!-- a href="https://www.curseforge.com/minecraft/mc-mods/create-interval-filter"><img src="https://img.shields.io/curseforge/dt/1281770?logo=curseforge&label=CurseForge&color=f16436" alt="CurseForge"></a-->
  <a href="https://github.com/Creators-of-Create/Create"><img src="https://img.shields.io/badge/Create-6.0.7%2B-4b8bc2" alt="Create"></a>
  <a href="https://neoforged.net"><img src="https://img.shields.io/badge/NeoForge-21.1.181%2B-d88231" alt="NeoForged"></a>
  <a href="https://files.minecraftforge.net"><img src="https://img.shields.io/badge/Forge-47.3.30%2B-26303d" alt="Forge"></a>
  <a href="https://fabricmc.net"><img src="https://img.shields.io/badge/Fabric-0.19.2%2B-dbd0a7" alt="Fabric"></a>
</p>

---

## What does it do?

Building automated contraptions in Create often requires placing items at precise, repeating intervals — something
vanilla Deployers can't do on their own. The Interval Filter solves this.
Place it in a Deployer's filter slot and it controls the cadence of activations: place items every 3rd block, every
10th block, or any interval up to 64 — with an optional offset to phase-shift where the pattern starts.

This opens up builds that were previously tedious or impossible to tune, such as:

- Evenly-spaced torch placement behind a mining drill
- Repeating decoration patterns on a wall or floor
- Skipping the first few blocks before a pattern begins
- Alternating between different items placed in sequence or at random

<table style="text-align:center;">
  <tr>
    <td><b>Before</b></td>
    <td><b>After</b></td>
</tr>         
  <tr>
    <td><a href=".github/images/before.png"><img src=".github/images/before.png" alt="Before: a mining tunnel lit with with torches placed every block"></a></td>                                                                                                                                                           
    <td><a href=".github/images/after.png"><img src=".github/images/after.png" alt="After: the same tunnel fully lit with evenly spaced torches"></a></td>
  </tr>                                                                                                                                                                                                                                                                                                          
</table>  

---

## Features

- **Block Interval** — Controls how many blocks the contraption travels between each Deployer activation. Supports
  values from 1 to 64.

- **Initial Offset** — Delays the start of the pattern by a set number of blocks. Useful for lining up placements
  without needing to redesign or reposition contraptions.

- **Execution Modes** — Two modes control what happens when a placement fails at the intended position:
    - **Strict** — Skips the activation and continues the pattern unchanged. Use this when exact positioning matters.
    - **Best Effort** — Keeps retrying on subsequent blocks until a placement succeeds. Use this when gaps are
      unacceptable.

- **Placement Mode** — Two modes control what happens when multiple items (up to 6) are configured:
    - **Sequential** — Cycles through all items in order each time the Deployer activates
    - **Random** — Selects one item at random for each activation.

<table style="text-align:center;">
  <tr>
    <td><b>Single Item</b></td>
    <td><b>Multiple Items</b></td>
  </tr>
  <tr>
    <td><a href=".github/images/configure_1.png"><img src=".github/images/configure_1.png" alt="Configuring the filter to place a lantern every 4 blocks"></a></td>                                                                                                                                                           
    <td><a href=".github/images/configure_2.png"><img src=".github/images/configure_2.png" alt="Configuring the filter to place each torch in sequence every 2 blocks"></a></td>
  </tr>                                                                                                                                                                                                                                                                                                          
  <tr>
    <td><a href=".github/images/sequence_1.png"><img src=".github/images/sequence_1.png" alt="Building a Cut Limestone Wall with Lanterns every 4 blocks"></a></td>                                                                                                                                                           
    <td><a href=".github/images/sequence_2.png"><img src=".github/images/sequence_2.png" alt="Deploying sequential torches evenly spaced every 2 blocks"></a></td>
  </tr>                                                                                                                                                                                                                                                                                                          
</table>  

---

## Crafting

Craft any wool block with 2× andesite alloy to create the Interval Filter.

![recipe](.github/images/recipe.png)

Crafting a configured Interval Filter resets all stored settings back to defaults.

![recipe_clear](.github/images/recipe_clear.png)

---

## Usage

1. Craft the Interval Filter.
2. Right-click it to open the configuration screen.
3. Set the desired Block Interval and optionally an Initial Offset.
4. Add up to 6 items to the filter slots. Choose Strict or Best Effort mode depending on whether missed placements
   should be skipped or retried...and choose Sequential or Random if using multiple items.
5. Place the configured Interval Filter into the filter slot of a Deployer on your contraption.

> **Note:** The Interval Filter only works in a Deployer's filter slot on moving contraptions. It cannot be used in
> other filter slots.

---

## Notes

- **Train Relocation** — Using a Wrench to pick up and relocate a train resets the interval counter, offset, and
  sequence order. Regular stops (such as stations) do not reset progress.
- **Teleporting** — Teleporting or relocating a contraption restarts the pattern rather than counting as distance
  traveled. Trains keep their pattern through portals.
- **Reversing** — Nothing is placed while a contraption travels in reverse, but the pattern is remembered and
  continues unbroken when it moves forward again. Forward is the front of a train, or the first direction other
  contraptions move.
- The filter can be reconfigured at any time by right-clicking it in your hand. Placing it back into the Deployer
  applies the updated settings immediately.

---

## FAQ

**Does the Interval Filter work on non-train contraptions?**
Yes — it works on any moving contraption that includes a Deployer, such as those driven by a Minecraft w/Furnace,
Mechanical Bearing or Contraption Controller.

**What happens if the Deployer's item supply runs out mid-pattern?**
The Deployer behaves as normal — it simply won't activate until items are available again. The interval counter
continues to advance.

**Can I use multiple Interval Filters on the same contraption?**
Yes. Each Deployer tracks its own interval state independently.
