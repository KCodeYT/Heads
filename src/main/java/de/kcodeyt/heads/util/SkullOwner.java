package de.kcodeyt.heads.util;

import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SkullOwner {

    public static SkullOwner fromCompoundTag(CompoundTag compoundTag) {
        if(!compoundTag.contains("Properties") || !(compoundTag.get("Properties") instanceof CompoundTag))
            return null;
        final String id = compoundTag.contains("Id") ? compoundTag.getString("Id") : null;
        final String name = compoundTag.contains("Name") ? compoundTag.getString("Name") : null;
        final CompoundTag properties = compoundTag.getCompound("Properties");
        final ListTag<CompoundTag> textures = properties.getList("textures", CompoundTag.class);
        final String texture = textures.get(0).getString("Value");
        return new SkullOwner(id, name, texture);
    }

    private final String id;
    private final String name;
    private final String texture;

    public CompoundTag toCompoundTag() {
        final CompoundTag compoundTag = new CompoundTag("Owner").
                putCompound("Properties", new CompoundTag().putList(new ListTag<>("textures").add(new CompoundTag().putString("Value", this.texture))));
        if(this.id != null)
            compoundTag.putString("Id", this.id);
        if(this.name != null)
            compoundTag.putString("Name", this.name);
        return compoundTag;
    }

}
