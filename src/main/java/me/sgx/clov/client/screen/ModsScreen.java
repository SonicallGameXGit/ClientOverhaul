package me.sgx.clov.client.screen;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import com.mojang.blaze3d.platform.NativeImage;

import me.sgx.clov.Clov;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ModsScreen extends Screen {
    private static class ModEntry extends ObjectSelectionList.Entry<ModEntry> {
        private final Minecraft minecraft;
        private final @NonNull String name;
        private final @NonNull Identifier icon;

        public ModEntry(final Minecraft minecraft, final @NonNull String name, final @NonNull Identifier icon) {
            this.minecraft = minecraft;
            this.name = name;
            this.icon = icon;
        }

        @Override
        public @NonNull Component getNarration() {
            return Component.literal("Mod: " + this.name); // TODO: Implement proper narration based on mod name and other details (maybe)
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float delta) {
            // final int contentWidth = this.getContentWidth();
            final int contentHeight = this.getContentHeight();
            
            final int spacing = 2;
            int x = this.getContentX();
            final int y = this.getContentY();

            if (this.icon != null) {
                graphics.blit(
                    this.icon,
                    x,
                    y,
                    x + contentHeight,
                    y + contentHeight,
                    0, 1, 0, 1
                );
            }
            x += contentHeight + spacing;
            graphics.text(this.minecraft.font, this.name, x, y + 2, 0xffffffff);
            x += this.minecraft.font.width(this.name) + spacing;
        }
    }
    private static class ModList extends ObjectSelectionList<ModEntry> {
        private final List<DynamicTexture> textures = new ArrayList<>();
        public ModList(final Minecraft minecraft, final int width, final int height, final int y, final int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
            for (ModContainer mod : Clov.MODS.values()) {
                ModMetadata metadata = mod.getMetadata();
                final String id = metadata.getId();

                Identifier icon = Identifier.fromNamespaceAndPath(Clov.MOD_ID, "default_mod_icon.png");

                String iconId = metadata.getIconPath(48).orElse("assets/" + id + "/icon.png");
                Optional<Path> iconPath = mod.findPath(iconId);
                if (iconPath.isPresent()) {
                    // Source:  https://github.com/TerraformersMC/ModMenu/blob/26.1/src/main/java/com/terraformersmc/modmenu/util/mod/fabric/FabricIconHandler.java (line 38)
                    try (InputStream inputStream = Files.newInputStream(iconPath.get())) {
                        NativeImage image = NativeImage.read(Objects.requireNonNull(inputStream));
                        Validate.validState(image.getHeight() == image.getWidth(), "Must be square icon");
                        
                        Identifier textureId = Identifier.fromNamespaceAndPath(Clov.MOD_ID, "mod_icon_" + id);
                        DynamicTexture texture = new DynamicTexture(() -> textureId.toString(), image);
                        this.minecraft.getTextureManager().register(textureId, texture);
                        this.textures.add(texture);
                        icon = textureId;
                    } catch (Exception exception) {
                        // Log the error and continue with the default icon
                        System.err.println("Failed to load icon for mod " + id + ": " + exception.getMessage());
                    }
                }

                this.addEntry(new ModEntry(
                    this.minecraft,
                    metadata.getName(),
                    icon
                ));
            }
        }

        public void close() {
            for (DynamicTexture texture : this.textures) {
                texture.close();
            }
        }
    }

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 33, 60);
    private @Nullable ModList modList;

    public ModsScreen(Component title) {
        super(title);
    }
    
    @Override
    protected void init() {
        super.init();
        this.modList = new ModList(this.minecraft, this.width, this.layout.getContentHeight(), this.layout.getHeaderHeight(), 48);
        this.layout.addTitleHeader(this.title, this.font);
        this.layout.addToContents(this.modList);

        this.layout.visitWidgets(widget -> this.addRenderableWidget(widget));
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.modList != null) {
            this.modList.updateSize(this.width, this.layout);
        }
    }

    @Override
    public void onClose() {
        if (this.modList != null) {
            this.modList.close();
        }
        super.onClose();
    }
}
