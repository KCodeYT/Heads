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

    public static CompoundTag buildTag(String texture) {
        return SkullOwner.buildTag(null, null, texture);
    }

    public static CompoundTag buildTag(String id, String texture) {
        return SkullOwner.buildTag(id, null, texture);
    }

    public static CompoundTag buildTag(String id, String name, String texture) {
        final CompoundTag compoundTag = new CompoundTag("Owner").
                putCompound("Properties", new CompoundTag().putList(new ListTag<>("textures").add(new CompoundTag().putString("Value", texture))));
        if(id != null)
            compoundTag.putString("Id", id);
        if(name != null)
            compoundTag.putString("Name", name);
        return compoundTag;
    }

    private final String id;
    private final String name;
    private final String texture;

    public CompoundTag toCompoundTag() {
        return SkullOwner.buildTag(this.id, this.name, this.texture);
    }

}
