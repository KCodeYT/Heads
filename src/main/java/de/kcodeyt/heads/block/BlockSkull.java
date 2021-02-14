package de.kcodeyt.heads.block;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.tag.CompoundTag;
import de.kcodeyt.heads.Heads;
import de.kcodeyt.heads.blockentity.BlockEntitySkull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockSkull extends cn.nukkit.block.BlockSkull {

    public BlockSkull() {
        this(0);
    }

    public BlockSkull(int meta) {
        super(meta);
    }

    @Override
    public boolean place(@Nonnull Item item, @Nonnull Block block, @Nonnull Block target, @Nonnull BlockFace face, double fx, double fy, double fz, @Nullable Player player) {
        final Item itemClone = item.clone();
        final CompoundTag namedTag = itemClone.getNamedTag();
        if(itemClone.hasCompoundTag() && namedTag.contains("SkullOwner")) {
            final CompoundTag customBlockData = itemClone.getCustomBlockData();
            itemClone.setCustomBlockData((customBlockData == null ? new CompoundTag() : customBlockData).
                    putCompound("SkullOwner", namedTag.getCompound("SkullOwner")));
        }
        return super.place(itemClone, block, target, face, fx, fy, fz, player);
    }

    @Override
    public Item[] getDrops(Item item) {
        final BlockEntitySkull blockEntitySkull = (BlockEntitySkull) this.getLevel().getBlockEntity(this);
        if(blockEntitySkull != null && blockEntitySkull.getSkullOwner() != null)
            return new Item[]{Heads.createItemByOwner(blockEntitySkull.getSkullOwner())};
        return super.getDrops(item);
    }

}
