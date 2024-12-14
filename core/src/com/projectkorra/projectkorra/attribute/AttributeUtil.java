package com.projectkorra.projectkorra.attribute;

import com.projectkorra.projectkorra.ProjectKorra;
import org.apache.commons.lang3.tuple.Pair;

public class AttributeUtil {

    public static Pair<AttributeModifier, Number> getModification(String modification) {
        //modification = modification.replaceAll("[ A-Za-z]", "");
        if (isValidValue(modification)) {
            AttributeModifier modifier = AttributeModifier.SET;
            double value;
            String parsingValue = modification;

            if (modification.startsWith("+")) {
                parsingValue = parsingValue.substring(1); //Get rid of the plus
                modifier = AttributeModifier.ADDITION;
            } else if (modification.startsWith("x") || modification.startsWith("*")) {
                parsingValue = parsingValue.substring(1);
                modifier = AttributeModifier.MULTIPLICATION;
            } else if (modification.startsWith("/")) {
                parsingValue = parsingValue.substring(1);
                modifier = AttributeModifier.DIVISION;
            } else if (modification.startsWith("-")) {
                parsingValue = parsingValue.substring(1);
                modifier = AttributeModifier.SUBTRACTION;
            }

            if (modification.endsWith("%")) {
                parsingValue = parsingValue.substring(0, parsingValue.length() - 1);
                modifier = AttributeModifier.MULTIPLICATION;
            }

            try {
                value = Double.parseDouble(parsingValue);

                if (modification.endsWith("%")) {
                    value /= 100;

                    if (modification.startsWith("+")) {
                        value += 1;
                    } else if (modification.startsWith("-")) {
                        value = 1 - value;
                    }
                }

                return Pair.of(modifier, value);

            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    public static boolean isValidValue(String modification) {
        modification = modification.replaceAll(" ", "");
        if (modification.endsWith("%")) {
            String percent = modification.substring(0, modification.length() - 1); //Cut off the %
            if (percent.startsWith("+") || percent.startsWith("-")) percent = percent.substring(1);
            try {
                Double.parseDouble(percent);
                return true;
            } catch (NumberFormatException e) { return false; }
        } else if (modification.startsWith("x") || modification.startsWith("*") || modification.startsWith("/")) {
            try {
                Double.parseDouble(modification.substring(1));
                return true;
            } catch (NumberFormatException e) { return false; }
        } else if (modification.startsWith("+") || modification.startsWith("-")) {
            try {
                Double.parseDouble(modification.substring(1));
                return true;
            } catch (NumberFormatException e) { return false; }
        } else {
            try {
                Double.parseDouble(modification);
                return true;
            } catch (NumberFormatException e) { return false; }
        }
    }
}
