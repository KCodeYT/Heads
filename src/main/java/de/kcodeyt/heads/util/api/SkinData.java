package de.kcodeyt.heads.util.api;

import cn.nukkit.utils.SerializedImage;
import lombok.Builder;
import lombok.Getter;

import java.util.Arrays;

@Builder
@Getter
public class SkinData {

    private final String texture;
    private final String skinOwnerName;
    private final String skinOwnerUniqueId;
    private final SerializedImage serializedImage;

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SkinData && (((SkinData) obj).skinOwnerName.equals(this.skinOwnerName) || Arrays.equals(((SkinData) obj).serializedImage.data, this.serializedImage.data));
    }

}
