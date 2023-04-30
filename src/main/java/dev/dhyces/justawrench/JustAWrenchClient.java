package dev.dhyces.justawrench;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public final class JustAWrenchClient {
    JustAWrenchClient(IEventBus modBus, IEventBus forgeBus) {
        modBus.addListener(this::addToTabs);
    }

    private void addToTabs(final CreativeModeTabEvent.BuildContents event) {
        if (event.getTab() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(Registers.WRENCH);
        }
    }
}
