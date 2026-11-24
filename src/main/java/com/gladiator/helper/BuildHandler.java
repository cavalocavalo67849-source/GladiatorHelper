
package com.gladiator.helper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;

public class BuildHandler {

    private static final List<BlockPos> pendingPositions =
        new ArrayList<BlockPos>();

    private static int pendingSlot = -1;
    private static int placed = 0;
    private static int currentIndex = 0;
    private static int waitTicks = 0;
    private static int attempts = 0;

    private static BlockPos awaitingPosition = null;

    private static final int CONFIRMATION_WAIT = 20;
    private static final int MAX_ATTEMPTS = 2;

    public static void build() {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (!pendingPositions.isEmpty()) {
            return;
        }

        EntityPlayerSP player = mc.thePlayer;
        WorldClient world = mc.theWorld;

        EntityLivingBase target = findNearestMob(player, world);

        if (target == null) {
            player.addChatMessage(
                new net.minecraft.util.ChatComponentText(
                    "§cNenhum mob encontrado em 5 blocos."
                )
            );
            return;
        }

        Item wallItem = Item.getItemFromBlock(
            Blocks.cobblestone_wall
        );

        pendingSlot = findWallSlot(
            player.inventory.mainInventory,
            wallItem
        );

        if (pendingSlot == -1) {
            player.addChatMessage(
                new net.minecraft.util.ChatComponentText(
                    "§cVocê precisa de muros de pedregulho!"
                )
            );
            return;
        }

        EnumFacing facing = player.getHorizontalFacing();
        AxisAlignedBB box = target.getEntityBoundingBox();

        int feetY = MathHelper.floor_double(target.posY);

        BlockPos leftPos = getSidePosition(
            box, feetY, facing.rotateYCCW()
        );

        BlockPos rightPos = getSidePosition(
            box, feetY, facing.rotateY()
        );

        BlockPos topPos = new BlockPos(
            target.posX,
            MathHelper.floor_double(
                target.posY + target.height
            ) + 1,
            target.posZ
        );

        pendingPositions.clear();
        pendingPositions.add(leftPos);
        pendingPositions.add(rightPos);
        pendingPositions.add(topPos);

        placed = 0;
        currentIndex = 0;
        waitTicks = 0;
        attempts = 0;
        awaitingPosition = null;

        player.addChatMessage(
            new net.minecraft.util.ChatComponentText(
                "§a§lGladiatorHelper §fConstruindo..."
            )
        );
    }

    public static void tick() {
        Minecraft mc = Minecraft.getMinecraft();

        if (pendingPositions.isEmpty()) {
            return;
        }

        if (mc.thePlayer == null || mc.theWorld == null) {
            reset();
            return;
        }

        EntityPlayerSP player = mc.thePlayer;
        WorldClient world = mc.theWorld;

        // Aguarda a confirmação do servidor.
        if (awaitingPosition != null) {
            waitTicks++;

            if (world.getBlockState(awaitingPosition).getBlock()
                == Blocks.cobblestone_wall) {

                placed++;
                currentIndex++;
                awaitingPosition = null;
                waitTicks = 0;
                attempts = 0;

                if (currentIndex >= pendingPositions.size()) {
                    finish(player);
                }

                return;
            }

            if (waitTicks < CONFIRMATION_WAIT) {
                return;
            }

            // A colocação não foi confirmada.
            awaitingPosition = null;
            waitTicks = 0;
            attempts++;

            if (attempts >= MAX_ATTEMPTS) {
                fail(player);
                return;
            }
        }

        if (currentIndex >= pendingPositions.size()) {
            finish(player);
            return;
        }

        ItemStack stack =
            player.inventory.getStackInSlot(pendingSlot);

        if (stack == null || stack.stackSize <= 0) {
            fail(player);
            return;
        }

        BlockPos pos = pendingPositions.get(currentIndex);

        if (!world.isAirBlock(pos)) {
            if (world.getBlockState(pos).getBlock()
                == Blocks.cobblestone_wall) {
                placed++;
            }

            currentIndex++;
            attempts = 0;

            if (currentIndex >= pendingPositions.size()) {
                finish(player);
            }

            return;
        }

        if (placeAtSupport(
            mc, player, world, pos, pendingSlot
        )) {
            awaitingPosition = pos;
            waitTicks = 0;
        } else {
            attempts++;

            if (attempts >= MAX_ATTEMPTS) {
                fail(player);
            }
        }
    }

    private static void finish(EntityPlayerSP player) {
        player.addChatMessage(
            new net.minecraft.util.ChatComponentText(
                "§aGladiatorHelper: §f" +
                placed + " de 3 muros confirmados."
            )
        );

        reset();
    }

    private static void fail(EntityPlayerSP player) {
        player.addChatMessage(
            new net.minecraft.util.ChatComponentText(
                "§cGladiatorHelper: §fNão foi possível confirmar " +
                "a colocação. " + placed + " de 3 muros confirmados."
            )
        );

        reset();
    }

    private static void reset() {
        pendingPositions.clear();
        pendingSlot = -1;
        placed = 0;
        currentIndex = 0;
        waitTicks = 0;
        attempts = 0;
        awaitingPosition = null;
    }

    private static BlockPos getSidePosition(
        AxisAlignedBB box,
        int y,
        EnumFacing side
    ) {
        int x = MathHelper.floor_double(
            (box.minX + box.maxX) / 2.0
        );

        int z = MathHelper.floor_double(
            (box.minZ + box.maxZ) / 2.0
        );

        if (side == EnumFacing.EAST) {
            x = MathHelper.floor_double(box.maxX) + 1;
        } else if (side == EnumFacing.WEST) {
            x = MathHelper.floor_double(box.minX) - 1;
        } else if (side == EnumFacing.SOUTH) {
            z = MathHelper.floor_double(box.maxZ) + 1;
        } else if (side == EnumFacing.NORTH) {
            z = MathHelper.floor_double(box.minZ) - 1;
        }

        return new BlockPos(x, y, z);
    }

    private static EntityLivingBase findNearestMob(
        EntityPlayerSP player,
        WorldClient world
    ) {
        AxisAlignedBB area = player.getEntityBoundingBox()
            .expand(5, 5, 5);

        List<EntityLivingBase> entities =
            world.getEntitiesWithinAABB(
                EntityLivingBase.class,
                area
            );

        EntityLivingBase nearest = null;
        double bestDistance = 5.0;

        for (EntityLivingBase entity : entities) {
            if (entity == player || entity.isDead) {
                continue;
            }

            double distance = player.getDistanceToEntity(entity);

            if (distance <= bestDistance) {
                bestDistance = distance;
                nearest = entity;
            }
        }

        return nearest;
    }

    private static int findWallSlot(
        ItemStack[] inventory,
        Item wallItem
    ) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory[i];

            if (stack != null
                && stack.getItem() == wallItem
                && stack.stackSize > 0) {
                return i;
            }
        }

        return -1;
    }

    private static boolean placeAtSupport(
        Minecraft mc,
        EntityPlayerSP player,
        WorldClient world,
        BlockPos target,
        int slot
    ) {
        EnumFacing[] sides = {
            EnumFacing.UP,
            EnumFacing.NORTH,
            EnumFacing.SOUTH,
            EnumFacing.WEST,
            EnumFacing.EAST,
            EnumFacing.DOWN
        };

        for (EnumFacing side : sides) {
            BlockPos support = target.offset(
                side.getOpposite()
            );

            if (world.isAirBlock(support)) {
                continue;
            }

            ItemStack stack =
                player.inventory.getStackInSlot(slot);

            if (stack == null || stack.stackSize <= 0) {
                return false;
            }

            player.inventory.currentItem = slot;

            Vec3 hit = new Vec3(
                support.getX() + 0.5
                    + side.getFrontOffsetX() * 0.5,
                support.getY() + 0.5
                    + side.getFrontOffsetY() * 0.5,
                support.getZ() + 0.5
                    + side.getFrontOffsetZ() * 0.5
            );

            if (mc.playerController.onPlayerRightClick(
                player,
                world,
                stack,
                support,
                side,
                hit
            )) {
                return true;
            }
        }

        return false;
    }
}
