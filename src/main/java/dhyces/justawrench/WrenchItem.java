package dhyces.justawrench;

import com.google.common.collect.ImmutableMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;

public class WrenchItem extends Item {
    // Possibly move the wrenching system to a new data pack model? Example:
    /*
    * {
    *   "entries": [
    *     "#minecraft:stairs": "justawrench:stair_function"
    *   ]
    * }
    */

    private static final ImmutableMap<Class<? extends Block>, WrenchFunction> WRENCHING_MAP = Util.make(new ImmutableMap.Builder<Class<? extends Block>, WrenchFunction>(), builder -> {
        registerInternal(builder::put);
    }).build();

    private static void registerInternal(BiConsumer<Class<? extends Block>, WrenchFunction> consumer) {
        final WrenchFunction slabFunction = (context, currentState) -> {
            return switch (currentState.getValue(SlabBlock.TYPE)) {
                case TOP -> currentState.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
                case BOTTOM -> currentState.setValue(SlabBlock.TYPE, SlabType.TOP);
                default -> currentState;
            };
        };
        consumer.accept(SlabBlock.class, slabFunction);
        consumer.accept(WeatheringCopperSlabBlock.class, slabFunction);
        // Doors use horizontal facing, HOWEVER THEY ARE SORT OF GLITCHY VISUALLY
        final WrenchFunction horizontalFacingSupported = (context, currentState) -> {
            BlockState cycled = currentState.cycle(BlockStateProperties.HORIZONTAL_FACING);
            BlockPos behind = context.getClickedPos().relative(cycled.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite());
            if (context.getLevel().getBlockState(behind).isFaceSturdy(context.getLevel(), behind, cycled.getValue(BlockStateProperties.HORIZONTAL_FACING))) {
                currentState = cycled;
            }
            return currentState;
        };
        consumer.accept(LadderBlock.class, horizontalFacingSupported);
        consumer.accept(WallTorchBlock.class, horizontalFacingSupported);
        consumer.accept(RedstoneWallTorchBlock.class, horizontalFacingSupported);
        consumer.accept(RailBlock.class, (context, currentState) -> {
            if (!currentState.getValue(RailBlock.SHAPE).isAscending()) {
                if (currentState.getValue(RailBlock.SHAPE).equals(RailShape.EAST_WEST)) {
                    currentState = currentState.setValue(RailBlock.SHAPE, RailShape.SOUTH_EAST);
                } else {
                    currentState = currentState.cycle(RailBlock.SHAPE);
                }
            }
            return currentState;
        });
        final WrenchFunction railShapeStraightFunction = (context, currentState) -> {
            if (!currentState.getValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT).isAscending()) {
                if (currentState.getValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT).equals(RailShape.EAST_WEST)) {
                    currentState = currentState.setValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT, RailShape.NORTH_SOUTH);
                } else {
                    currentState = currentState.cycle(BlockStateProperties.RAIL_SHAPE_STRAIGHT);
                }
            }
            return currentState;
        };
        consumer.accept(PoweredRailBlock.class, railShapeStraightFunction);
        consumer.accept(DetectorRailBlock.class, railShapeStraightFunction);

        final WrenchFunction chestFunction = (context, currentState) -> {
            // TODO: TEST THIS IN MULTIPLAYER WHEN ONE PLAYER HAS THE CHEST OPEN
            Level level = context.getLevel();
            BlockPos clickedPos = context.getClickedPos();
            if (!currentState.getValue(ChestBlock.TYPE).equals(ChestType.SINGLE)) {
                BlockPos offsetPos = clickedPos.relative(ChestBlock.getConnectedDirection(currentState));
                level.setBlock(offsetPos, level.getBlockState(offsetPos).setValue(ChestBlock.TYPE, ChestType.SINGLE), Block.UPDATE_ALL);
            }
            currentState = currentState.cycle(ChestBlock.FACING);
            Direction currentFacing = currentState.getValue(ChestBlock.FACING);
            return currentState.setValue(ChestBlock.TYPE, getConnectedChestType(level, clickedPos, currentState, currentFacing));
        };

        consumer.accept(PistonBaseBlock.class, (context, currentState) -> currentState.getValue(PistonBaseBlock.EXTENDED) ? currentState : currentState.cycle(DirectionalBlock.FACING));
        consumer.accept(ChestBlock.class, chestFunction);
        consumer.accept(TrappedChestBlock.class, chestFunction);
        // Ender chest uses horizontal facing
        consumer.accept(HopperBlock.class, (context, currentState) -> currentState.cycle(HopperBlock.FACING));
        // Standing sign has rotation16
        consumer.accept(WallSignBlock.class, horizontalFacingSupported);
        // Banner has rotation16
        consumer.accept(WallBannerBlock.class, horizontalFacingSupported);
    }

    private static ChestType getConnectedChestType(Level level, BlockPos pos, BlockState state, Direction facing) {
        BlockState otherState = level.getBlockState(pos.relative(facing.getClockWise()));
        if (canChestConnect(state, facing, otherState)) {
            return ChestType.LEFT;
        }
        otherState = level.getBlockState(pos.relative(facing.getCounterClockWise()));
        if (canChestConnect(state, facing, otherState)) {
            return ChestType.RIGHT;
        }
        return ChestType.SINGLE;
    }

    private static boolean canChestConnect(BlockState state, Direction facing, BlockState neighborState) {
        return neighborState.is(state.getBlock()) && neighborState.getValue(ChestBlock.TYPE).equals(ChestType.SINGLE) && neighborState.getValue(ChestBlock.FACING).equals(facing);
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
            if (eventModifiedState != null && eventModifiedState != state) {
                if (!level.isClientSide) {
                    level.setBlock(pos, eventModifiedState, Block.UPDATE_ALL);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (WRENCHING_MAP.containsKey(state.getBlock().getClass())) {
                if (!level.isClientSide) {
                    WrenchFunction function = WRENCHING_MAP.get(state.getBlock().getClass());
                    BlockState wrenchedState = function.wrench(pContext, state);
                    if (wrenchedState != state) {
                        level.setBlock(pos, wrenchedState, Block.UPDATE_ALL);
                    }
                }
                level.playSound(null, pos, Registers.WRENCH_SOUND.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            } else if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                if (!level.isClientSide) {
                    level.setBlock(pos, state.cycle(BlockStateProperties.HORIZONTAL_FACING), Block.UPDATE_ALL);
                }
                level.playSound(null, pos, Registers.WRENCH_SOUND.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            } else if (state.hasProperty(BlockStateProperties.FACING)) {
                if (!level.isClientSide) {
                    level.setBlock(pos, state.cycle(BlockStateProperties.FACING), Block.UPDATE_ALL);
                }
                level.playSound(null, pos, Registers.WRENCH_SOUND.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            } else if (state.hasProperty(BlockStateProperties.ROTATION_16)) {
                if (!level.isClientSide) {
                    level.setBlock(pos, state.cycle(BlockStateProperties.ROTATION_16), Block.UPDATE_ALL);
                }
                level.playSound(null, pos, Registers.WRENCH_SOUND.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            } else if (state.hasProperty(BlockStateProperties.AXIS)) {
                if (!level.isClientSide) {
                    level.setBlock(pos, state.cycle(BlockStateProperties.AXIS), Block.UPDATE_ALL);
                }
                level.playSound(null, pos, Registers.WRENCH_SOUND.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            } else {
                JustAWrench.LOGGER.error("No wrench behavior found for %s".formatted(Registry.BLOCK.getKey(state.getBlock())));
                level.playSound(null, pos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1.0f, 1.7f);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useOn(pContext);
    }

    @Override
    public boolean isValidRepairItem(ItemStack pStack, ItemStack pRepairCandidate) {
        return pRepairCandidate.is(JustAWrench.REPAIRS_WRENCH);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return JustAWrench.WRENCH.equals(toolAction);
    }

    public interface WrenchFunction {
        @Nonnull
        BlockState wrench(UseOnContext context, BlockState currentState);
    }
}
