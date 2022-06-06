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
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import de.kcodeyt.heads.block.BlockSkull;
import de.kcodeyt.heads.blockentity.BlockEntitySkull;
import de.kcodeyt.heads.command.HeadCommand;
import de.kcodeyt.heads.entity.EntitySkull;
import de.kcodeyt.heads.lang.Language;
import de.kcodeyt.heads.listener.EventListener;
import de.kcodeyt.heads.util.LocalSkinAPI;
import de.kcodeyt.heads.util.PluginHolder;
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

        this.saveResource("config.yml");
        final Config config = this.getConfig();

        final File langDir = new File(this.getDataFolder(), "lang");
        final File[] files = langDir.listFiles();
        if(!langDir.exists() || files == null || files.length == 0) {
            if(!langDir.exists() && !langDir.mkdirs()) {
                this.getLogger().error("Could not create the language directory for this plugin!");
                return;
            }

            try(final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.getResource("lang/lang_list.txt")))) {
                String line;
                while((line = bufferedReader.readLine()) != null)
                    this.saveResource("lang/" + line + ".txt");
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

}
