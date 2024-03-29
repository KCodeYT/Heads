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

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.network.protocol.*;
import de.kcodeyt.heads.entity.EntitySkull;

import java.util.Base64;

public class SkullPackets {

    private final EntitySkull entitySkull;

    private final PlayerListPacket addToListPacket;
    private final SetEntityDataPacket setEntityDataPacket;
    private final AddPlayerPacket addPlayerPacket;
    private final PlayerListPacket removeFromListPacket;
    private final MovePlayerPacket movePlayerPacket;
    private final RemoveEntityPacket removeEntityPacket;

    public SkullPackets(EntitySkull entitySkull) {
        this.entitySkull = entitySkull;

        final Skin skin = new Skin();
        skin.setGeometryData(entitySkull.getSkin().getGeometryData());
        skin.setSkinResourcePatch(entitySkull.getSkin().getSkinResourcePatch());
        skin.setSkinData(entitySkull.getSkin().getSkinData());
        skin.setSkinId(Base64.getEncoder().encodeToString(entitySkull.getSkin().getSkinData().data));
        skin.setTrusted(true);

        this.addToListPacket = new PlayerListPacket();
        this.addToListPacket.type = PlayerListPacket.TYPE_ADD;
        this.addToListPacket.entries = new PlayerListPacket.Entry[]{new PlayerListPacket.Entry(
                entitySkull.getUniqueId(), entitySkull.getId(), entitySkull.getName(), skin
        )};

        this.setEntityDataPacket = new SetEntityDataPacket();
        this.setEntityDataPacket.eid = entitySkull.getId();
        this.setEntityDataPacket.metadata = new EntityMetadata();
        entitySkull.getDataProperties().getMap().forEach((key, data) -> this.setEntityDataPacket.metadata.put(data));

        entitySkull.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_INVISIBLE, true);

        this.addPlayerPacket = new AddPlayerPacket();
        this.addPlayerPacket.uuid = entitySkull.getUniqueId();
        this.addPlayerPacket.username = entitySkull.getName();
        this.addPlayerPacket.entityUniqueId = entitySkull.getId();
        this.addPlayerPacket.entityRuntimeId = entitySkull.getId();
        this.addPlayerPacket.x = (float) entitySkull.x;
        this.addPlayerPacket.y = (float) entitySkull.y;
        this.addPlayerPacket.z = (float) entitySkull.z;
        this.addPlayerPacket.speedX = (float) entitySkull.motionX;
        this.addPlayerPacket.speedY = (float) entitySkull.motionY;
        this.addPlayerPacket.speedZ = (float) entitySkull.motionZ;
        this.addPlayerPacket.yaw = (float) entitySkull.yaw;
        this.addPlayerPacket.pitch = (float) entitySkull.pitch;
        this.addPlayerPacket.item = entitySkull.getInventory().getItemInHand();
        this.addPlayerPacket.metadata = entitySkull.getDataProperties();

        this.removeFromListPacket = new PlayerListPacket();
        this.removeFromListPacket.type = PlayerListPacket.TYPE_REMOVE;
        this.removeFromListPacket.entries = new PlayerListPacket.Entry[]{new PlayerListPacket.Entry(entitySkull.getUniqueId())};

        this.movePlayerPacket = new MovePlayerPacket();
        this.movePlayerPacket.eid = entitySkull.getId();
        this.movePlayerPacket.x = (float) entitySkull.x;
        this.movePlayerPacket.y = (float) entitySkull.y + 1.62F;
        this.movePlayerPacket.z = (float) entitySkull.z;
        this.movePlayerPacket.yaw = (float) entitySkull.yaw;
        this.movePlayerPacket.headYaw = (float) entitySkull.yaw;
        this.movePlayerPacket.pitch = (float) entitySkull.pitch;

        this.removeEntityPacket = new RemoveEntityPacket();
        this.removeEntityPacket.eid = entitySkull.getId();
    }

    public void spawnTo(Player player) {
        player.dataPacket(this.addToListPacket);
        player.dataPacket(this.addPlayerPacket);
        player.dataPacket(this.movePlayerPacket);
        player.dataPacket(this.removeFromListPacket);

        player.getServer().getScheduler().scheduleDelayedTask(null, () -> {
            if(this.entitySkull.getViewers().containsKey(player.getLoaderId()))
                player.dataPacket(this.setEntityDataPacket);
        }, 3);
    }

    public void despawnFrom(Player player) {
        player.dataPacket(this.removeEntityPacket);
    }

}
