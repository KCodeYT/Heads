package de.kcodeyt.heads.block;

import cn.nukkit.item.Item;
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
    public Item[] getDrops(Item item) {
        final BlockEntitySkull blockEntitySkull = (BlockEntitySkull) this.getLevel().getBlockEntity(this);
        if(blockEntitySkull != null && blockEntitySkull.getSkullOwner() != null)
            return new Item[]{Heads.createItemByOwner(blockEntitySkull.getSkullOwner())};
        return super.getDrops(item);
    }

}
