package dhyces.justawrench;

import com.google.common.collect.ImmutableMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class WrenchItem extends Item {
    // Possibly move the wrenching system to a new data pack model? Example:
    /*
    * {
    *   "entries": [
    *     "#minecraft:stairs": "facing" - BAD IDEA, NEVERMIND
    *   ]
    * }
    */

    ImmutableMap<Block, Property<?>> WRENCHING_MAP = Util.make(new ImmutableMap.Builder<Block, Property<?>>(), builder -> {
        registerInternal(builder::put);
    }).build();

    private static void registerInternal(BiConsumer<Block, Property<?>> consumer) {
        consumer.accept(Blocks.HOPPER, HopperBlock.FACING);
    }
    
    public WrenchItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.is(JustAWrench.WRENCHABLE)) {
            BlockState eventModifiedState = ForgeEventFactory.onToolUse(state, pContext, JustAWrench.WRENCH, false);
            if (eventModifiedState != null) {
                if (!level.isClientSide) {
                    level.setBlock(pos, eventModifiedState, Block.UPDATE_ALL);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                level.setBlock(pos, state.cycle(BlockStateProperties.HORIZONTAL_FACING), Block.UPDATE_ALL);
            } else if (false /* TODO: Add API for adding other behavior*/) {

            } else {
                JustAWrench.LOGGER.error("No wrench behavior found for %s".formatted(Registry.BLOCK.getKey(state.getBlock())));
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(pContext);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return JustAWrench.WRENCH.equals(toolAction);
    }
}
