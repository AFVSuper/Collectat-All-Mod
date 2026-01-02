package org.afv.collectatall.client.mixin;

import net.minecraft.client.toast.AdvancementToast;
import net.minecraft.client.toast.RecipeToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import org.afv.collectatall.client.CollectatAllClient;
import org.afv.collectatall.client.mode.CollectatallModeStateClient;
import org.afv.collectatall.client.timer.SpeedrunHud;
import org.afv.collectatall.client.timer.TimerHud;
import org.afv.collectatall.util.CollectatallMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public class ToastManagerMixin {

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void filterRecipeToast(Toast toast, CallbackInfo ci) {
        if (!(toast instanceof AdvancementToast)) {
            ci.cancel();
        } else {
            CollectatallMode mode = CollectatallModeStateClient.getMode();
            if (mode == CollectatallMode.SPEEDRUN || mode == CollectatallMode.COUNT) SpeedrunHud.toast = true;
            else if (mode == CollectatallMode.TIMED) TimerHud.toast = true;
        }
    }
}