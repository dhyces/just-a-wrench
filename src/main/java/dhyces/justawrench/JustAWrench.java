package dhyces.justawrench;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(JustAWrench.MODID)
public class JustAWrench {
    public static final String MODID = "justawrench";
    public static final Logger LOGGER = LogManager.getLogger(JustAWrench.class);

    public static final TagKey<Block> WRENCHABLE = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MODID, "wrenchable"));

    public static final ToolAction WRENCH = ToolAction.get("justawrench:wrench");

    private static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(Registry.ITEM_REGISTRY, MODID);

    public JustAWrench() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEM_REGISTER.register(modBus);
        ITEM_REGISTER.register("wrench", () -> new WrenchItem(new Item.Properties().durability(300).tab(CreativeModeTab.TAB_TOOLS)));
    }
}
