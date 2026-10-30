# Combat++

Combat systems for Paper/Folia: **1.8.9**, **1.12**, and **1.21.11**.

## Features

- `/combat 1.8.9` — attack cooldown disabled, sweep and shields disabled, sword blocking, knockback, and W-tap as in 1.8.
- `/combat 1.12` — modern cooldown, no sweep, shields enabled.
- `/combat 1.21.11` — vanilla modern combat.
- Combat tags and combos are published via metadata for the HUD, sidebar, kits, and spawn.
- Does not require other plugins; works as a standalone combat module.

## Commands

| Command | Description |
|---|---|
| `/combat` / `/combat info` | Current mode, tag, and combo |
| `/combat 1.8.9\|1.12\|1.21.11` | Change mode |
| `/combat reload` | Reload config |

## Permissions

- `combatplus.use` — view mode (default for everyone).
- `combatplus.admin` — change mode and reload (default for OP).

## Config

```yaml
settings: 
combat: 
mode: "1.21.11" 
tag-seconds: 15 
combo-seconds: 3 
legacy: 
blocking-damage-multiplier: 0.5 
blocking-damage-flat: 0.5 
blocking-speed-multiplier: 0.2 
knockback-horizontal: 0.4 
knockback-sprint-horizontal: 0.9 
knockback-vertical: 0.4
```

## Versions and platforms

- Paper/Folia: `1.16.5`, `1.17.1`, `1.18.2`, `1.19.4`, `1.20.6`, `1.21.1`, `1.21.4`, `1.21.8`, `1.21.11`.
- Jar for each version: `./gradlew build -PmcVersion=1.20.6-R0.1-SNAPSHOT`.
- Spigot is not supported by the current Paper build.
- 1.8.9 — combat mode for server 1.16.5+; not server 1.8.9.

## Build

```bash
./gradlew build
```

Jar: `build/libs/CombatPlusPlus-1.0.0.jar`.

License: MIT.
