package org.afv.collectatall.util;

import net.minecraft.util.math.MathHelper;

public class ColorUtil {
    public static int lerpColor(int start, int end, float t) {
        t = MathHelper.clamp(t, 0.0f, 1.0f);

        int a1 = (start >> 24) & 0xFF;
        int r1 = (start >> 16) & 0xFF;
        int g1 = (start >> 8)  & 0xFF;
        int b1 =  start        & 0xFF;

        int a2 = (end >> 24) & 0xFF;
        int r2 = (end >> 16) & 0xFF;
        int g2 = (end >> 8)  & 0xFF;
        int b2 =  end        & 0xFF;

        int a = MathHelper.lerp(t, a1, a2);
        int r = MathHelper.lerp(t, r1, r2);
        int g = MathHelper.lerp(t, g1, g2);
        int b = MathHelper.lerp(t, b1, b2);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
