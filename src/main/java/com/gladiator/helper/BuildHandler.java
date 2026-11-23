
package com.gladiator.helper;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BuildHandler {

    public static void build() {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        World world = mc.theWorld;
        BlockPos player = new BlockPos(mc.thePlayer.posX,
                mc.thePlayer.posY, mc.thePlayer.posZ);

        EnumFacing facing = mc.thePlayer.getHorizontalFacing();
        EnumFacing right = facing.rotateY();
        EnumFacing left = facing.rotateYCCW();

        BlockPos center = player.up(1);

        world.setBlockState(
            center.offset(left),
            Blocks.cobblestone_wall.getDefaultState()
        );

        world.setBlockState(
            center.offset(right),
            Blocks.cobblestone_wall.getDefaultState()
        );

        world.setBlockState(
            center.offset(left).up(),
            Blocks.cobblestone_wall.getDefaultState()
        );

        world.setBlockState(
            center.offset(right).up(),
            Blocks.cobblestone_wall.getDefaultState()
        );
    }
}
