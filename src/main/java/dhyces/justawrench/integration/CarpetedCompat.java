package dhyces.justawrench.integration;

import dhyces.justawrench.WrenchFunctions;
import net.mehvahdjukaar.carpeted.CarpetStairBlock;
import net.mehvahdjukaar.carpeted.Carpeted;
import net.mehvahdjukaar.carpeted.CarpetedBlockTile;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Optional;

public class CarpetedCompat {
    public static void addCompat() {
        WrenchFunctions.register(CarpetStairBlock.class, (context, currentState, mutableFlags) -> {
            Optional<CarpetedBlockTile> beOptional = context.getLevel().getBlockEntity(context.getClickedPos(), Carpeted.CARPET_STAIRS_TILE.get());
            if (beOptional.isPresent()) {
                CarpetedBlockTile be = beOptional.get();
                BlockState held = be.getHeldBlock(0);
                held = held.cycle(BlockStateProperties.HORIZONTAL_FACING);
                currentState = currentState.cycle(BlockStateProperties.HORIZONTAL_FACING);
                be.setHeldBlock(held, 0);
                mutableFlags.set(mutableFlags.get() | Block.UPDATE_KNOWN_SHAPE);
                return currentState;
            }
            return currentState;
        });
    }
}
