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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GiveSkullResult {

    /**
     * Signals that the skull owner could not be resolved because the skull owner had an invalid name.
     */
    public static final GiveSkullResult FAILURE_INVALID_NAME = new GiveSkullResult(false, null, FailureCause.INVALID_NAME);

    /**
     * Signals that the skull owner could not be resolved because the skull owner was not found.
     */
    public static final GiveSkullResult FAILURE_PLAYER_NOT_FOUND = new GiveSkullResult(false, null, FailureCause.PLAYER_NOT_FOUND);

    /**
     * Creates a new {@link GiveSkullResult} with the given player name that signals
     * that the skull owner was successfully resolved.
     *
     * @param playerName the name of the skull owner that was resolved.
     * @return a new successful {@link GiveSkullResult} with the given player name.
     */
    public static GiveSkullResult success(String playerName) {
        return new GiveSkullResult(true, playerName, null);
    }

    boolean success;
    String playerName;
    FailureCause cause;

    public enum FailureCause {
        INVALID_NAME,
        PLAYER_NOT_FOUND
    }

}
