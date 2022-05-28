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

import cn.nukkit.Server;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.utils.SerializedImage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.kcodeyt.heads.Heads;
import de.kcodeyt.heads.util.api.SkinData;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalSkinAPI {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<String, SkinData> SKIN_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, String> NAME_TO_LATEST = new ConcurrentHashMap<>();

    public static void saveNameLookup() {
        final Heads heads = PluginHolder.get();

        final File localSkinDir = new File(heads.getConfig().getString("local-skin-folder"));
        if(!localSkinDir.exists() && !localSkinDir.mkdirs())
            Server.getInstance().getLogger().warning("Could not create local skin directory!");

        final File mappingFile = new File(localSkinDir, "mapping.json");

        try(final FileWriter writer = new FileWriter(mappingFile)) {
            GSON.toJson(NAME_TO_LATEST, writer);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadNameLookup() {
        final Heads heads = PluginHolder.get();

        final File localSkinDir = new File(heads.getConfig().getString("local-skin-folder"));
        if(!localSkinDir.exists() && !localSkinDir.mkdirs())
            Server.getInstance().getLogger().warning("Could not create local skin directory!");

        final File mappingFile = new File(localSkinDir, "mapping.json");
        if(!mappingFile.exists())
            return;

        try(final FileReader reader = new FileReader(mappingFile)) {
            final Map<?, ?> map = GSON.fromJson(reader, Map.class);
            for(Map.Entry<?, ?> entry : map.entrySet())
                NAME_TO_LATEST.put((String) entry.getKey(), (String) entry.getValue());
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addOrUpdatePlayer(String name, Skin skin) {
        NAME_TO_LATEST.put(name, skin.getSkinId());

        final SkinData skinData = SkinData.builder().
                skinOwnerName(name).
                skinOwnerUniqueId(skin.getSkinId()).
                serializedImage(skin.getSkinData()).
                build();

        SKIN_CACHE.put(skin.getSkinId(), skinData);

        saveSkinData(skin.getSkinId(), skinData);
    }

    public static String getPlayerName(String skinId) {
        return NAME_TO_LATEST.entrySet().stream().
                filter(entry -> entry.getValue().equals(skinId)).
                findFirst().
                map(Map.Entry::getKey).
                orElse(null);
    }

    public static String getLatestSkinId(String name) {
        return NAME_TO_LATEST.entrySet().stream().
                filter(entry -> entry.getKey().equalsIgnoreCase(name)).
                findFirst().
                map(Map.Entry::getValue).
                orElse(null);
    }

    public static SkinData getSkinData(String skinId) {
        SkinData skinData = SKIN_CACHE.get(skinId);
        if(skinData == null) skinData = tryLoadSkinData(skinId);

        return skinData;
    }

    private static void saveSkinData(String skinId, SkinData skinData) {
        final Heads heads = PluginHolder.get();

        final File localSkinDir = new File(heads.getConfig().getString("local-skin-folder"));
        if(!localSkinDir.exists() && !localSkinDir.mkdirs())
            Server.getInstance().getLogger().warning("Could not create local skin directory!");

        final File localSkinFile = new File(localSkinDir, skinId + ".json");
        try(final Writer writer = new FileWriter(localSkinFile)) {
            final Map<String, Object> skinDataMap = new HashMap<>();

            skinDataMap.put("skinOwnerName", skinData.getSkinOwnerName());
            skinDataMap.put("skinOwnerUniqueId", skinData.getSkinOwnerUniqueId());

            final Map<String, Object> serializedImageMap = new HashMap<>();
            final SerializedImage serializedImage = skinData.getSerializedImage();

            serializedImageMap.put("width", serializedImage.width);
            serializedImageMap.put("height", serializedImage.height);
            serializedImageMap.put("data", Base64.getEncoder().encodeToString(serializedImage.data));

            skinDataMap.put("serializedImage", serializedImageMap);

            GSON.toJson(skinDataMap, writer);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static SkinData tryLoadSkinData(String skinId) {
        final Heads heads = PluginHolder.get();

        final File localSkinDir = new File(heads.getConfig().getString("local-skin-folder"));
        if(!localSkinDir.exists() && !localSkinDir.mkdirs())
            Server.getInstance().getLogger().warning("Could not create local skin directory!");

        final File localSkinFile = new File(localSkinDir, skinId + ".json");
        if(!localSkinFile.exists()) return null;

        try(final Reader reader = new FileReader(localSkinFile)) {
            final Map<?, ?> jsonData = GSON.fromJson(reader, Map.class);

            final String skinOwnerName = (String) jsonData.get("skinOwnerName");
            final String skinOwnerUniqueId = (String) jsonData.get("skinOwnerUniqueId");
            final Map<?, ?> serializedImageData = (Map<?, ?>) jsonData.get("serializedImage");

            final SerializedImage serializedImage = new SerializedImage(
                    ((Double) serializedImageData.get("width")).intValue(),
                    ((Double) serializedImageData.get("height")).intValue(),
                    Base64.getDecoder().decode((String) serializedImageData.get("data"))
            );

            return SkinData.builder().
                    skinOwnerName(skinOwnerName).
                    skinOwnerUniqueId(skinOwnerUniqueId).
                    serializedImage(serializedImage).
                    build();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

}
