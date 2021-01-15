package de.kcodeyt.heads.util.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SkinResponse {

    public static final SkinResponse NOT_FOUND = new SkinResponse(false, null);

    static SkinResponse of(SkinData skinData) {
        return new SkinResponse(true, skinData);
    }

    private final boolean success;
    private final SkinData skinData;

}
