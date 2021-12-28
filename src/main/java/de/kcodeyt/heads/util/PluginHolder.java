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
