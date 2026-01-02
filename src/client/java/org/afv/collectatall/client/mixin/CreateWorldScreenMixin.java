package org.afv.collectatall.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import org.afv.collectatall.CollectatAll;
import org.afv.collectatall.util.CollectatallMode;
import org.afv.collectatall.util.CollectatallModeState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {
    @Unique
    private CollectatallMode selectedMode = CollectatallMode.NORMAL;
    @Unique
    private TextFieldWidget intField;
    @Unique
    private int selectedInt = 0;

    @Shadow
    @Final
    private TabManager tabManager;

    @Unique
    private CyclingButtonWidget<CollectatallMode> modeButton;

    @Inject(method = "init", at = @At("TAIL"))
    private void addModeButton(CallbackInfo ci) {
        CreateWorldScreen screen = (CreateWorldScreen) (Object) this;

        int modeWidth = 210;
        int timeFieldWidht = 62;
        int height = 20;
        int x = screen.width / 2 - modeWidth / 2;
        int y = screen.height / 6 + 120; // adjust vertical position

        modeButton = CyclingButtonWidget.builder(
                        (CollectatallMode value) -> Text.translatable(value.getTranslationKey()),
                        (Supplier<CollectatallMode>) () -> selectedMode
                )
                .values(CollectatallMode.values())
                .build(
                        x, y, modeWidth, height,
                        Text.translatable("collectatall.world-gen.mode"),
                        (button, value) -> {
                            selectedMode = value;
                            button.setTooltip(Tooltip.of(value.getDescription()));
                        }
                );

        intField = new TextFieldWidget(
                MinecraftClient.getInstance().textRenderer,
                x + 8 + 140,
                y, // below the mode button
                timeFieldWidht,
                height,
                Text.translatable("collectatall.world-gen.int_field")
        );

        intField.setPlaceholder(Text.translatable("collectatall.world-gen.minutes"));
        intField.setMaxLength(9); // reasonable upper bound
        intField.setVisible(false);

        // Only allow digits
        intField.setChangedListener(text -> {
            if (text.isEmpty()) {
                selectedInt = 0;
                return;
            }

            try {
                int value = Integer.parseInt(text);
                selectedInt = Math.max(0, value);
            } catch (NumberFormatException ignored) {
                selectedInt = 0;
            }
        });

        ((ScreenAccessor) screen).callAddDrawableChild(intField);
        // Add it to the screen initially, we'll control visibility in render
        ((ScreenAccessor) screen).callAddDrawableChild(modeButton);
        modeButton.visible = false; // hidden by default
    }

    @Inject(method = "createLevel", at = @At("TAIL"))
    private void afterCreateWorld(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance(); // safe here, running on client
        IntegratedServer server = client.getServer(); // now we have the server instance
        if (server != null) {
            // CollectatAll.LOGGER.info("Creation: {}", selectedInt);
            CollectatallModeState state = CollectatallModeState.getServerState(server);
            state.setMode(selectedMode);

            if (selectedMode == CollectatallMode.TIMED && selectedInt > 0) {
                state.setTime(selectedInt);
            } else if (selectedMode == CollectatallMode.COUNT && selectedInt > 0) {
                state.setCount(selectedInt);
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Use title comparison since GameTab class is private
        boolean bl = tabManager.getCurrentTab() != null &&
                tabManager.getCurrentTab().getTitle().equals(Text.translatable("createWorld.tab.game.title"));

        modeButton.visible = bl;
        boolean textVis = selectedMode == CollectatallMode.TIMED || selectedMode == CollectatallMode.COUNT;
        if (textVis) {
            modeButton.setWidth(140);
        } else modeButton.setWidth(210);
        modeButton.setTooltip(Tooltip.of(selectedMode.getDescription()));
        intField.visible = bl && textVis;
        if (selectedMode == CollectatallMode.TIMED) {
            intField.setPlaceholder(Text.translatable("collectatall.world-gen.minutes"));
        } else if (selectedMode == CollectatallMode.COUNT) {
            intField.setPlaceholder(Text.translatable("collectatall.world-gen.count"));
        }
    }

    @Inject(method = "refreshWidgetPositions", at = @At("TAIL"))
    private void onRefreshPositions(CallbackInfo ci) {
        CreateWorldScreen screen = (CreateWorldScreen) (Object) this;
        int x = screen.width / 2 - 105;
        int y = screen.height / 6 + 120;

        if (modeButton != null) {
            modeButton.setX(x);
            modeButton.setY(y);
        }

        if (intField != null) {
            intField.setX(x + 8 + 140);
            intField.setY(y);
        }
    }

    @Unique
    public CollectatallMode getSelectedMode() {
        return selectedMode;
    }

    @Unique
    public void setSelectedMode(CollectatallMode mode) {
        selectedMode = mode;
    }
}