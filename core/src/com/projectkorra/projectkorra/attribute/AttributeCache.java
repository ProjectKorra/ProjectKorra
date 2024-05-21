package com.projectkorra.projectkorra.attribute;

import com.projectkorra.projectkorra.ability.CoreAbility;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class AttributeCache {

    private Field field;
    private String attribute;
    private Map<Class<? extends Annotation>, Annotation> markers = new HashMap<>();
    private Map<CoreAbility, Object> initialValues = new HashMap<>();

    public AttributeCache(Field field, String attribute) {
        this.field = field;
        this.attribute = attribute;
    }

    public Field getField() {
        return field;
    }

    public String getAttribute() {
        return attribute;
    }

    public boolean hasMarker(Class<? extends Annotation> markerClass) {
        return markers.containsKey(markerClass);
    }

    public void addMaker(Annotation marker) {
        markers.put(marker.annotationType(), marker);
    }

    public <T extends Annotation> T getMarker(Class<T> markerClass) {
        return (T) markers.get(markerClass);
    }

    public Map<CoreAbility, Object> getInitialValues() {
        return initialValues;
    }
}
