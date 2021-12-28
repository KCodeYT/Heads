package de.kcodeyt.heads.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SkullScale {

    VANILLA(1.068f),
    SAFE_OVERLAY(1.125f),
    THICK(1.25f);

    private final float value;

}
