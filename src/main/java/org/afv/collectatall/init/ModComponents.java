package org.afv.collectatall.init;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.afv.collectatall.CollectatAll;

public class ModComponents {
    public static final ComponentType<Boolean> ITEM_CHECKED =
            Registry.register(
                    Registries.DATA_COMPONENT_TYPE,
                    Identifier.of(CollectatAll.ModID, "item_checked"),
                    ComponentType.<Boolean>builder().codec(Codec.BOOL).build()
            );

    public static void init() {}
}
