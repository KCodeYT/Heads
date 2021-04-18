package de.kcodeyt.heads.util.api;

import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
public class Mojang {

    public static final String API = "https://api.mojang.com/users/profiles/minecraft/";
    public static final String SESSION_SERVER = "https://sessionserver.mojang.com/session/minecraft/profile/";
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
    public static class Profile extends Error {

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
