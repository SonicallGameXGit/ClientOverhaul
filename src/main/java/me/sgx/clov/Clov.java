package me.sgx.clov;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class Clov implements ModInitializer {
    public static final String MOD_ID = "clov";
    public static final Map<String, ModContainer> MODS = new HashMap<>();

    @Override
    public void onInitialize() {
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            final ModMetadata metadata = mod.getMetadata();
            final String id = metadata.getId();

            // Source: https://github.com/TerraformersMC/ModMenu/blob/26.1/src/main/java/com/terraformersmc/modmenu/util/mod/fabric/FabricMod.java (line 104)
            /* Hardcode parents and badges for Fabric API & Fabric Loader */
            if (
                id.startsWith("fabric") &&
                (
                    metadata.containsCustomValue("fabric-api:module-lifecycle") ||
                    metadata.getProvides().contains("fabricloader") ||
                    metadata.getProvides().contains("fabric-api") ||
                    metadata.getProvides().contains("fabric") ||
                    id.equals("fabric-language-kotlin") ||
                    id.equals("fabricloader") ||
                    id.equals("fabric-api") ||
                    id.equals("fabric")
                )
            ) {
                continue;
            }
            if (id.equals("java") || id.equals("minecraft") || id.equals("mixinextras")) {
                continue;
            }
            if (mod.getContainingMod().isPresent()) {
                continue;
            }
            
            MODS.put(id, mod);
            System.out.println("Found mod: " + id);
        }
    }
}
