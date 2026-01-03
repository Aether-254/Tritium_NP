package org.craftamethyst.tritium.integration.sodium;

import net.caffeinemc.mods.sodium.client.gui.options.OptionImpact;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatter;
import org.craftamethyst.tritium.config.TritiumConfigBase;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class TritiumOptionDefinition<T> {
    private final String key;
    private final String translationKey;
    private final Function<TritiumConfigBase, T> getter;
    private final BiConsumer<TritiumConfigBase, T> setter;
    private final OptionImpact impact;
    private final boolean hasImpact;
    private final ControlType controlType;
    private final Object[] controlParams;
    private final ControlValueFormatter formatter;

    public TritiumOptionDefinition(String key, String translationKey,
                                   Function<TritiumConfigBase, T> getter,
                                   BiConsumer<TritiumConfigBase, T> setter,
                                   OptionImpact impact, ControlType controlType,
                                   ControlValueFormatter formatter, Object... controlParams) {
        this.key = key;
        this.translationKey = translationKey;
        this.getter = getter;
        this.setter = setter;
        this.impact = impact;
        this.hasImpact = true;
        this.controlType = controlType;
        this.controlParams = controlParams;
        this.formatter = formatter;
    }

    public TritiumOptionDefinition(String key, String translationKey,
                                   Function<TritiumConfigBase, T> getter,
                                   BiConsumer<TritiumConfigBase, T> setter,
                                   ControlType controlType,
                                   ControlValueFormatter formatter, Object... controlParams) {
        this.key = key;
        this.translationKey = translationKey;
        this.getter = getter;
        this.setter = setter;
        this.impact = null;
        this.hasImpact = false;
        this.controlType = controlType;
        this.controlParams = controlParams;
        this.formatter = formatter;
    }

    public TritiumOptionDefinition(String key, String translationKey,
                                   Function<TritiumConfigBase, T> getter,
                                   BiConsumer<TritiumConfigBase, T> setter,
                                   OptionImpact impact, ControlType controlType, Object... controlParams) {
        this(key, translationKey, getter, setter, impact, controlType, null, controlParams);
    }

    public TritiumOptionDefinition(String key, String translationKey,
                                   Function<TritiumConfigBase, T> getter,
                                   BiConsumer<TritiumConfigBase, T> setter,
                                   ControlType controlType, Object... controlParams) {
        this(key, translationKey, getter, setter, null, controlType, null, controlParams);
    }

    public String getKey() {
        return key;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public Function<TritiumConfigBase, T> getGetter() {
        return getter;
    }

    public BiConsumer<TritiumConfigBase, T> getSetter() {
        return setter;
    }

    public OptionImpact getImpact() {
        return impact;
    }

    public boolean hasImpact() {
        return hasImpact;
    }

    public ControlType getControlType() {
        return controlType;
    }

    public Object[] getControlParams() {
        return controlParams;
    }

    public ControlValueFormatter getFormatter() {
        return formatter;
    }

    public enum ControlType {
        BOOLEAN,
        INTEGER_SLIDER,
        DOUBLE_SLIDER,
        ENUM_CYCLE
    }
}
