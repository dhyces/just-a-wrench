package dev.dhyces.justawrench;

import dev.dhyces.justawrench.integration.CarpetedCompat;
import dev.dhyces.justawrench.integration.Compats;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(JustAWrench.MODID)
public class JustAWrench {
    public static final String MODID = "justawrench";
    public static final Logger LOGGER = LoggerFactory.getLogger(JustAWrench.class);

    public static final TagKey<Block> WRENCHABLE = TagKey.create(Registries.BLOCK, new ResourceLocation(MODID, "wrenchable"));
    public static final TagKey<Item> REPAIRS_WRENCH = TagKey.create(Registries.ITEM, new ResourceLocation(MODID, "repairs_wrench"));

    public static final ToolAction WRENCH = ToolAction.get("wrench");

    public JustAWrench() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        Registers.register(modBus);

        if (FMLLoader.getDist().isClient()) {
            new JustAWrenchClient(modBus, MinecraftForge.EVENT_BUS);
        }

        modBus.addListener(this::onCommonSetup);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (Compats.hasCarpeted()) {
                CarpetedCompat.addCompat();
            }
        });
    }
}
