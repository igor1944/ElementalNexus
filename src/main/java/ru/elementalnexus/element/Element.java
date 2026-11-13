package ru.elementalnexus.element;

import java.util.Locale;

/**
 * Стихии плагина. Каждая стихия имеет английский ID и русское название;
 * оба варианта принимаются в команде /magic combine.
 */
public enum Element {

    FIRE("fire", "огонь"),
    WIND("wind", "ветер"),
    WATER("water", "вода"),
    LIGHTNING("lightning", "молния");

    private final String id;
    private final String russianName;

    Element(String id, String russianName) {
        this.id = id;
        this.russianName = russianName;
    }

    public String getId() {
        return id;
    }

    public String getRussianName() {
        return russianName;
    }

    /**
     * Распознаёт название стихии по английскому ID, английскому имени
     * или русскому названию (регистр не важен).
     *
     * @return стихия или null, если название не распознано
     */
    public static Element fromString(String input) {
        if (input == null) {
            return null;
        }
        String value = input.trim().toLowerCase(Locale.ROOT);
        for (Element element : values()) {
            if (element.id.equals(value)
                    || element.russianName.equals(value)
                    || element.name().toLowerCase(Locale.ROOT).equals(value)) {
                return element;
            }
        }
        return null;
    }

    /**
     * Список всех названий для сообщений об ошибках.
     */
    public static String namesList() {
        StringBuilder builder = new StringBuilder();
        for (Element element : values()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(element.id).append('/').append(element.russianName);
        }
        return builder.toString();
    }
}
