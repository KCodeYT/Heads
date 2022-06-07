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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HeadSearchMethod {

    /**
     * Resolves the skull owner only by local players.
     */
    LOCAL_ONLY(SkullOwnerResolveMethod.LOCAL),

    /**
     * Resolves the skull owner only by searching the skull owner in the mojang skin api.
     */
    MOJANG_ONLY(SkullOwnerResolveMethod.MOJANG),

    /**
     * Resolves the skull owner by searching the skull owner locally and if it was not found
     * then by searching the skull owner in the mojang skin api.
     */
    BOTH(SkullOwnerResolveMethod.LOCAL_AND_MOJANG);

    private final SkullOwnerResolveMethod resolveMethod;

}
