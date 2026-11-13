# ElementalNexus

Магическая система стихий для серверов **Paper 1.18.2**: мана, заклинания четырёх
стихий и комбинации двух стихий. Все сообщения — на русском языке.

## Структура проекта

```
ElementalNexus/
├── pom.xml
└── src/main/
    ├── java/ru/elementalnexus/
    │   ├── ElementalNexusPlugin.java        — главный класс плагина
    │   ├── command/MagicCommand.java        — /magic + таб-комплит
    │   ├── combo/
    │   │   ├── Combo.java                   — базовый класс комбинации (порядок стихий не важен)
    │   │   ├── ComboManager.java            — реестр и запуск комбинаций
    │   │   └── impl/                        — 6 комбинаций (вихрь, пар, плазма, туман, гроза, разряд)
    │   ├── config/
    │   │   ├── NexusConfig.java             — загрузка и валидация config.yml, сообщения
    │   │   └── AbilitySettings.java         — настройки заклинания/комбинации
    │   ├── element/Element.java             — FIRE / WIND / WATER / LIGHTNING (ru+en названия)
    │   ├── event/                           — PlayerManaChangeEvent, SpellCastEvent,
    │   │                                        ElementCombinationEvent (все отменяемые)
    │   ├── listener/                        — ManaListener (вход/выход), ProjectileListener
    │   ├── mana/
    │   │   ├── ManaManager.java             — мана, players.yml, задачи регенерации/сохранения
    │   │   └── ManaChangeReason.java
    │   ├── spell/
    │   │   ├── Spell.java                   — базовый класс заклинания
    │   │   ├── SpellManager.java            — реестр и запуск заклинаний
    │   │   └── impl/                        — огненный снаряд, порыв ветра, водяная волна,
    │   │                                        удар молнии
    │   └── util/                            — TargetUtil (поиск целей), FormatUtil
    └── resources/
        ├── plugin.yml                       — api-version: 1.18, команда /magic, permissions
        └── config.yml                       — мана, заклинания, комбинации, сообщения (RU)
```

## Сборка и установка

Требуется **JDK 17+** и **Maven**.

```bash
mvn clean package
```

Готовый файл `target/ElementalNexus-1.0.0.jar` положите в папку `plugins/`
сервера Paper 1.18.2 и перезапустите сервер. При первом запуске создадутся
`plugins/ElementalNexus/config.yml` (настройки) и `players.yml` (данные маны).

## Команды (`/magic`)

| Команда | Описание | Право |
|---|---|---|
| `/magic` | Справка | — |
| `/magic mana` | Текущая и максимальная мана | `elementalnexus.use` |
| `/magic spells` | Список заклинаний и комбинаций | `elementalnexus.use` |
| `/magic cast <id>` | Использовать заклинание | `elementalnexus.use` (только игрок) |
| `/magic combine <ст1> <ст2>` | Комбинация, напр. `fire wind` или `огонь ветер` | `elementalnexus.use` (только игрок) |
| `/magic reload` | Перезагрузка конфигурации | `elementalnexus.admin` |
| `/magic setmana <игрок> <n>` | Установить ману | `elementalnexus.admin` |
| `/magic addmana <игрок> <n>` | Добавить/отнять ману | `elementalnexus.admin` |

## Заклинания

- **fireball** (FIRE) — огненный снаряд (без поджога блоков и разрушений);
- **wind_gust** (WIND) — порыв ветра, отбрасывающий сущности;
- **water_wave** (WATER) — водяная волна: замедление + отбрасывание;
- **lightning_strike** (LIGHTNING) — визуальная молния по цели + ручной урон.

## Комбинации (порядок стихий не важен)

- `fire + wind` — огненный вихрь (урон по области);
- `fire + water` — паровой взрыв (отбрасывание + слабость);
- `fire + lightning` — плазменный удар;
- `wind + water` — ледяной туман (замедление);
- `wind + lightning` — грозовой порыв;
- `water + lightning` — цепной разряд по нескольким целям.

## События API

`PlayerManaChangeEvent`, `SpellCastEvent`, `ElementCombinationEvent` — отменяемые,
вызываются в основном потоке сервера; у отменённого заклинания не тратится мана
и не запускается перезарядка.
