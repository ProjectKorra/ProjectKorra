package com.projectkorra.projectkorra.ability.util;

import java.util.ArrayList;
import java.util.List;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.ClickType;

public class ComboUtil {

    /**
     * Generates a combination for a ComboAbility based on the string list pulled from
     * a config
     * @param combo The ComboAbility
     * @param configList The string list from the config
     * @return The combination
     */
    public static ArrayList<AbilityInformation> generateCombinationFromList(ComboAbility combo, List<String> configList) {
        ArrayList<AbilityInformation> comboList = new ArrayList<>();

        String comboName = ((CoreAbility)combo).getName();
        for (String line : configList) {
            String[] split = line.split(":");
            if (split.length != 2) {
                ProjectKorra.log.warning("Invalid combination for ability \"" + comboName + "\": Contains no colon and ClickType found!");
                continue;
            }
            String ability = split[0].trim();
            String click = split[1].trim();
            CoreAbility coreAbility = CoreAbility.getAbility(ability);
            if (coreAbility == null) {
                ProjectKorra.log.warning("Invalid combination for ability \"" + comboName + "\": Ability \"" + ability + "\" not found!");
                continue;
            }
            if (coreAbility instanceof ComboAbility) {
                ProjectKorra.log.warning("Invalid combination for ability \"" + comboName + "\": Ability \"" + ability + "\" is a combo and can't be used in another combination!");
                continue;
            }
            if (coreAbility instanceof PassiveAbility) {
                ProjectKorra.log.warning("Invalid combination for ability \"" + comboName + "\": Ability \"" + ability + "\" is a passive and can't be used in a combination!");
                continue;
            }

            ClickType clickType = getClickType(click);
            if (clickType == null) {
                ProjectKorra.log.warning("Invalid combination for ability \"" + comboName + "\": ClickType \"" + ability + "\" not found! Using LEFT_CLICK instead!");
                clickType = ClickType.LEFT_CLICK;
            }
            comboList.add(new AbilityInformation(ability, clickType));
        }

        if (comboList.size() < 2) {
            ProjectKorra.log.warning("Invalid combination for ability \"" + comboName + "\": Doesn't contain at least 2 combinations!");
            return null;
        }

        return comboList;
    }

    public static ClickType getClickType(String string) {
        try {
            ClickType type = ClickType.valueOf(string.toUpperCase());
            if (type != ClickType.CUSTOM) return type;
        } catch (IllegalArgumentException ignored) {}

        switch (string.toUpperCase().replaceAll("[_ ]", "")) {
            case "CLICK":
            case "LEFT":
            case "LEFTCLICK":
            case "LEFTCLICKBLOCK":
                return ClickType.LEFT_CLICK;
            case "RIGHT":
            case "RIGHTCLICK":
                return ClickType.RIGHT_CLICK;
            case "RIGHTCLICKBLOCK":
                return ClickType.RIGHT_CLICK_BLOCK;
            case "RIGHTCLICKENTITY":
                return ClickType.RIGHT_CLICK_ENTITY;
            case "HITENTITY":
            case "HITMOB":
            case "LEFTCLICKENTITY":
            case "CLICKENTITY":
            case "HIT":
                return ClickType.LEFT_CLICK_ENTITY;
            case "SNEAKUP":
            case "SHIFTUP":
                return ClickType.SHIFT_UP;
            case "SNEAKDOWN":
            case "SHIFTDOWN":
                return ClickType.SHIFT_DOWN;
            case "OFFHAND":
            case "OFFHANDTRIGGER":
                return ClickType.OFFHAND_TRIGGER;
            default:
                return null;
        }
    }
}
