package com.projectkorra.projectkorra.attribute;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AvatarAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AttributeCache {

    private Field field;
    private String attribute;
    private Map<Class<? extends Annotation>, Annotation> markers = new HashMap<>();
    private Map<CoreAbility, Object> initialValues = new HashMap<>();
    private Optional<AttributeModification> avatarStateModifier = Optional.empty();

    public AttributeCache(Field field, String attribute) {
        this.field = field;
        this.attribute = attribute;
    }

    @NotNull
    public Field getField() {
        return field;
    }

    @NotNull
    public String getAttribute() {
        return attribute;
    }

    public boolean hasMarker(Class<? extends Annotation> markerClass) {
        return markers.containsKey(markerClass);
    }

    public void addMaker(@NotNull Annotation marker) {
        markers.put(marker.annotationType(), marker);
    }

    @Nullable
    public <T extends Annotation> T getMarker(Class<T> markerClass) {
        return (T) markers.get(markerClass);
    }

    public Map<CoreAbility, Object> getInitialValues() {
        return initialValues;
    }

    /**
     * Calculate the AvatarState equivalent of the attribute for the ability
     * @param ability The ability to calculate the AvatarState modifier for
     */
    public void calculateAvatarStateModifier(CoreAbility ability) {
        // If the ability is an AvatarAbility and requires the Avatar element, we don't want to apply the AvatarState modifier
        if (ability instanceof AvatarAbility && ((AvatarAbility) ability).requireAvatar()) return;

        String configName = attribute;

        if (attribute.equals(Attribute.AVATAR_STATE_TOGGLE)) configName = "IsToggle";
        String elementName = ability.getElement().getName();
        if (ability.getElement() instanceof Element.SubElement) {
            elementName = ((Element.SubElement) ability.getElement()).getParentElement().getName();
        }
        String configPath = "Abilities." + elementName + "." + ability.getName() + "." + configName;
        Object configObject = ConfigManager.avatarStateConfig.get().get(configPath);

        if (configObject == null) { //If the attribute doesn't exist for the ability in the config, check the _All section (for the element)
            configPath = "Abilities." + ability.getElement().getName() + "._All." + attribute;

            configObject = ConfigManager.avatarStateConfig.get().get(configPath);

            if (configObject == null) { //If that is also null, check the global _All section

                configPath = "Abilities._All." + attribute;
                configObject = ConfigManager.avatarStateConfig.get().get(configPath);

                //And if it still isn't a thing, ignore it
                if (configObject == null || configObject instanceof ConfigurationSection) return;
            }
        }


        String stringObject = configObject.toString();

        if (configObject instanceof Boolean && field.getType() == Boolean.TYPE) {
            avatarStateModifier = Optional.of(AttributeModification.setter((Boolean) configObject, AttributeModification.PRIORITY_LOW, AttributeModification.AVATAR_STATE_FACTOR));
        } else if (configObject instanceof Number) {
            avatarStateModifier = Optional.of(AttributeModification.of(AttributeModifier.SET, (Number) configObject, AttributeModification.PRIORITY_LOW, AttributeModification.AVATAR_STATE_FACTOR));
        } else if (stringObject != null) {
            stringObject = stringObject.replaceAll(" ", "");

            //Parse the value from the config. E.g. x0.8 turns into a multiplication of 0.8, and +10% turns into a multiplication of 1.1
            Pair<AttributeModifier, Number> parsed = AttributeUtil.getModification(stringObject);

            if (parsed == null) {
                ProjectKorra.log.severe("Failed to parse AvatarState modification for " + ability.getName() + " " + attribute + " with value " + stringObject);
                return;
            }
            avatarStateModifier = Optional.of(AttributeModification.of(parsed.getLeft(), parsed.getRight(), AttributeModification.PRIORITY_LOW, AttributeModification.AVATAR_STATE_FACTOR));
        }


    }

    public Optional<AttributeModification> getAvatarStateModifier() {
        return avatarStateModifier;
    }

}
