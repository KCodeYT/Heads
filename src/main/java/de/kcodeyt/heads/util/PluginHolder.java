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

import cn.nukkit.Server;
import cn.nukkit.plugin.Plugin;
import de.kcodeyt.heads.Heads;

public final class PluginHolder {

    private static Heads plugin;
    private static boolean searched = false;

    public static Heads get() {
        if(PluginHolder.plugin == null && !PluginHolder.searched) {
            Plugin found = null;
            for(Plugin plugin : Server.getInstance().getPluginManager().getPlugins().values()) {
                if(plugin instanceof Heads) {
                    found = plugin;
                    break;
                }
            }

            PluginHolder.plugin = (Heads) found;
            PluginHolder.searched = true;
        }

        return PluginHolder.plugin;
    }

    public static void init(Heads plugin) {
        PluginHolder.plugin = plugin;
    }

}
