/*
 * Copyright 2022 KCodeYT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
