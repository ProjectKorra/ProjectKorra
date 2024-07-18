package com.projectkorra.projectkorra.hooks;

import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.Caller;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.NotReadyException;
import com.djrapitops.plan.extension.annotation.BooleanProvider;
import com.djrapitops.plan.extension.annotation.Conditional;
import com.djrapitops.plan.extension.annotation.NumberProvider;
import com.djrapitops.plan.extension.annotation.PluginInfo;
import com.djrapitops.plan.extension.annotation.StringProvider;
import com.djrapitops.plan.extension.annotation.TableProvider;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.table.Table;
import com.djrapitops.plan.extension.table.TableColumnFormat;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.OfflineBendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.object.Preset;
import com.projectkorra.projectkorra.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

@PluginInfo(name = "ProjectKorra", iconName = "circle-arrow-down", iconFamily = Family.SOLID, color = Color.DEEP_PURPLE)
public class PlanExtension implements DataExtension {

    @Override
    public CallEvents[] callExtensionMethodsOn() {
        return new CallEvents[]{
                CallEvents.PLAYER_JOIN,
                CallEvents.PLAYER_LEAVE,
                CallEvents.SERVER_EXTENSION_REGISTER,
                CallEvents.SERVER_PERIODICAL
        };
    }

    @BooleanProvider(
            text = "Permanently Removed", // ALWAYS REQUIRED
            description = "Whether or not the player has permanently had their bending removed",
            priority = 60,
            iconName = "user-minus",
            iconFamily = Family.SOLID,
            iconColor = Color.RED,
            conditionName = "permaremoved",
            hidden = false,
            showInPlayerTable = true
    )
    public boolean isPermaRemoved(UUID playerUUID) {
        return getPlayer(playerUUID).isPermaRemoved();
    }

    @StringProvider(
            text = "Elements", // ALWAYS REQUIRED
            description = "The elements the player has",
            priority = 80,
            iconName = "layer-group",
            iconFamily = Family.SOLID,
            iconColor = Color.NONE
    )
    @Conditional(
            value = "permaremoved",
            negated = true
    )
    public String elements(UUID playerUUID) {
        return getPlayer(playerUUID).getElements().stream().map(e -> e.getColor() + e.getName() + ChatColor.RESET).reduce((a, b) -> a + ", " + b).orElse("None");
    }

    @StringProvider(
            text = "Element", // ALWAYS REQUIRED
            description = "The element the player has",
            priority = 100,
            iconName = "layer-group",
            iconFamily = Family.SOLID,
            iconColor = Color.NONE,
            showInPlayerTable = true
    )
    public String elementsShort(UUID playerUUID) {
        OfflineBendingPlayer bPlayer = getPlayer(playerUUID);
        return bPlayer.getElements().isEmpty() ? "Nonbender" : (bPlayer.getElements().size() > 1 ? ChatColor.DARK_PURPLE + "Avatar" : bPlayer.getElements().get(0).getColor() + bPlayer.getElements().get(0).getName());
    }

    @NumberProvider(
            text = "Presets Created", // ALWAYS REQUIRED
            description = "The number of presets the player has",
            priority = 70,
            iconName = "table-list",
            iconFamily = Family.SOLID,
            iconColor = Color.NONE,
            showInPlayerTable = false
    )
    public long presets(UUID playerUUID) {
        return Preset.presets.getOrDefault(playerUUID, Collections.emptyList()).size();
    }

    @StringProvider(text = "Slots", description = "The slots the player has bound", priority = -1, iconName = "arrow-down-1-9", iconFamily = Family.SOLID, iconColor = Color.ORANGE)
    public String slotsTitle(UUID player) {return "";}

    @StringProvider(text = "1", priority = -2, iconName = "")
    public String slotsTitle1(UUID player) {return slot(player, 1);}

    @StringProvider(text = "2", priority = -3, iconName = "")
    public String slotsTitle2(UUID player) {return slot(player, 2);}

    @StringProvider(text = "3", priority = -4, iconName = "")
    public String slotsTitle3(UUID player) {return slot(player, 3);}

    @StringProvider(text = "4", priority = -5, iconName = "")
    public String slotsTitle4(UUID player) {return slot(player, 4);}

    @StringProvider(text = "5", priority = -6, iconName = "")
    public String slotsTitle5(UUID player) {return slot(player, 5);}

    @StringProvider(text = "6", priority = -7, iconName = "")
    public String slotsTitle6(UUID player) {return slot(player, 6);}

    @StringProvider(text = "7", priority = -8, iconName = "")
    public String slotsTitle7(UUID player) {return slot(player, 7);}

    @StringProvider(text = "8", priority = -9, iconName = "")
    public String slotsTitle8(UUID player) {return slot(player, 8);}

    @StringProvider(text = "9", priority = -10)
    public String slotsTitle9(UUID player) {return slot(player, 9);}

    private String slot(UUID player, int num) {
        String ability = getPlayer(player).getAbilities().getOrDefault(num, "");
        if (ability.equalsIgnoreCase("")) return "-";

        CoreAbility coreAbility = CoreAbility.getAbility(ability);
        if (coreAbility == null) return ability;

        return coreAbility.getElement().getColor() + ability;
    }

    /*@TableProvider(tableColor = Color.GREY)
    public Table getBinds(UUID playerUUID) {
        OfflineBendingPlayer bPlayer = getPlayer(playerUUID);
        Table.Factory table = Table.builder()
                .columnOne("Slot", Icon.called("arrow-down-1-9").build())
                .columnTwo("Ability", Icon.called("bolt").build());
        String none = "<i>None</i>";
        table.addRow("1", bPlayer.getAbilities().getOrDefault(1, none));
        table.addRow("2", bPlayer.getAbilities().getOrDefault(2, none));
        table.addRow("3", bPlayer.getAbilities().getOrDefault(3, none));
        table.addRow("4", bPlayer.getAbilities().getOrDefault(4, none));
        table.addRow("5", bPlayer.getAbilities().getOrDefault(5, none));
        table.addRow("6", bPlayer.getAbilities().getOrDefault(6, none));
        table.addRow("7", bPlayer.getAbilities().getOrDefault(7, none));
        table.addRow("8", bPlayer.getAbilities().getOrDefault(8, none));
        table.addRow("9", bPlayer.getAbilities().getOrDefault(9, none));

        return table.build();
    }*/



    @TableProvider(tableColor = Color.GREY)
    public Table getTempElements(UUID playerUUID) {
        OfflineBendingPlayer bPlayer = getPlayer(playerUUID);
        Table.Factory table = Table.builder()
                .columnOne("Temp Element", Icon.called("layer-group").build())
                .columnTwo("Duration", Icon.called("clock").build());

        for (Element element : bPlayer.getTempElements().keySet()) {
            long duration = System.currentTimeMillis() - bPlayer.getTempElements().get(element);
            table.addRow(element.getColor() + element.getName(), TimeUtil.formatTime(duration));
        }
        for (Element.SubElement subElement : bPlayer.getTempSubElements().keySet()) {
            long duration = bPlayer.getTempSubElements().get(subElement);
            if (duration > 0) table.addRow(subElement.getName(), TimeUtil.formatTime(System.currentTimeMillis() - duration));
        }

        return table.build();
    }

    private OfflineBendingPlayer getPlayer(UUID playerUUID) {
        OfflineBendingPlayer bPlayer = BendingPlayer.getOrLoadOffline(Bukkit.getOfflinePlayer(playerUUID));

        if (bPlayer == null) throw new NotReadyException();
        return bPlayer;
    }

}
