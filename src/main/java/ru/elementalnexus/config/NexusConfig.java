package ru.elementalnexus.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import ru.elementalnexus.ElementalNexusPlugin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Загружает и проверяет config.yml, хранит настройки маны,
 * параметры заклинаний/комбинаций и все сообщения.
 *
 * При некорректных значениях выводит понятное предупреждение в консоль
 * и использует значение по умолчанию.
 */
public class NexusConfig {

    private static final double DEFAULT_MANA_COST = 10.0;
    private static final double DEFAULT_COOLDOWN = 5.0;

    private final ElementalNexusPlugin plugin;

    private double maxMana;
    private double initialMana;
    private double regenPerSecond;
    private long saveIntervalSeconds;

    private final Map<String, AbilitySettings> spellSettings = new HashMap<>();
    private final Map<String, AbilitySettings> comboSettings = new HashMap<>();
    private final Map<String, String> messages = new HashMap<>();

    public NexusConfig(ElementalNexusPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Загружает (или перезагружает) config.yml.
     *
     * @param spellIds известные ID заклинаний
     * @param comboIds известные ID комбинаций
     */
    public void load(Collection<String> spellIds, Collection<String> comboIds) {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        maxMana = positiveDouble(config, "mana.max", 100.0);
        initialMana = nonNegativeDouble(config, "mana.initial", 100.0);
        if (initialMana > maxMana) {
            warn("Значение 'mana.initial' (" + initialMana + ") больше 'mana.max' (" + maxMana
                    + "). Начальная мана будет равна максимуму.");
            initialMana = maxMana;
        }
        regenPerSecond = positiveDouble(config, "mana.regen-per-second", 2.0);
        saveIntervalSeconds = (long) positiveDouble(config, "save-interval-seconds", 300.0);

        loadMessages(config);
        loadAbilities(config, "spells", spellIds, spellSettings);
        loadAbilities(config, "combos", comboIds, comboSettings);

        plugin.getLogger().info("Конфигурация загружена: заклинаний — " + spellSettings.size()
                + ", комбинаций — " + comboSettings.size() + ".");
    }

    private void loadAbilities(FileConfiguration config, String rootSection, Collection<String> ids,
                               Map<String, AbilitySettings> target) {
        target.clear();
        for (String id : ids) {
            String base = rootSection + "." + id;
            ConfigurationSection section = config.getConfigurationSection(base);
            if (section == null) {
                warn("Раздел '" + base + "' не найден в config.yml."
                        + " Используются значения по умолчанию (стоимость " + DEFAULT_MANA_COST
                        + " маны, перезарядка " + DEFAULT_COOLDOWN + " сек.).");
                target.put(id, new AbilitySettings(id, id, DEFAULT_MANA_COST, DEFAULT_COOLDOWN,
                        Collections.emptyMap()));
                continue;
            }

            String name = section.getString("name", id);
            double manaCost = positiveDouble(config, base + ".mana-cost", DEFAULT_MANA_COST);
            double cooldown = positiveDouble(config, base + ".cooldown-seconds", DEFAULT_COOLDOWN);

            Map<String, Double> parameters = new HashMap<>();
            for (String key : section.getKeys(false)) {
                if (key.equals("name") || key.equals("mana-cost") || key.equals("cooldown-seconds")) {
                    continue;
                }
                double value = section.getDouble(key, Double.NaN);
                if (Double.isNaN(value)) {
                    warn("Значение '" + base + "." + key + "' не является числом — параметр проигнорирован.");
                    continue;
                }
                parameters.put(key, value);
            }
            target.put(id, new AbilitySettings(id, name, manaCost, cooldown, parameters));
        }
    }

    private void loadMessages(FileConfiguration config) {
        messages.clear();
        String prefix = colorize(config.getString("messages.prefix", "&6[ElementalNexus] &r"));
        for (Map.Entry<String, String> entry : defaultMessages().entrySet()) {
            String key = entry.getKey();
            String value = config.getString("messages." + key);
            if (value == null) {
                warn("Сообщение 'messages." + key + "' не найдено в config.yml. Используется текст по умолчанию.");
                value = entry.getValue();
            }
            messages.put(key, colorize(value).replace("%prefix%", prefix));
        }
    }

    /**
     * Сообщение по ключу с подстановкой плейсхолдеров %имя%.
     * Плейсхолдеры передаются парами: имя, значение, имя, значение...
     */
    public String msg(String key, String... replacements) {
        String message = messages.get(key);
        if (message == null) {
            return key;
        }
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            message = message.replace("%" + replacements[i] + "%", replacements[i + 1]);
        }
        return message;
    }

    private double positiveDouble(FileConfiguration config, String path, double defaultValue) {
        double value = config.getDouble(path, defaultValue);
        if (value <= 0 || Double.isNaN(value) || Double.isInfinite(value)) {
            warn("Значение '" + path + "' должно быть положительным числом (сейчас: "
                    + config.getString(path) + "). Используется значение по умолчанию: " + defaultValue);
            return defaultValue;
        }
        return value;
    }

    private double nonNegativeDouble(FileConfiguration config, String path, double defaultValue) {
        double value = config.getDouble(path, defaultValue);
        if (value < 0 || Double.isNaN(value) || Double.isInfinite(value)) {
            warn("Значение '" + path + "' должно быть неотрицательным числом (сейчас: "
                    + config.getString(path) + "). Используется значение по умолчанию: " + defaultValue);
            return defaultValue;
        }
        return value;
    }

    private static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private void warn(String message) {
        plugin.getLogger().warning("[ElementalNexus] Конфигурация: " + message);
    }

    public double getMaxMana() {
        return maxMana;
    }

    public double getInitialMana() {
        return initialMana;
    }

    public double getRegenPerSecond() {
        return regenPerSecond;
    }

    public long getSaveIntervalSeconds() {
        return saveIntervalSeconds;
    }

    public AbilitySettings getSpellSettings(String id) {
        return spellSettings.get(id);
    }

    public AbilitySettings getComboSettings(String id) {
        return comboSettings.get(id);
    }

    public Set<String> getSpellIds() {
        return spellSettings.keySet();
    }

    public Set<String> getComboIds() {
        return comboSettings.keySet();
    }

    private static Map<String, String> defaultMessages() {
        Map<String, String> defaults = new LinkedHashMap<>();
        defaults.put("prefix", "&6[ElementalNexus] &r");
        defaults.put("no-permission", "%prefix%&cУ вас нет прав для использования этой команды.");
        defaults.put("players-only", "%prefix%&cЭта команда доступна только игрокам.");
        defaults.put("unknown-subcommand", "%prefix%&cНеизвестная подкоманда. Введите &e/magic&c для справки.");
        defaults.put("unknown-spell", "%prefix%&cНеизвестное заклинание: &e%spell%&c. Список заклинаний: &e/magic spells");
        defaults.put("unknown-element", "%prefix%&cНеизвестная стихия: &e%element%&c. Доступные: fire/огонь, wind/ветер, water/вода, lightning/молния.");
        defaults.put("same-elements", "%prefix%&cДля комбинации нужно выбрать две разные стихии.");
        defaults.put("combination-not-found", "%prefix%&cДля стихий %first% и %second% комбинация не найдена.");
        defaults.put("mana-info", "%prefix%&bВаша мана: &f%current%&7/&f%max%");
        defaults.put("not-enough-mana", "%prefix%&cНедостаточно маны! Требуется: &e%cost%&c, у вас: &e%current%&c.");
        defaults.put("spell-on-cooldown", "%prefix%&e%spell%&e перезаряжается ещё &6%time%&e сек.");
        defaults.put("spell-cancelled", "%prefix%&cИспользование заклинания было отменено.");
        defaults.put("cast-error", "%prefix%&cПроизошла ошибка при использовании заклинания. Сообщите администрации сервера.");
        defaults.put("reloaded", "%prefix%&aКонфигурация успешно перезагружена.");
        defaults.put("player-not-found", "%prefix%&cИгрок &e%player%&c не найден в сети.");
        defaults.put("invalid-number", "%prefix%&cНекорректное число: &e%value%&c.");
        defaults.put("mana-set", "%prefix%&aМана игрока &e%player%&a установлена на &b%mana%&a.");
        defaults.put("mana-added", "%prefix%&aМана игрока &e%player%&a изменена на &b%amount%&a, теперь: &b%mana%&a.");
        defaults.put("target-mana-set", "%prefix%&eВаша мана установлена на &b%mana%&e.");
        defaults.put("target-mana-added", "%prefix%&eВаша мана изменена на &b%amount%&e, теперь: &b%mana%&e.");
        defaults.put("usage-cast", "%prefix%&eИспользование: &6/magic cast <заклинание>");
        defaults.put("usage-combine", "%prefix%&eИспользование: &6/magic combine <стихия1> <стихия2>&e, например: /magic combine fire wind");
        defaults.put("usage-setmana", "%prefix%&eИспользование: &6/magic setmana <игрок> <количество>");
        defaults.put("usage-addmana", "%prefix%&eИспользование: &6/magic addmana <игрок> <количество>");
        defaults.put("help-header", "&6========== &eElementalNexus &6==========");
        defaults.put("help-magic", "&e/magic &7— показать эту справку");
        defaults.put("help-mana", "&e/magic mana &7— показать вашу ману");
        defaults.put("help-spells", "&e/magic spells &7— список заклинаний и комбинаций");
        defaults.put("help-cast", "&e/magic cast <заклинание> &7— использовать заклинание");
        defaults.put("help-combine", "&e/magic combine <стихия1> <стихия2> &7— комбинировать стихии (например: fire wind)");
        defaults.put("help-reload", "&e/magic reload &7— перезагрузить конфигурацию &8(только для админов)");
        defaults.put("help-setmana", "&e/magic setmana <игрок> <количество> &7— установить ману &8(только для админов)");
        defaults.put("help-addmana", "&e/magic addmana <игрок> <количество> &7— добавить ману &8(только для админов)");
        defaults.put("spells-header", "&6=== &eЗаклинания &6===");
        defaults.put("spells-entry", "&e%id% &7(%name%&7) &8— &b%cost% маны&7, перезарядка &b%cooldown%с");
        defaults.put("combos-header", "&6=== &eКомбинации стихий &6===");
        defaults.put("combos-entry", "&e%first% + %second% &8→ &e%id% &7(%name%&7) &8— &b%cost% маны&7, перезарядка &b%cooldown%с");
        return defaults;
    }
}
