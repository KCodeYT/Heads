package de.kcodeyt.heads.manager;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.Location;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.IntTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.utils.SerializedImage;
import de.kcodeyt.heads.blockentity.BlockEntitySkull;
import de.kcodeyt.heads.entity.EntitySkull;

import java.util.UUID;

public class SkullManager {

    public static EntitySkull createSkullEntity(SerializedImage image, BlockEntitySkull blockEntitySkull) {
        //noinspection deprecation
        final BlockFace blockFace = BlockFace.fromIndex(blockEntitySkull.getBlock().getDamage());
        if(blockFace == BlockFace.DOWN)
            return null;
        Location location = Location.fromObject(blockEntitySkull.add(0.5, -0.00735, 0.5));
        location.yaw = blockFace == BlockFace.UP ? (blockEntitySkull.namedTag.getByte("Rot") * 22.5 + 180) % 360 : blockFace.getHorizontalIndex() * 90;
        if(blockFace != BlockFace.UP)
            location = location.add(blockFace.getUnitVector().multiply(-0.23895).add(0, 0.25, 0));

        final EntitySkull entitySkull = new EntitySkull(
                blockEntitySkull.getChunk(),
                Entity.getDefaultNBT(location).
                        putCompound("Skin", new CompoundTag().
                                putString("ModelId", UUID.randomUUID().toString()).
                                putByteArray("Data", image.data)).
                        putCompound("SkullOwner", blockEntitySkull.namedTag.getCompound("Owner")).
                        putList(new ListTag<>("BoundBlock").
                                add(new IntTag("x", blockEntitySkull.getFloorX())).
                                add(new IntTag("y", blockEntitySkull.getFloorY())).
                                add(new IntTag("z", blockEntitySkull.getFloorZ()))
                        )
        );
        entitySkull.spawnToAll();
        return entitySkull;
    }

}
