package com.gladiator.helper;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ChatComponentText;

import java.util.List;

public class BuildHandler {

    public static void build() {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        EntityLivingBase target = null;
        double closestDistance = 5.0;

        AxisAlignedBB area = mc.thePlayer.getEntityBoundingBox()
            .expand(5, 5, 5);

        List<EntityLivingBase> entities =
            mc.theWorld.getEntitiesWithinAABB(
                EntityLivingBase.class, area
            );

        for (EntityLivingBase entity : entities) {
            if (entity == mc.thePlayer || entity.isDead) {
                continue;
            }

            double distance = mc.thePlayer.getDistanceToEntity(entity);

            if (distance <= closestDistance) {
                closestDistance = distance;
                target = entity;
            }
        }

        if (target == null) {
            mc.thePlayer.addChatMessage(
                new ChatComponentText("§cNenhum mob encontrado em 5 blocos.")
            );
            return;
        }

        EnumFacing facing = mc.thePlayer.getHorizontalFacing();
        EnumFacing left = facing.rotateYCCW();
        EnumFacing right = facing.rotateY();

        BlockPos feet = new BlockPos(
            target.posX, target.posY, target.posZ
        );

        BlockPos leftPosition = feet.offset(left);
        BlockPos rightPosition = feet.offset(right);
        BlockPos abovePosition = new BlockPos(
            target.posX,
            target.posY + target.height,
            target.posZ
        );

        mc.thePlayer.addChatMessage(
            new ChatComponentText(
                "§aMob encontrado: §f" + target.getName()
            )
        );

        mc.thePlayer.addChatMessage(
            new ChatComponentText(
                "§ePosições calculadas: esquerda, direita e acima."
            )
        );
    }
}
