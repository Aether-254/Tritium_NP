package org.craftamethyst.tritium.integration.embeddium;

import com.google.common.collect.ImmutableList;
import me.jellysquid.mods.sodium.client.gui.options.OptionGroup;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpl;
import me.jellysquid.mods.sodium.client.gui.options.control.SliderControl;
import me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.config.TritiumConfigBase;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;

public class TritiumPageBuilder {
    private final String pageId;
    private final Component title;
    private final List<OptionGroup> groups = new ArrayList<>();

    public TritiumPageBuilder(String pageId, Component title) {
        this.pageId = pageId;
        this.title = title;
    }

    public TritiumPageBuilder addGroup(String groupId, GroupConfigurator configurator) {
        GroupBuilder builder = new GroupBuilder(groupId);
        configurator.configure(builder);
        groups.add(builder.build());
        return this;
    }

    public TritiumEmbPage build() {
        return new TritiumEmbPage(pageId, title, ImmutableList.copyOf(groups));
    }

    @FunctionalInterface
    public interface GroupConfigurator {
        void configure(GroupBuilder builder);
    }

    public static class GroupBuilder {
        private final OptionGroup.Builder builder;
        private final TritiumOptionStorage optionStorage;

        public GroupBuilder(String groupId) {
            this.builder = OptionGroup.createBuilder()
                    .setId(ResourceLocation.tryBuild(TritiumCommon.MOD_ID, groupId));
            this.optionStorage = TritiumOptionStorage.getInstance();
        }

        public GroupBuilder addBoolean(String id, String translationKey,
                                       BiConsumer<TritiumConfigBase, Boolean> setter,
                                       Function<TritiumConfigBase, Boolean> getter) {
            builder.add(OptionImpl.createBuilder(boolean.class, optionStorage)
                    .setId(ResourceLocation.tryBuild(TritiumCommon.MOD_ID, id))
                    .setName(Component.translatable(translationKey))
                    .setTooltip(Component.translatable(translationKey + ".tooltip"))
                    .setControl(TickBoxControl::new)
                    .setBinding(setter, getter)
                    .build());
            return this;
        }

        public GroupBuilder addInteger(String id, String translationKey,
                                       int min, int max, int step,
                                       IntFunction<Component> formatter,
                                       BiConsumer<TritiumConfigBase, Integer> setter,
                                       Function<TritiumConfigBase, Integer> getter) {
            builder.add(OptionImpl.createBuilder(int.class, optionStorage)
                    .setId(ResourceLocation.tryBuild(TritiumCommon.MOD_ID, id))
                    .setName(Component.translatable(translationKey))
                    .setTooltip(Component.translatable(translationKey + ".tooltip"))
                    .setControl(option -> new SliderControl(option, min, max, step, formatter::apply))
                    .setBinding(setter, getter)
                    .build());
            return this;
        }

        public OptionGroup build() {
            return builder.build();
        }
    }
}