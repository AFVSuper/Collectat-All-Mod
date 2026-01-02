package org.afv.collectatall.init;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRuleCategory;
import org.afv.collectatall.util.ObtainMode;

public class ModGamerules {
    private static final Identifier RARE_BROADCAST_ID = Identifier.of("rare_broadcast");
    private static final Identifier MODIFY_OBTAINED_ID = Identifier.of("modify_obtained");

    public  static final GameRule<Boolean> RARE_BROADCAST_GAMERULE = GameRuleBuilder
            .forBoolean(false) //default value declaration
            .category(GameRuleCategory.MISC)
            .buildAndRegister(RARE_BROADCAST_ID);
    public  static final GameRule<ObtainMode> MODIFY_OBTAINED_GAMERULE = GameRuleBuilder
            .forEnum(ObtainMode.NONE) //default value declaration
            .category(GameRuleCategory.MISC)
            .buildAndRegister(MODIFY_OBTAINED_ID);

    public static void init() {}
}
