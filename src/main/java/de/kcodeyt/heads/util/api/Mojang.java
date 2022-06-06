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

import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
public final class Mojang {

    public static final HttpRequest<UserProfile> API = new HttpRequest<>("https://api.mojang.com/users/profiles/minecraft/", UserProfile.class);
    public static final HttpRequest<SessionProfile> SESSION_SERVER = new HttpRequest<>("https://sessionserver.mojang.com/session/minecraft/profile/", SessionProfile.class);
    public static final String TEXTURES = "http://textures.minecraft.net/texture/";

    @Getter
    @ToString
    public static class Error {
        private String error;
        private String path;

        public boolean isError() {
            return this.error != null;
        }
    }

    @Getter
    @ToString
    public static class UserProfile extends Error {
        private String id;
        private String name;
    }

    @Getter
    @ToString
    public static class SessionProfile extends Error {
        private String id;
        private String name;
        private List<Property> properties;
    }

    @Getter
    @ToString
    public static class Property {
        private String name;
        private String value;
    }

    @Getter
    @ToString
    public static class DecodedTexturesProperty {
        private long timestamp;
        private String profileId;
        private String profileName;
        private Map<String, Texture> textures;
    }

    @Getter
    @ToString
    public static class Texture {
        private String url;
    }

}
