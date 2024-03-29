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

package de.kcodeyt.heads.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import de.kcodeyt.heads.api.HeadAPI;
import de.kcodeyt.heads.lang.Language;
import de.kcodeyt.heads.lang.TranslationKey;
import de.kcodeyt.heads.util.PluginHolder;

public class HeadCommand extends Command {

    public HeadCommand() {
        super("head", "Gives yourself a player head", "/head <skull owner>");
        this.setPermission("heads.command.head");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        final Language language = PluginHolder.get().getLanguage();

        if(!(sender instanceof Player)) {
            sender.sendMessage(language.translate(null, TranslationKey.CONSOLE_USES_PLAYER_COMMAND));
            return false;
        }

        if(!this.testPermission(sender)) return false;

        final Player player = (Player) sender;
        if(args.length == 0) {
            player.sendMessage(language.translate(player, TranslationKey.HEAD_COMMAND_USAGE, this.getUsage()));
            return false;
        }

        final String skullOwner = String.join(" ", args);

        HeadAPI.giveHead(player, skullOwner).whenComplete((result, throwable) -> {
            if(result == null || throwable != null) {
                player.sendMessage(language.translate(player, TranslationKey.ERROR_WHILE_GIVING_HEAD));
                return;
            }

            if(!result.isSuccess()) {
                switch(result.getCause()) {
                    case INVALID_NAME:
                        player.sendMessage(language.translate(player, TranslationKey.INVALID_NAME));
                        return;
                    case PLAYER_NOT_FOUND:
                        player.sendMessage(language.translate(player, TranslationKey.PLAYER_NOT_FOUND));
                        return;
                    default:
                        player.sendMessage(language.translate(player, TranslationKey.ERROR_WHILE_GIVING_HEAD));
                        return;
                }
            }

            player.sendMessage(language.translate(player, TranslationKey.HEAD_GIVEN, result.getPlayerName()));
        });
        return false;
    }

}
