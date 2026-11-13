package ru.elementalnexus.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Иммутабельные настройки одного заклинания или одной комбинации,
 * загруженные (и проверенные) из config.yml.
 */
public final class AbilitySettings {

    private final String id;
    private final String displayName;
    private final double manaCost;
    private final double cooldownSeconds;
    private final Map<String, Double> parameters;

    public AbilitySettings(String id, String displayName, double manaCost, double cooldownSeconds,
                           Map<String, Double> parameters) {
        this.id = id;
        this.displayName = displayName;
        this.manaCost = manaCost;
        this.cooldownSeconds = cooldownSeconds;
        this.parameters = Collections.unmodifiableMap(new HashMap<>(parameters));
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getManaCost() {
        return manaCost;
    }

    public double getCooldownSeconds() {
        return cooldownSeconds;
    }

    public Map<String, Double> getParameters() {
        return parameters;
    }

    /**
     * Числовой параметр заклинания (урон, радиус и т.д.).
     *
     * @param key ключ параметра
     * @param defaultValue значение по умолчанию, если параметр отсутствует
     */
    public double getDouble(String key, double defaultValue) {
        Double value = parameters.get(key);
        return value != null ? value : defaultValue;
    }
}
