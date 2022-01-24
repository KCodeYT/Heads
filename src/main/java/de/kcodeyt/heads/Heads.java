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

package de.kcodeyt.heads;

import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import de.kcodeyt.heads.block.BlockSkull;
import de.kcodeyt.heads.blockentity.BlockEntitySkull;
import de.kcodeyt.heads.command.DebugHeadCommand;
import de.kcodeyt.heads.entity.EntitySkull;
import de.kcodeyt.heads.listener.EventListener;
import de.kcodeyt.heads.util.*;
import de.kcodeyt.heads.util.api.SkinAPI;
import de.kcodeyt.heads.util.api.SkinData;

import java.lang.reflect.Field;

public class Heads extends PluginBase {

    public static final RuntimeException EXCEPTION = new RuntimeException();

    @Override
    public void onLoad() {
        final Config config = this.getConfig();
        config.reload();
        if(!config.exists("skull-scale")) {
            config.set("skull-scale", "VANILLA");
            config.save();
        }
        if(!config.exists("save-skin-cache")) {
            config.set("save-skin-cache", true);
            config.save();
        }
        if(!config.exists("skin-cache-folder")) {
            config.set("skin-cache-folder", "./skins/");
            config.save();
        }

        Entity.registerEntity("Skull", EntitySkull.class, true);
        BlockEntity.registerBlockEntity(BlockEntity.SKULL, BlockEntitySkull.class);

        Block.list[Block.SKULL_BLOCK] = BlockSkull.class;
        int dataBits;
        try {
            //noinspection JavaReflectionMemberAccess
            final Field dataBitsField = Block.class.getDeclaredField("DATA_BITS");
            dataBits = (int) dataBitsField.get(null);
        } catch(NoSuchFieldException | IllegalAccessException e) {
            dataBits = 4;
        }
        for(int data = 0; data < (1 << dataBits); ++data)
            Block.fullList[(Block.SKULL_BLOCK << dataBits) | data] = new BlockSkull(data);
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new EventListener(), this);
        this.getServer().getCommandMap().register("heads", new DebugHeadCommand());
        PluginHolder.init(this);
    }

    public static ScheduledFuture<ItemResult> createItem(HeadInput input) {
        switch(input.getType()) {
            case PLAYER:
                return SkinAPI.getSkin(input.getName()).thenApply(skinResponse -> {
                    if(skinResponse.isSuccess()) {
                        final SkinData skinData = skinResponse.getSkinData();
                        return new ItemResult(Heads.createItemByOwner(new SkullOwner(skinData.getSkinOwnerUniqueId(), skinData.getSkinOwnerName(), skinData.getTexture())), skinData.getSkinOwnerName());
                    }
                    throw EXCEPTION;
                });
            case TEXTURE:
                return ScheduledFuture.completed(new ItemResult(Heads.createItemByOwner(new SkullOwner(input.getUniqueId(), null, input.getTexture())), null));
        }
        return ScheduledFuture.completed(null);
    }

    public static Item createItemByOwner(SkullOwner skullOwner) {
        final Item item = Heads.createItemByType(3).setNamedTag(new CompoundTag().putCompound("SkullOwner", skullOwner.toCompoundTag()));
        if(skullOwner.getName() != null)
            item.setCustomName("§r§f" + skullOwner.getName() + "'s Head");
        return item;
    }

    public static Item createItemByType(int skullType) {
        return Item.get(Item.SKULL, skullType, 1);
    }

}
