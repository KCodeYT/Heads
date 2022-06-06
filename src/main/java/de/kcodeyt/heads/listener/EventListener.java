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

package de.kcodeyt.heads.listener;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerBlockPickEvent;
import cn.nukkit.event.player.PlayerChangeSkinEvent;
import cn.nukkit.event.player.PlayerLoginEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import de.kcodeyt.heads.api.HeadAPI;
import de.kcodeyt.heads.blockentity.BlockEntitySkull;
import de.kcodeyt.heads.util.LocalSkinAPI;
import de.kcodeyt.heads.util.SkullOwner;

public class EventListener implements Listener {

    @EventHandler
    public void onPick(PlayerBlockPickEvent event) {
        final Player player = event.getPlayer();
        final Item item = event.getItem();
        final Block block = event.getBlockClicked();
        final Level level = player.getLevel();
        final BlockEntity blockEntity = level.getBlockEntity(block);

        if(item.hasCustomBlockData() && blockEntity instanceof BlockEntitySkull) {
            final SkullOwner skullOwner = ((BlockEntitySkull) blockEntity).getSkullOwner();
            if(skullOwner != null) event.setItem(HeadAPI.createSkullItemByOwner(skullOwner));
            else event.setItem(HeadAPI.createSkullItemByType(((BlockEntitySkull) blockEntity).getSkullType()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(PlayerLoginEvent event) {
        final Player player = event.getPlayer();

        LocalSkinAPI.addOrUpdatePlayer(player.getName(), player.getSkin());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChangeSkin(PlayerChangeSkinEvent event) {
        final Player player = event.getPlayer();

        LocalSkinAPI.addOrUpdatePlayer(player.getName(), event.getSkin());
    }

}
