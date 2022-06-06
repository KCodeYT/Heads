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

package de.kcodeyt.heads.api;

public enum SkullOwnerResolveMethod {

    /**
     * Resolves the skull owner by using a local players name.
     */
    LOCAL,

    /**
     * Resolves the skull owner by using the mojang skin api and the players name.
     */
    MOJANG,

    /**
     * Resolves the skull owner by using the local players name and if it was not found by
     * using the mojang skin api and the players name instead.
     */
    LOCAL_AND_MOJANG,

    /**
     * Resolves the skull owner by using the texture url.
     */
    TEXTURE

}
