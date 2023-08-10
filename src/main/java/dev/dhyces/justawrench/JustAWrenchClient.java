package dev.dhyces.justawrench;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public final class JustAWrenchClient {
    JustAWrenchClient(IEventBus modBus, IEventBus forgeBus) {
        modBus.addListener(this::addToTabs);
    }

    private void addToTabs(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(Registers.WRENCH);
        }
    }
}
