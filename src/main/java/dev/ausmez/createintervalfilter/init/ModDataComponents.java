package dev.ausmez.createintervalfilter.init;

import com.mojang.serialization.Codec;
import dev.ausmez.createintervalfilter.CreateIntervalFilter;
import dev.ausmez.createintervalfilter.item.ExecutionMode;
import dev.ausmez.createintervalfilter.item.PlacementMethod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    private static final DeferredRegister.DataComponents REGISTER =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CreateIntervalFilter.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> INTERVAL =
            REGISTER.registerComponentType("interval",
                    builder -> builder
                            .persistent(Codec.intRange(1, 64))
                            .networkSynchronized(ByteBufCodecs.VAR_INT)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> OFFSET =
            REGISTER.registerComponentType("offset",
                    builder -> builder
                            .persistent(Codec.intRange(0, 16))
                            .networkSynchronized(ByteBufCodecs.VAR_INT)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ExecutionMode>> EXECUTION_MODE =
            REGISTER.registerComponentType("execution_mode",
                    builder -> builder
                            .persistent(ExecutionMode.CODEC)
                            .networkSynchronized(ExecutionMode.STREAM_CODEC)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PlacementMethod>> PLACEMENT_METHOD =
            REGISTER.registerComponentType("placement_method",
                    builder -> builder
                            .persistent(PlacementMethod.CODEC)
                            .networkSynchronized(PlacementMethod.STREAM_CODEC)
            );

    public static void register(IEventBus modBus) {
        REGISTER.register(modBus);
    }
}
