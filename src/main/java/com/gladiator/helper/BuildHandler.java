
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
import net.minecraft.util.Vec3;

import java.util.List;

public class BuildHandler {

    public static void build() {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.thePlayer == null || mc.theWorld == null) {
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

        int slot = findWallSlot(
            player.inventory.mainInventory,
            wallItem
        );

        if (slot == -1) {
            player.addChatMessage(
                new net.minecraft.util.ChatComponentText(
                    "§cVocê precisa de muros de pedregulho!"
                )
            );
            return;
        }

        EnumFacing facing = player.getHorizontalFacing();
        EnumFacing left = facing.rotateYCCW();
        EnumFacing right = facing.rotateY();

        BlockPos feet = new BlockPos(
            target.posX,
            target.posY,
            target.posZ
        );

        BlockPos[] positions = new BlockPos[] {
            feet.offset(left),
            feet.offset(right),
            new BlockPos(
                target.posX,
                MathHelper.floor_double(
                    target.posY + target.height
                ) + 1,
                target.posZ
            )
        };

        int placed = 0;

        for (BlockPos pos : positions) {
            if (!world.isAirBlock(pos)) {
                continue;
            }

            if (placeAtSupport(mc, player, world, pos, slot)) {
                placed++;
            }
        }

        player.addChatMessage(
            new net.minecraft.util.ChatComponentText(
                "§aGladiatorHelper: §f" + placed +
                " de 3 muros colocados."
            )
        );
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
        for (EnumFacing side : EnumFacing.values()) {
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
