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
import de.kcodeyt.heads.command.HeadCommand;
import de.kcodeyt.heads.entity.EntitySkull;
import de.kcodeyt.heads.lang.Language;
import de.kcodeyt.heads.listener.EventListener;
import de.kcodeyt.heads.util.*;
import de.kcodeyt.heads.util.api.SkinAPI;
import de.kcodeyt.heads.util.api.SkinData;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

public class Heads extends PluginBase {

    private static final String DEFAULT_LANGUAGE = "en_US";

    public static final RuntimeException EXCEPTION = new RuntimeException();

    @Getter
    private Language language;

    @Override
    public void onLoad() {
        PluginHolder.init(this);

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
        if(!config.exists("local-skin-folder")) {
            config.set("local-skin-folder", "./local_skins/");
            config.save();
        }

        if(!config.exists("default_lang")) {
            config.set("default_lang", DEFAULT_LANGUAGE);
            config.save();
        }

        final File langDir = new File(this.getDataFolder(), "lang");
        final File[] files = langDir.listFiles();
        if(!langDir.exists() || files == null || files.length == 0) {
            if(!langDir.exists() && !langDir.mkdirs()) {
                this.getLogger().error("Could not create the language directory for this plugin!");
                return;
            }

            try(final InputStreamReader inputReader = new InputStreamReader(this.getResource("lang"));
                final BufferedReader bufferedReader = new BufferedReader(inputReader)) {
                String line;
                while((line = bufferedReader.readLine()) != null)
                    this.saveResource("lang/" + line);
            } catch(Exception e) {
                this.getLogger().error("Could not find the language resources of this plugin!", e);
                return;
            }
        }

        try {
            final String defaultLang = config.getString("default_lang");

            this.language = new Language(langDir, defaultLang);
            this.getLogger().info("This plugin is using the " + this.language.getDefaultLang() + " as default language file!");
        } catch(IOException e) {
            this.getLogger().error(e.getMessage(), e);
            return;
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
        this.getServer().getCommandMap().register("heads", new HeadCommand());

        LocalSkinAPI.loadNameLookup();
    }

    @Override
    public void onDisable() {
        LocalSkinAPI.saveNameLookup();
    }

    public static ScheduledFuture<ItemResult> createItem(HeadInput input) {
        switch(input.getType()) {
            case LOCAL:
                final String skinId = LocalSkinAPI.getLatestSkinId(input.getName());
                if(skinId == null) return ScheduledFuture.completed(null);

                final String originalName = LocalSkinAPI.getPlayerName(skinId);

                return ScheduledFuture.completed(new ItemResult(Heads.createItemByOwner(new SkullOwner(skinId, originalName, null)), originalName));
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
