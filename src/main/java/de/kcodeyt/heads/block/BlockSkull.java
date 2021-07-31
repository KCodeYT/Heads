package de.kcodeyt.heads.block;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.tag.CompoundTag;
import de.kcodeyt.heads.Heads;
import de.kcodeyt.heads.blockentity.BlockEntitySkull;

public class BlockSkull extends cn.nukkit.block.BlockSkull {

    public BlockSkull() {
        this(0);
    }

    public BlockSkull(int meta) {
        super(meta);
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
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
        final BlockEntitySkull blockEntity = (BlockEntitySkull) this.getLevel().getBlockEntity(this);
        if(blockEntity != null && blockEntity.getSkullOwner() != null)
            return new Item[]{Heads.createItemByOwner(blockEntity.getSkullOwner())};
        return super.getDrops(item);
    }

}
