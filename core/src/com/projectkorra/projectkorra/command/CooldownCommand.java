package com.projectkorra.projectkorra.command;

import com.google.common.collect.Lists;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.OfflineBendingPlayer;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.event.PlayerCooldownChangeEvent;
import com.projectkorra.projectkorra.util.ChatUtil;
import com.projectkorra.projectkorra.util.Cooldown;
import com.projectkorra.projectkorra.util.TimeUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CooldownCommand extends PKCommand {

    private static final Set<String> COOLDOWNS = new HashSet<>();

    public CooldownCommand() {
        super("cooldown", "/bending cooldown <view/set/reset> <player> [cooldown]", ConfigManager.getConfig().getString("Commands.Cooldown.Description"), new String[] {"cooldown", "cooldowns", "cd"});

        COOLDOWNS.add("ChooseElement");

        Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, () -> {
            for (CoreAbility ability : CoreAbility.getAbilities()) {
                if (ability.isHiddenAbility() || ability instanceof PassiveAbility || !ability.isEnabled()) continue;

                COOLDOWNS.add(ability.getName());
            }
        }, 1);
    }

    @Override
    public void execute(CommandSender sender, List<String> list) {
        if (!correctLength(sender, list.size(), 1, 4) || !Arrays.asList(new String[] {"view", "v", "set", "s", "reset", "r"}).contains(list.get(0).toLowerCase())) {
            return;
        }

        if (list.size() == 1 && !this.isPlayer(sender)) {
            return;
        }

        if (!this.hasPermission(sender)) {
            return;
        }

        if (list.size() == 1) {
            _execute(sender, list, (Player) sender);
            return;
        }

        this.getPlayer(list.get(1)).thenAccept(oPlayer -> {
            if (oPlayer == null || (!oPlayer.isOnline() && !oPlayer.hasPlayedBefore())) {
                ChatUtil.sendBrandingMessage(sender, ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Cooldown.InvalidPlayer"));
                return;
            }
            _execute(sender, list, oPlayer);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });;
    }

    public void _execute(CommandSender sender, List<String> list, OfflinePlayer oPlayer) {

        BendingPlayer.getOrLoadOfflineAsync(oPlayer).thenAccept(bPlayer -> {
            if (Arrays.asList(new String[] {"view", "v"}).contains(list.get(0).toLowerCase())) {
                if (!this.hasPermission(sender, "view")) {
                    return;
                }

                List<String> cooldowns = bPlayer.getCooldowns().entrySet().stream()
                        .sorted(Comparator.comparingLong(entry -> entry.getValue().getCooldown()))
                        .filter(entry -> entry.getValue().getCooldown() > 0)
                        .map(entry -> ChatColor.YELLOW + entry.getKey() + ": " + ChatColor.RED +
                                TimeUtil.formatTime(entry.getValue().getCooldown() - System.currentTimeMillis()))
                        .collect(Collectors.toList());

                if (cooldowns.isEmpty()) {
                    String titleMessage = ConfigManager.languageConfig.get().getString("Commands.Cooldown.ViewNone").replace("{player}", oPlayer.getName());
                    ChatUtil.sendBrandingMessage(sender, ChatColor.RED + titleMessage);
                    return;
                }

                String titleMessage = ConfigManager.languageConfig.get().getString("Commands.Cooldown.View");
                if (cooldowns.size() > 10) {
                    titleMessage = ConfigManager.languageConfig.get().getString("Commands.Cooldown.ViewMax");
                }
                titleMessage = titleMessage.replace("{player}", oPlayer.getName()).replace("{number}", Math.min(cooldowns.size(), 10) + "");
                ChatUtil.sendBrandingMessage(sender, ChatColor.GREEN + titleMessage);
                for (int i = 0; i < 10 && i < cooldowns.size(); i++) {
                    String message = cooldowns.get(i);
                    String cooldownName = ChatColor.stripColor(message).split(":")[0];
                    String showText = ConfigManager.languageConfig.get().getString("Commands.Cooldown.ResetPreview").replace("{cooldown}", cooldownName);
                    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.YELLOW + showText));
                    ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/b cooldown reset " + oPlayer.getName() + " " + cooldownName);
                    TextComponent textComponent = new TextComponent(message);
                    textComponent.setClickEvent(clickEvent);
                    textComponent.setHoverEvent(hoverEvent);
                    sender.spigot().sendMessage(textComponent);
                }
                return;
            }

            boolean set = Arrays.asList(new String[] {"set", "s"}).contains(list.get(0).toLowerCase());

            String cooldown;

            if (list.size() < 3) { //They didn't provide a cooldown name
                cooldown = "ALL";
            } else
                cooldown = list.get(2);

            if (list.size() > 3 && set) {
                if (!this.hasPermission(sender, "set")) {
                    return;
                }

                long time;
                try {
                    time = TimeUtil.unformatTime(list.get(3));
                } catch (NumberFormatException e) {
                    ChatUtil.sendBrandingMessage(sender, ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Cooldown.InvalidTime").replace("{value}", list.get(3)));
                    return;
                }
                if (cooldown.equals("*") || cooldown.equalsIgnoreCase("ALL")) {
                    if (time == 0) {
                        Set<String> cooldownKeys = new HashSet<>(bPlayer.getCooldowns().keySet()); //Clone the list to prevent concurrentmodifications
                        cooldownKeys.forEach(bPlayer::removeCooldown); //We do this instead of clear() because we need to call the event
                        bPlayer.saveCooldowns();
                        ChatUtil.sendBrandingMessage(sender, ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Cooldown.ResetAll").replace("{player}", oPlayer.getName()));
                        return;
                    }
                    ChatUtil.sendBrandingMessage(sender, ChatColor.GREEN + ConfigManager.languageConfig.get().getString("Commands.Cooldown.SetAll").replace("{player}", oPlayer.getName()));
                    return;
                }

                String fixedCooldown = setCooldown(sender, bPlayer, cooldown, time);
                if (fixedCooldown == null) {
                    return;
                }

                String message = ConfigManager.languageConfig.get().getString("Commands.Cooldown.Set").replace("{player}",
                        oPlayer.getName()).replace("{cooldown}", fixedCooldown).replace("{value}", TimeUtil.formatTime(time));
                ChatUtil.sendBrandingMessage(sender, ChatColor.GREEN + message);
            } else if (set) {
                if (!this.hasPermission(sender, "set")) {
                    return;
                }
                ChatUtil.sendBrandingMessage(sender, ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Cooldown.SetNoValue"));
            } else {
                if (!this.hasPermission(sender, "reset")) {
                    return;
                }
                if (cooldown.equals("*") || cooldown.equalsIgnoreCase("ALL")) {
                    Set<String> cooldownKeys = new HashSet<>(bPlayer.getCooldowns().keySet()); //Clone the list to prevent concurrentmodifications
                    cooldownKeys.forEach(bPlayer::removeCooldown); //We do this instead of clear() because we need to call the event
                    bPlayer.saveCooldowns();
                    ChatUtil.sendBrandingMessage(sender, ChatColor.GREEN + ConfigManager.languageConfig.get().getString("Commands.Cooldown.ResetAll").replace("{player}", oPlayer.getName()));
                    return;
                }

                String fixedCooldown = setCooldown(sender, bPlayer, cooldown, 0);
                if (fixedCooldown == null) {
                    return;
                }

                String message = ConfigManager.languageConfig.get().getString("Commands.Cooldown.Reset").replace("{player}",
                        oPlayer.getName()).replace("{cooldown}", fixedCooldown);
                ChatUtil.sendBrandingMessage(sender, ChatColor.GREEN + message);
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    private String setCooldown(CommandSender sender, OfflineBendingPlayer bPlayer, String cooldown, long time) {
        String fixedCooldown = COOLDOWNS.stream().filter(s -> s.equalsIgnoreCase(cooldown)).findFirst().orElse(null);
        if (fixedCooldown == null) {
            ChatUtil.sendBrandingMessage(sender, ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Cooldown.InvalidCooldown"));
            return null;
        }

        boolean cancelled = false;
        if (bPlayer.getPlayer().isOnline()) {
            final PlayerCooldownChangeEvent event = new PlayerCooldownChangeEvent(((BendingPlayer)bPlayer).getPlayer(), cooldown, time, time <= 0 ? PlayerCooldownChangeEvent.Result.REMOVED : PlayerCooldownChangeEvent.Result.SET);
            Bukkit.getServer().getPluginManager().callEvent(event);
            cancelled = event.isCancelled();
        }

        if (!cancelled) {
            if (time <= 0) {
                bPlayer.getCooldowns().remove(fixedCooldown);
                return fixedCooldown;
            }

            Cooldown cooldownObject = bPlayer.getCooldowns().get(fixedCooldown);
            cooldownObject = new Cooldown(time + System.currentTimeMillis(), cooldownObject != null && cooldownObject.isDatabase());
            bPlayer.getCooldowns().put(fixedCooldown, cooldownObject);
            return fixedCooldown;
        }

        return null;
    }

    @Override
    protected List<String> getTabCompletion(CommandSender sender, List<String> args) {
        if (args.size() < 1) return Arrays.asList("view", "set", "reset");
        if (args.size() < 2) return getOnlinePlayerNames(sender);
        else if (args.size() < 3 && !Arrays.asList(new String[] {"view", "v"}).contains(args.get(0).toLowerCase())) {
            List<String> list = new ArrayList<>(COOLDOWNS);
            list.add("*");
            list.add("ALL");
            return list;
        } else if (Arrays.asList(new String[] {"set", "s"}).contains(args.get(0))) return Lists.newArrayList("60s", "30m", "2h", "30d");
        return new ArrayList<>(0);
    }

    public static void addCooldownType(String cooldown) {
        COOLDOWNS.add(cooldown);
    }
}
