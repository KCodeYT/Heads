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

package de.kcodeyt.heads.blockentity;

import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import de.kcodeyt.heads.entity.EntitySkull;
import de.kcodeyt.heads.provider.SkullProvider;
import de.kcodeyt.heads.util.SkullOwner;
import de.kcodeyt.heads.util.api.SkinAPI;
import lombok.Getter;

public class BlockEntitySkull extends cn.nukkit.blockentity.BlockEntitySkull {

    private EntitySkull entitySkull;
    @Getter
    private SkullOwner skullOwner;

    public BlockEntitySkull(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initBlockEntity() {
        if(!this.isBlockEntityValid()) return;

        final CompoundTag skullOwnerTag = this.namedTag.getCompound("SkullOwner");
        final CompoundTag ownerTag = this.namedTag.getCompound("Owner");
        if(!ownerTag.isEmpty())
            this.skullOwner = SkullOwner.fromCompoundTag(ownerTag);
        else if(!skullOwnerTag.isEmpty()) {
            this.skullOwner = SkullOwner.fromCompoundTag(skullOwnerTag);
            this.namedTag.remove("SkullOwner");
        }

        super.initBlockEntity();
        if(this.skullOwner == null) return;

        this.namedTag.putCompound("Owner", this.skullOwner.toCompoundTag());
        this.namedTag.putByte("SkullType", 3);

        SkinAPI.getSkinByTexture(this.skullOwner.getTexture()).
                whenComplete((serializedImage, throwable) -> {
                    if(this.closed) return;

                    if(serializedImage != null) this.entitySkull = SkullProvider.createSkullEntity(serializedImage, this);
                    else {
                        this.namedTag.remove("Owner");
                        this.skullOwner = null;
                    }
                });
    }

    public int getSkullType() {
        return this.namedTag.getByte("SkullType");
    }

    @Override
    public void close() {
        if(this.entitySkull != null) this.entitySkull.close();
        super.close();
    }

}
