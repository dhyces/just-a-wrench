package dhyces.justawrench;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class Registers {

    private static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(Registry.ITEM_REGISTRY, JustAWrench.MODID);
    private static final DeferredRegister<SoundEvent> SOUND_EVENT_REGISTER = DeferredRegister.create(Registry.SOUND_EVENT_REGISTRY, JustAWrench.MODID);

    public static final RegistryObject<WrenchItem> WRENCH = ITEM_REGISTER.register("wrench", () -> new WrenchItem(new Item.Properties().durability(300).tab(CreativeModeTab.TAB_TOOLS)));

    public static final RegistryObject<SoundEvent> WRENCH_SOUND = SOUND_EVENT_REGISTER.register("wrench", () -> new SoundEvent(new ResourceLocation(JustAWrench.MODID, "item.justawrench.wrench")));

    public static void register(IEventBus modBus) {
        ITEM_REGISTER.register(modBus);
        SOUND_EVENT_REGISTER.register(modBus);
    }
}
