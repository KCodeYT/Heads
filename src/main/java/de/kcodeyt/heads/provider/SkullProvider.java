package de.kcodeyt.heads.provider;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.Location;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.IntTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.utils.SerializedImage;
import de.kcodeyt.heads.Heads;
import de.kcodeyt.heads.blockentity.BlockEntitySkull;
import de.kcodeyt.heads.entity.EntitySkull;
import de.kcodeyt.heads.util.PluginHolder;
import de.kcodeyt.heads.util.SkinUtil;
import de.kcodeyt.heads.util.SkullOwner;
import de.kcodeyt.heads.util.SkullScale;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkullProvider {

    private static final Map<String, CompoundTag> SKIN_TAG_CACHE = new HashMap<>();

    public static EntitySkull createSkullEntity(SerializedImage image, BlockEntitySkull skull) {
        final BlockFace blockFace = BlockFace.fromIndex(skull.getBlock().getDamage());
        if(blockFace == BlockFace.DOWN)
            return null;
        Location location = Location.fromObject(skull.add(0.5, -0.00735, 0.5));
        location.yaw = blockFace == BlockFace.UP ? (skull.namedTag.getByte("Rot") * 22.5 + 180) % 360 : blockFace.getHorizontalIndex() * 90;
        if(blockFace != BlockFace.UP)
            location = location.add(blockFace.getUnitVector().multiply(-0.23895).add(0, 0.25, 0));

        final EntitySkull entitySkull = new EntitySkull(skull.getChunk(), Entity.getDefaultNBT(location).
                putCompound("Skin", SkullProvider.getOrCreateSkinTag(image, skull)).
                putCompound("SkullOwner", skull.namedTag.getCompound("Owner")).
                putList(new ListTag<>("BoundBlock").
                        add(new IntTag("x", skull.getFloorX())).
                        add(new IntTag("y", skull.getFloorY())).
                        add(new IntTag("z", skull.getFloorZ()))));
        entitySkull.spawnToAll();
        return entitySkull;
    }

    private static CompoundTag getOrCreateSkinTag(SerializedImage image, BlockEntitySkull blockEntitySkull) {
        final SkullOwner skullOwner = blockEntitySkull.getSkullOwner();
        final String skullId = skullOwner.getId();
        final String texture = skullOwner.getTexture();
        return SKIN_TAG_CACHE.computeIfAbsent(texture, s -> new CompoundTag().
                putString("ModelId", skullId == null ? UUID.randomUUID().toString() : skullId).
                putString("GeometryName", SkinUtil.PLACED_SKULL_GEOMETRY_NAME).
                putByteArray("GeometryData", SkinUtil.PLACED_SKULL_GEOMETRY.getBytes(StandardCharsets.UTF_8)).
                putByteArray("Data", image.data).
                putBoolean("PremiumSkin", false).
                putBoolean("PersonaSkin", false).
                putBoolean("CapeOnClassicSkin", false).
                putBoolean("IsTrustedSkin", true));
    }

    public static float getSkullScale() {
        SkullScale skullScale = SkullScale.VANILLA;
        final Heads plugin = PluginHolder.get();
        if(plugin != null) {
            try {
                skullScale = SkullScale.valueOf(plugin.getConfig().getString("skull-scale"));
            } catch(IllegalArgumentException ignored) {
            }
        }

        return skullScale.getValue();
    }

}
