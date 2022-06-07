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

package de.kcodeyt.heads.util;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class UsernameValidator {

    private static final Pattern BEDROCK_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z\\d_ ]{3,12}$");
    private static final Pattern JAVA_USERNAME_PATTERN = Pattern.compile("^\\w{3,16}$");

    /**
     * Checks if the given username matches the given restrictions by the bedrock edition.
     *
     * @param username the username to check.
     * @return true if the username matches the restrictions, false otherwise.
     */
    public boolean isBedrockValid(String username) {
        return BEDROCK_USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Checks if the given username matches the given restrictions by the java edition.
     *
     * @param username the username to check.
     * @return true if the username matches the restrictions, false otherwise.
     */
    public boolean isJavaValid(String username) {
        return JAVA_USERNAME_PATTERN.matcher(username).matches();
    }

}
