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
        if(!this.isBlockEntityValid())
            return;

        final CompoundTag skullOwnerTag = this.namedTag.getCompound("SkullOwner");
        final CompoundTag ownerTag = this.namedTag.getCompound("Owner");
        if(!ownerTag.isEmpty())
            this.skullOwner = SkullOwner.fromCompoundTag(ownerTag);
        else if(!skullOwnerTag.isEmpty()) {
            this.skullOwner = SkullOwner.fromCompoundTag(skullOwnerTag);
            this.namedTag.remove("SkullOwner");
        }

        super.initBlockEntity();
        if(this.skullOwner == null)
            return;

        this.namedTag.putCompound("Owner", this.skullOwner.toCompoundTag());
        this.namedTag.putByte("SkullType", 3);

        SkinAPI.getSkinByTexture(this.skullOwner.getTexture()).
                whenComplete((serializedImage, throwable) -> {
                    if(this.closed)
                        return;
                    if(serializedImage != null)
                        this.entitySkull = SkullProvider.createSkullEntity(serializedImage, this);
                    else {
                        this.namedTag.remove("Owner");
                        this.skullOwner = null;
                    }
                });
    }

    @Override
    public CompoundTag getCleanedNBT() {
        return super.getCleanedNBT().remove("Rot");
    }

    @Override
    public void close() {
        if(this.entitySkull != null)
            this.entitySkull.close();
        super.close();
    }

}
