package com.projectkorra.projectkorra.util;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ChatUtil {

    /**
     * Send a message prefixed with the ProjectKorra branding to the provided receiver
     * @param receiver The person to send the message to
     * @param message The message to send
     */
    public static void sendBrandingMessage(final CommandSender receiver, final String message) {
        if (Strings.isEmpty(ChatColor.stripColor(message))) return;

        sendBrandingMessage(receiver, TextComponent.fromLegacyText(color(message)));
    }

    /**
     * Send a message prefixed with the ProjectKorra branding to the provided receiver
     * @param receiver The person to send the message to
     * @param message The message to send
     */
    public static void sendBrandingMessage(final CommandSender receiver, final BaseComponent[] message) {
        if (message == null || message.length == 0) return;

        BaseComponent newComp = new TextComponent();

        for (BaseComponent comp : message) {
            newComp.addExtra(comp);
        }
        sendBrandingMessage(receiver, newComp);
    }

    /**
     * Send a message prefixed with the ProjectKorra branding to the provided receiver
     * @param receiver The person to send the message to
     * @param message The message to send
     */
    public static void sendBrandingMessage(final CommandSender receiver, final BaseComponent message) {
        ChatColor color;
        try {
            color = ChatColor.of(ConfigManager.languageConfig.get().getString("Chat.Branding.Color").toUpperCase());
        } catch (final IllegalArgumentException exception) {
            color = ChatColor.GOLD;
        }

        final String start = color(ConfigManager.languageConfig.get().getString("Chat.Branding.ChatPrefix.Prefix", ""));
        final String main = color(ConfigManager.languageConfig.get().getString("Chat.Branding.ChatPrefix.Main", "ProjectKorra"));
        final String end = color(ConfigManager.languageConfig.get().getString("Chat.Branding.ChatPrefix.Suffix", " \u00BB "));
        final String prefix = color + start + main + end;
        if (!(receiver instanceof Player)) {
            receiver.sendMessage(prefix + message.toLegacyText());
        } else {
            final TextComponent prefixComponent = new TextComponent(prefix);
            final String hover = multiline(color + ConfigManager.languageConfig.get().getString("Chat.Branding.ChatPrefix.Hover", color + "Bending brought to you by ProjectKorra | Fork Roku!\n" + color + "Click for more info."));
            final String click = ConfigManager.languageConfig.get().getString("Chat.Branding.ChatPrefix.Click", "https://www.projectkorra.com");
            if (!hover.equals(""))
                prefixComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
            if (!click.equals("")) {
                ClickEvent.Action action = ClickEvent.Action.RUN_COMMAND;
                if (click.toLowerCase().startsWith("http://") || click.toLowerCase().startsWith("https://") || click.toLowerCase().startsWith("www.")) {
                    action = ClickEvent.Action.OPEN_URL;
                }
                prefixComponent.setClickEvent(new ClickEvent(action, click));
            }
            final TextComponent messageComponent = new TextComponent(message);
            messageComponent.setColor(ChatColor.YELLOW); //Set the base color to yellow
            ((Player) receiver).spigot().sendMessage(new TextComponent(prefixComponent, messageComponent));
        }
    }

    /**
     * Colors a string with color codes and hex color codes. This also does &amp;#RRGGBB codes which are not
     * done by ChatColor's methods
     * @param string The string to color
     * @return The colored string
     */
    public static String color(String string) {
        string = string == null ? "" : string; //Ensure no NullPointers
        return ChatColor.translateAlternateColorCodes('&', string.replaceAll("\\\\n", "\n")
                .replaceAll("[\u00A7&]#([A-Fa-f\\d]{1})([A-Fa-f\\d]{1})([A-Fa-f\\d]{1})([A-Fa-f\\d]{1})([A-Fa-f\\d]{1})([A-Fa-f\\d]{1})",
                        "\u00A7x\u00A7$1\u00A7$2\u00A7$3\u00A7$4\u00A7$5\u00A7$6")); //Replaces &#RRGGBB to &x&R&R&B&B&G&G (how hex actually works)
    }

    /**
     * Ensures a multiline string is properly formatted with color codes
     * @param string The string to format
     * @return The formatted string
     */
    public static String multiline(String string) {
        string = color(string);
        Character lastColor = null;
        String hex = null;
        List<String> l = new ArrayList<>();
        for (String line : string.split("\n")) {
            String prefix = "";
            if (lastColor != null) {
                prefix = "\u00A7" + lastColor;  //Make the prefix the current color
                if (hex != null) prefix += hex; //If the current color is a hex code, add on the RGB as well

                if (l.size() == 0 && string.charAt(0) == '\u00A7') prefix = ""; //Don't bother adding a pointless color code
            }

            l.add(prefix + line);
            if (line.contains("\u00A7")) {
                int index = line.lastIndexOf('\u00A7');
                lastColor = line.charAt(index + 1);
                if (lastColor == 'x') {
                    hex = line.substring(index + 2, index + 14);
                } else hex = null;
            }
        }
        return String.join("\n", l);
    }

    /**
     * Previews the current move to the player
     * @param player The player
     * @param slot The slot to display
     */
    public static void displayMovePreview(final Player player, final int slot) {
        if (!ConfigManager.defaultConfig.get().getBoolean("Properties.BendingPreview")) {
            return;
        }

        final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
        if (bPlayer == null) {
            return;
        }
        String displayedMessage = bPlayer.getAbilities().get(slot);
        final CoreAbility ability = CoreAbility.getAbility(displayedMessage);

        if (ability != null && bPlayer != null) {
            displayedMessage = ability.getMovePreview(player);
        } else if (displayedMessage == null || displayedMessage.isEmpty() || displayedMessage.equals("")) {
            displayedMessage = "";
        }

        sendActionBar(displayedMessage, player);
    }

    /**
     * Previews the current move to the player
     * @param player The player
     */
    public static void displayMovePreview(final Player player) {
        displayMovePreview(player, player.getInventory().getHeldItemSlot() + 1);
    }

    /**
     * Sends an action bar message to the player
     *
     * @param message the message to send
     * @param player the player to send the message to
     */
    public static void sendActionBar(final String message, final Player... player) {
        for (Player e : player) {
            e.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(TextComponent.fromLegacyText(message)));
        }
    }
}
