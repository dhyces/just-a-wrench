package dhyces.justawrench.integration;

import net.minecraftforge.fml.ModList;

public class Compats {
    public static boolean hasCarpeted() {
        return ModList.get().isLoaded("carpeted");
    }
}
