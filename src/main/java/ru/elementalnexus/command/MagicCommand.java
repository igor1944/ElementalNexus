package ru.elementalnexus.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.elementalnexus.ElementalNexusPlugin;
import ru.elementalnexus.combo.Combo;
import ru.elementalnexus.config.AbilitySettings;
import ru.elementalnexus.config.NexusConfig;
import ru.elementalnexus.element.Element;
import ru.elementalnexus.mana.ManaChangeReason;
import ru.elementalnexus.spell.Spell;
import ru.elementalnexus.util.FormatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Обработчик команды /magic и таб-комплит.
 *
 * Подкоманды: mana, spells, cast, combine, reload, setmana, addmana.
 * reload/setmana/addmana доступны только с правом elementalnexus.admin,
 * команды магии требуют elementalnexus.use и выполняются только игроком.
 */
public class MagicCommand implements CommandExecutor, TabCompleter {

    private static final List<String> BASE_SUBCOMMANDS =
            Arrays.asList("help", "mana", "spells", "cast", "combine");
    private static final List<String> ADMIN_SUBCOMMANDS =
            Arrays.asList("reload", "setmana", "addmana");

    private final ElementalNexusPlugin plugin;

    public MagicCommand(ElementalNexusPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        NexusConfig config = plugin.getNexusConfig();

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender, config);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "mana" -> handleMana(sender, config);
            case "spells" -> handleSpells(sender, config);
            case "cast" -> handleCast(sender, config, args);
            case "combine" -> handleCombine(sender, config, args);
            case "reload" -> handleReload(sender, config);
            case "setmana" -> handleSetMana(sender, config, args, false);
            case "addmana" -> handleSetMana(sender, config, args, true);
            default -> {
                sender.sendMessage(config.msg("unknown-subcommand"));
                sendHelp(sender, config);
            }
        }
        return true;
    }

    private void handleMana(CommandSender sender, NexusConfig config) {
        Player player = requirePlayer(sender, config);
        if (player == null || !checkUsePermission(sender, config)) {
            return;
        }
        player.sendMessage(config.msg("mana-info",
                "current", FormatUtil.num(plugin.getManaManager().getMana(player)),
                "max", FormatUtil.num(plugin.getManaManager().getMaxMana())));
    }

    private void handleSpells(CommandSender sender, NexusConfig config) {
        if (!checkUsePermission(sender, config)) {
            return;
        }
        sender.sendMessage(config.msg("spells-header"));
        for (String id : plugin.getSpellManager().getSpellIds()) {
            Spell spell = plugin.getSpellManager().getSpell(id);
            AbilitySettings settings = config.getSpellSettings(id);
            if (settings == null) {
                continue;
            }
            sender.sendMessage(config.msg("spells-entry",
                    "id", spell.getId(),
                    "name", settings.getDisplayName(),
                    "cost", FormatUtil.num(settings.getManaCost()),
                    "cooldown", FormatUtil.num(settings.getCooldownSeconds())));
        }
        sender.sendMessage(config.msg("combos-header"));
        for (String id : plugin.getComboManager().getComboIds()) {
            Combo combo = plugin.getComboManager().getCombo(id);
            AbilitySettings settings = config.getComboSettings(id);
            if (settings == null) {
                continue;
            }
            sender.sendMessage(config.msg("combos-entry",
                    "id", combo.getId(),
                    "name", settings.getDisplayName(),
                    "first", combo.getFirst().getId(),
                    "second", combo.getSecond().getId(),
                    "cost", FormatUtil.num(settings.getManaCost()),
                    "cooldown", FormatUtil.num(settings.getCooldownSeconds())));
        }
    }

    private void handleCast(CommandSender sender, NexusConfig config, String[] args) {
        Player player = requirePlayer(sender, config);
        if (player == null || !checkUsePermission(sender, config)) {
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(config.msg("usage-cast"));
            return;
        }
        plugin.getSpellManager().cast(player, args[1]);
    }

    private void handleCombine(CommandSender sender, NexusConfig config, String[] args) {
        Player player = requirePlayer(sender, config);
        if (player == null || !checkUsePermission(sender, config)) {
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(config.msg("usage-combine"));
            return;
        }
        Element first = Element.fromString(args[1]);
        if (first == null) {
            sender.sendMessage(config.msg("unknown-element", "element", args[1]));
            return;
        }
        Element second = Element.fromString(args[2]);
        if (second == null) {
            sender.sendMessage(config.msg("unknown-element", "element", args[2]));
            return;
        }
        plugin.getComboManager().combine(player, first, second);
    }

    private void handleReload(CommandSender sender, NexusConfig config) {
        if (!checkAdminPermission(sender, config)) {
            return;
        }
        plugin.reloadNexusConfig();
        sender.sendMessage(config.msg("reloaded"));
    }

    private void handleSetMana(CommandSender sender, NexusConfig config, String[] args, boolean add) {
        if (!checkAdminPermission(sender, config)) {
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(config.msg(add ? "usage-addmana" : "usage-setmana"));
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(config.msg("player-not-found", "player", args[1]));
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(config.msg("invalid-number", "value", args[2]));
            return;
        }
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            sender.sendMessage(config.msg("invalid-number", "value", args[2]));
            return;
        }

        if (add) {
            plugin.getManaManager().addMana(target, amount, ManaChangeReason.COMMAND);
            sender.sendMessage(config.msg("mana-added",
                    "player", target.getName(),
                    "amount", FormatUtil.num(amount),
                    "mana", FormatUtil.num(plugin.getManaManager().getMana(target))));
            target.sendMessage(config.msg("target-mana-added",
                    "amount", FormatUtil.num(amount),
                    "mana", FormatUtil.num(plugin.getManaManager().getMana(target))));
        } else {
            plugin.getManaManager().setMana(target, amount, ManaChangeReason.COMMAND);
            sender.sendMessage(config.msg("mana-set",
                    "player", target.getName(),
                    "mana", FormatUtil.num(plugin.getManaManager().getMana(target))));
            target.sendMessage(config.msg("target-mana-set",
                    "mana", FormatUtil.num(plugin.getManaManager().getMana(target))));
        }
    }

    private void sendHelp(CommandSender sender, NexusConfig config) {
        sender.sendMessage(config.msg("help-header"));
        sender.sendMessage(config.msg("help-magic"));
        sender.sendMessage(config.msg("help-mana"));
        sender.sendMessage(config.msg("help-spells"));
        sender.sendMessage(config.msg("help-cast"));
        sender.sendMessage(config.msg("help-combine"));
        if (sender.hasPermission(ElementalNexusPlugin.PERMISSION_ADMIN)) {
            sender.sendMessage(config.msg("help-reload"));
            sender.sendMessage(config.msg("help-setmana"));
            sender.sendMessage(config.msg("help-addmana"));
        }
    }

    private Player requirePlayer(CommandSender sender, NexusConfig config) {
        if (sender instanceof Player player) {
            return player;
        }
        sender.sendMessage(config.msg("players-only"));
        return null;
    }

    private boolean checkUsePermission(CommandSender sender, NexusConfig config) {
        if (sender.hasPermission(ElementalNexusPlugin.PERMISSION_USE)) {
            return true;
        }
        sender.sendMessage(config.msg("no-permission"));
        return false;
    }

    private boolean checkAdminPermission(CommandSender sender, NexusConfig config) {
        if (sender.hasPermission(ElementalNexusPlugin.PERMISSION_ADMIN)) {
            return true;
        }
        sender.sendMessage(config.msg("no-permission"));
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            if (sender.hasPermission(ElementalNexusPlugin.PERMISSION_USE)) {
                options.addAll(BASE_SUBCOMMANDS);
            } else {
                options.add("help");
            }
            if (sender.hasPermission(ElementalNexusPlugin.PERMISSION_ADMIN)) {
                options.addAll(ADMIN_SUBCOMMANDS);
            }
            return partial(args[0], options);
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "cast" -> {
                if (args.length == 2 && sender.hasPermission(ElementalNexusPlugin.PERMISSION_USE)) {
                    return partial(args[1], new ArrayList<>(plugin.getSpellManager().getSpellIds()));
                }
            }
            case "combine" -> {
                if (!sender.hasPermission(ElementalNexusPlugin.PERMISSION_USE)) {
                    return Collections.emptyList();
                }
                if (args.length == 2) {
                    return partial(args[1], allElementNames(null));
                }
                if (args.length == 3) {
                    Element chosen = Element.fromString(args[1]);
                    return partial(args[2], allElementNames(chosen));
                }
            }
            case "setmana", "addmana" -> {
                if (!sender.hasPermission(ElementalNexusPlugin.PERMISSION_ADMIN)) {
                    return Collections.emptyList();
                }
                if (args.length == 2) {
                    List<String> names = new ArrayList<>();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        names.add(player.getName());
                    }
                    return partial(args[1], names);
                }
                if (args.length == 3) {
                    return partial(args[2], Arrays.asList("10", "50", "100"));
                }
            }
            default -> {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    /**
     * Все названия стихий (английские и русские) для таб-комплита.
     *
     * @param exclude стихия, которую нужно исключить (уже выбранная), или null
     */
    private static List<String> allElementNames(Element exclude) {
        List<String> names = new ArrayList<>();
        for (Element element : Element.values()) {
            if (element == exclude) {
                continue;
            }
            names.add(element.getId());
            names.add(element.getRussianName());
        }
        return names;
    }

    private static List<String> partial(String prefix, List<String> options) {
        List<String> matches = new ArrayList<>();
        String lowerPrefix = prefix.toLowerCase(Locale.ROOT);
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lowerPrefix)) {
                matches.add(option);
            }
        }
        Collections.sort(matches);
        return matches;
    }
}
