package me.sgx.clov.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.sgx.clov.client.screen.ModsScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(at = @At("HEAD"), method = "init()V")
    private void init(CallbackInfo callbackInfo) {
        final int spacing = 4;
        this.addRenderableWidget(Button.builder(
            Component.literal("Mods"),
            button -> this.minecraft.setScreen(new ModsScreen(Component.literal("Mods")))
        ).bounds(this.width / 2 + 100 + spacing, this.height / 4 + 48, 64, 20).build());
    }
}
