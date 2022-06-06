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

package de.kcodeyt.heads.util.api;

import cn.nukkit.utils.SerializedImage;
import com.google.gson.Gson;
import de.kcodeyt.heads.Heads;
import de.kcodeyt.heads.util.ScheduledFuture;
import de.kcodeyt.heads.util.SkinUtil;
import de.kcodeyt.heads.util.api.Mojang.SessionProfile;
import de.kcodeyt.heads.util.api.Mojang.UserProfile;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

@UtilityClass
public class SkinAPI {

    private static final Gson GSON = new Gson();

    private static final Pattern UUID_PATTERN = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");

    private static final Map<String, String> UUID_CACHE = new ConcurrentHashMap<>();
    private static final List<SkinData> SKIN_CACHE = new CopyOnWriteArrayList<>();

    public ScheduledFuture<SkinResponse> getSkin(String name) {
        return ScheduledFuture.supplyAsync(() -> {
            try {
                String uniqueId;
                String playerName;
                String foundName = null;
                for(String uuidKey : UUID_CACHE.keySet()) {
                    if(uuidKey.equalsIgnoreCase(name))
                        foundName = uuidKey;
                }
                if(foundName == null) {
                    final UserProfile userProfile = Mojang.API.request(name);
                    uniqueId = UUID_PATTERN.matcher(userProfile.getId()).replaceFirst("$1-$2-$3-$4-$5");
                    playerName = userProfile.getName();
                    UUID_CACHE.put(playerName, uniqueId);
                } else
                    uniqueId = UUID_CACHE.get(playerName = foundName);

                SkinData skin;
                if((skin = SKIN_CACHE.stream().filter(skinData -> skinData.getSkinOwnerName().equalsIgnoreCase(playerName) || skinData.getSkinOwnerUniqueId().equals(uniqueId)).findAny().orElse(null)) == null) {
                    final SessionProfile profile = Mojang.SESSION_SERVER.request(uniqueId.replace("-", ""));
                    if(profile.isError())
                        return SkinResponse.NOT_FOUND;

                    final String texture = shrinkBase64(profile.getProperties().stream().
                            filter(property -> property.getName().equals("textures")).findAny().
                            orElseThrow(NullPointerException::new).getValue());
                    SKIN_CACHE.removeIf(skinData -> skinData.getTexture().equalsIgnoreCase(texture));

                    skin = SkinData.builder().
                            texture(texture).
                            skinOwnerName(playerName).
                            skinOwnerUniqueId(uniqueId).
                            serializedImage(getSkinByTexture(texture).join()).
                            build();

                    SKIN_CACHE.add(skin);
                }

                return SkinResponse.of(skin);
            } catch(Throwable cause) {
                throw Heads.EXCEPTION;
            }
        });
    }

    private String shrinkBase64(String base64Texture) {
        final Map<String, Object> shrunkMap = Collections.singletonMap("textures", Collections.singletonMap("SKIN", Collections.singletonMap("url", fromBase64(base64Texture))));
        return Base64.getEncoder().encodeToString(GSON.toJson(shrunkMap).getBytes(StandardCharsets.UTF_8));
    }

    public String fromBase64(String base64Texture) {
        return GSON.fromJson(new String(Base64.getDecoder().decode(base64Texture)), Mojang.DecodedTexturesProperty.class).getTextures().get("SKIN").getUrl();
    }

    public ScheduledFuture<SerializedImage> getSkinByTexture(String texture) {
        final SkinData skin;
        if((skin = SKIN_CACHE.stream().filter(skinData -> skinData.getTexture().equalsIgnoreCase(texture)).findAny().orElse(null)) == null)
            return SkinUtil.base64Texture(texture);
        return ScheduledFuture.completed(skin.getSerializedImage());
    }

}
