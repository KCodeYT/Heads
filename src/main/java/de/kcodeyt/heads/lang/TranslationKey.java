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

package de.kcodeyt.heads.lang;

import lombok.Getter;

import java.util.Locale;

/**
 * @author Kevims KCodeYT
 * @version 1.0
 */
public enum TranslationKey {

    CONSOLE_USES_PLAYER_COMMAND,
    HEAD_COMMAND_USAGE,
    ERROR_WHILE_GIVING_HEAD,
    INVALID_NAME,
    PLAYER_NOT_FOUND,
    HEAD_GIVEN;

    @Getter
    private final String key;

    TranslationKey() {
        this.key = this.name().toLowerCase(Locale.ROOT).replace("_", "-");
    }

}
