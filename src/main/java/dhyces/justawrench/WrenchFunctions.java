package dhyces.justawrench;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.SlabType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class WrenchFunctions {
    // Possibly move the wrenching system to a new data pack model? Example:
    /*
     * {
     *   "entries": [
     *     "#minecraft:stairs": "justawrench:stair_function"
     *   ]
     * }
     */

    private static final Map<Class<? extends Block>, WrenchFunction> MUTABLE_API_MAP = new HashMap<>();

    private static final Supplier<ImmutableMap<Class<? extends Block>, WrenchFunction>> WRENCHING_MAP = Suppliers.memoize(() ->
            Util.make(new ImmutableMap.Builder<Class<? extends Block>, WrenchFunction>(), builder -> {
                registerInternal(builder::put);
                builder.putAll(MUTABLE_API_MAP);
            }).build()
    );

    /**
     * Use this method to register a new function to your custom block class
     * @param blockClass
     * @param function
     * @return Whether the map already contains the class and failed
     */
    public static boolean register(Class<? extends Block> blockClass, WrenchFunction function) {
        if (MUTABLE_API_MAP.containsKey(blockClass)) {
            return false;
        }
        MUTABLE_API_MAP.put(blockClass, function);
        return true;
    }

    private static void registerInternal(BiConsumer<Class<? extends Block>, WrenchFunction> consumer) {
        consumer.accept(SlabBlock.class, WrenchFunctions.SLAB_TYPE);
        consumer.accept(WeatheringCopperSlabBlock.class, WrenchFunctions.SLAB_TYPE);
        // Doors use horizontal facing, HOWEVER THEY ARE SORT OF GLITCHY VISUALLY

        consumer.accept(LadderBlock.class, WrenchFunctions.SUPPORTED_HORIZONTAL_FACING);
        consumer.accept(WallTorchBlock.class, WrenchFunctions.SUPPORTED_HORIZONTAL_FACING);
        consumer.accept(RedstoneWallTorchBlock.class, WrenchFunctions.SUPPORTED_HORIZONTAL_FACING);
        consumer.accept(RailBlock.class, (context, currentState, mutableFlags) -> {
            if (!currentState.getValue(RailBlock.SHAPE).isAscending()) {
                if (currentState.getValue(RailBlock.SHAPE).equals(RailShape.EAST_WEST)) {
                    currentState = currentState.setValue(RailBlock.SHAPE, RailShape.SOUTH_EAST);
                } else {
                    currentState = currentState.cycle(RailBlock.SHAPE);
                }
            }
            return currentState;
        });

        consumer.accept(PoweredRailBlock.class, WrenchFunctions.RAIL_SHAPE_STRAIGHT);
        consumer.accept(DetectorRailBlock.class, WrenchFunctions.RAIL_SHAPE_STRAIGHT);

        consumer.accept(PistonBaseBlock.class, (context, currentState, mutableFlags) -> currentState.getValue(PistonBaseBlock.EXTENDED) ? currentState : currentState.cycle(DirectionalBlock.FACING));
        consumer.accept(ChestBlock.class, WrenchFunctions.CHEST);
        consumer.accept(TrappedChestBlock.class, WrenchFunctions.CHEST);
        // Ender chest uses horizontal facing
        consumer.accept(HopperBlock.class, (context, currentState, mutableFlags) -> currentState.cycle(HopperBlock.FACING));
        // Standing sign has rotation16
        consumer.accept(WallSignBlock.class, WrenchFunctions.SUPPORTED_HORIZONTAL_FACING);
        // Banner has rotation16
        consumer.accept(WallBannerBlock.class, WrenchFunctions.SUPPORTED_HORIZONTAL_FACING);
    }

    public static Optional<WrenchFunction> get(Block block) {
        return Optional.ofNullable(WRENCHING_MAP.get().get(block.getClass()));
    }

    @Nullable
    public static WrenchFunction getOrNull(Block block) {
        return WRENCHING_MAP.get().get(block.getClass());
    }

    public static boolean has(Block block) {
        return WRENCHING_MAP.get().containsKey(block.getClass());
    }

    public static final WrenchFunction SUPPORTED_HORIZONTAL_FACING = (context, currentState, mutableFlags) -> {
        BlockState cycled = currentState.cycle(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos behind = context.getClickedPos().relative(cycled.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite());
        if (context.getLevel().getBlockState(behind).isFaceSturdy(context.getLevel(), behind, cycled.getValue(BlockStateProperties.HORIZONTAL_FACING))) {
            currentState = cycled;
        }
        return currentState;
    };

    public static final WrenchFunction SLAB_TYPE = (context, currentState, mutableFlags) -> {
        return switch (currentState.getValue(SlabBlock.TYPE)) {
            case TOP -> currentState.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
            case BOTTOM -> currentState.setValue(SlabBlock.TYPE, SlabType.TOP);
            default -> currentState;
        };
    };

    public static final WrenchFunction CHEST = (context, currentState, mutableFlags) -> {
        if (!currentState.getValue(ChestBlock.TYPE).equals(ChestType.SINGLE)) {
            return currentState;
        }
        return currentState.cycle(ChestBlock.FACING);
    };

//    private static ChestType getConnectedChestType(Level level, BlockPos pos, BlockState state, Direction facing) {
//        BlockState otherState = level.getBlockState(pos.relative(facing.getClockWise()));
//        if (canChestConnect(state, facing, otherState)) {
//            return ChestType.LEFT;
//        }
//        otherState = level.getBlockState(pos.relative(facing.getCounterClockWise()));
//        if (canChestConnect(state, facing, otherState)) {
//            return ChestType.RIGHT;
//        }
//        return ChestType.SINGLE;
//    }
//
//    private static boolean canChestConnect(BlockState state, Direction facing, BlockState neighborState) {
//        return neighborState.is(state.getBlock()) && neighborState.getValue(ChestBlock.TYPE).equals(ChestType.SINGLE) && neighborState.getValue(ChestBlock.FACING).equals(facing);
//    }

    public static final WrenchFunction RAIL_SHAPE_STRAIGHT = (context, currentState, mutableFlags) -> {
        if (!currentState.getValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT).isAscending()) {
            if (currentState.getValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT).equals(RailShape.EAST_WEST)) {
                currentState = currentState.setValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT, RailShape.NORTH_SOUTH);
            } else {
                currentState = currentState.cycle(BlockStateProperties.RAIL_SHAPE_STRAIGHT);
            }
        }
        return currentState;
    };

    public interface WrenchFunction {
        /**
         *
         * @param context Context from the useOn method
         * @param currentState
         * @param mutableFlags This stores a default flag, Block.UPDATE_ALL. Useful if you require different flags
         * @return The new BlockState to place. If it's the same as before, does nothing
         */
        @Nonnull
        BlockState wrench(UseOnContext context, BlockState currentState, AtomicInteger mutableFlags);
    }
}
