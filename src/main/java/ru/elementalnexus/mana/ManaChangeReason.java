package ru.elementalnexus.mana;

/**
 * Причина изменения маны, передаётся в PlayerManaChangeEvent.
 */
public enum ManaChangeReason {

    /** Естественное восстановление маны. */
    REGENERATION,

    /** Трата маны на обычное заклинание. */
    SPELL_USE,

    /** Трата маны на комбинированное заклинание. */
    COMBINATION_USE,

    /** Изменение командой (/magic setmana, /magic addmana). */
    COMMAND,

    /** Любая другая (внутренняя) причина. */
    OTHER
}
