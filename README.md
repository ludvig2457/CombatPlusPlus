# Combat++

Боевые системы для Paper/Folia: **1.8.9**, **1.12** и **1.21.11**.

## Возможности

- `/combat 1.8.9` — отключён attack cooldown, отключены sweep и щиты, блокировка мечом, кнокбек и W-tap как в 1.8.
- `/combat 1.12` — modern cooldown, без sweep, щиты включены.
- `/combat 1.21.11` — ванильный современный бой.
- Combat tag и комбо публикуются через metadata для HUD, сайдбара, китов и spawn.
- Не требует других плагинов; работает как самостоятельный combat-модуль.

## Команды

| Команда | Описание |
|---|---|
| `/combat` / `/combat info` | Текущий режим, тег и комбо |
| `/combat 1.8.9\|1.12\|1.21.11` | Сменить режим |
| `/combat reload` | Перезагрузить конфиг |

## Права

- `combatplus.use` — просмотр режима (по умолчанию всем).
- `combatplus.admin` — смена режима и reload (по умолчанию OP).

## Конфиг

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

## Версии и платформы

- Paper/Folia: `1.16.5`, `1.17.1`, `1.18.2`, `1.19.4`, `1.20.6`, `1.21.1`, `1.21.4`, `1.21.8`, `1.21.11`.
- Jar под каждую версию: `./gradlew build -PmcVersion=1.20.6-R0.1-SNAPSHOT`.
- Spigot не поддерживается текущей Paper-сборкой.
- 1.8.9 — режим боя для сервера 1.16.5+; не сервер 1.8.9.

## Сборка

```bash
./gradlew build
```

Jar: `build/libs/CombatPlusPlus-1.0.0.jar`.

Лицензия: MIT.
