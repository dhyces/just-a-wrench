package dev.dhyces.justawrench;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class WrenchItem extends Item {

    public WrenchItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = pContext.getPlayer();
        if (state.is(JustAWrench.WRENCHABLE)) {
            BlockState eventModifiedState = ForgeEventFactory.onToolUse(state, pContext, JustAWrench.WRENCH, false);
            if (eventModifiedState != null && eventModifiedState != state) {
                if (!level.isClientSide) {
                    level.setBlock(pos, eventModifiedState, Block.UPDATE_ALL);
                    if (player != null) {
                        pContext.getItemInHand().hurtAndBreak(1, player, player1 -> player1.broadcastBreakEvent(pContext.getHand()));
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (WrenchFunctions.has(state.getBlock())) {
                if (!level.isClientSide) {
                    WrenchFunctions.WrenchFunction function = WrenchFunctions.getOrNull(state.getBlock());
                    AtomicInteger flags = new AtomicInteger(Block.UPDATE_ALL);
                    BlockState wrenchedState = function.wrench(pContext, state, flags);
                    if (wrenchedState != state) {
                        level.setBlock(pos, wrenchedState, flags.get());
                        if (player != null) {
                            pContext.getItemInHand().hurtAndBreak(1, player, player1 -> player1.broadcastBreakEvent(pContext.getHand()));
                        }
                    }
                }
                level.playSound(null, pos, Registers.WRENCH_SOUND.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            } else if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                if (!level.isClientSide) {
                    level.setBlock(pos, state.cycle(BlockStateProperties.HORIZONTAL_FACING), Block.UPDATE_ALL);
                    if (player != null) {
                        pContext.getItemInHand().hurtAndBreak(1, player, player1 -> player1.broadcastBreakEvent(pContext.getHand()));
                    }
                }
                level.playSound(null, pos, Registers.WRENCH_SOUND.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            } else if (state.hasProperty(BlockStateProperties.FACING)) {
                if (!level.isClientSide) {
                    level.setBlock(pos, state.cycle(BlockStateProperties.FACING), Block.UPDATE_ALL);
                    if (player != null) {
                        pContext.getItemInHand().hurtAndBreak(1, player, player1 -> player1.broadcastBreakEvent(pContext.getHand()));
                    }
                }
                level.playSound(null, pos, Registers.WRENCH_SOUND.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            } else if (state.hasProperty(BlockStateProperties.ROTATION_16)) {
                if (!level.isClientSide) {
                    level.setBlock(pos, state.cycle(BlockStateProperties.ROTATION_16), Block.UPDATE_ALL);
                    if (player != null) {
                        pContext.getItemInHand().hurtAndBreak(1, player, player1 -> player1.broadcastBreakEvent(pContext.getHand()));
                    }
                }
                level.playSound(null, pos, Registers.WRENCH_SOUND.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            } else if (state.hasProperty(BlockStateProperties.AXIS)) {
                if (!level.isClientSide) {
                    level.setBlock(pos, state.cycle(BlockStateProperties.AXIS), Block.UPDATE_ALL);
                    if (player != null) {
                        pContext.getItemInHand().hurtAndBreak(1, player, player1 -> player1.broadcastBreakEvent(pContext.getHand()));
                    }
                }
                level.playSound(null, pos, Registers.WRENCH_SOUND.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            } else {
                if (!level.isClientSide) {
                    JustAWrench.LOGGER.debug("No wrench behavior found for %s".formatted(BuiltInRegistries.BLOCK.getKey(state.getBlock())));
                }
                level.playSound(null, pos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.2f, 1.7f);
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
}
