package de.kcodeyt.heads.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HeadInput {

    public static HeadInput ofPlayer(String name) {
        return new HeadInput(Type.PLAYER, name, null, null);
    }

    public static HeadInput ofTexture(String texture, String uniqueId) {
        return new HeadInput(Type.TEXTURE, null, texture, uniqueId);
    }

    public enum Type {
        PLAYER,
        TEXTURE
    }

    private final Type type;
    private final String name;
    private final String texture;
    private final String uniqueId;

}
