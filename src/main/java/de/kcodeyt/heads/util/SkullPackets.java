package de.kcodeyt.heads.util;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.network.protocol.*;
import de.kcodeyt.heads.entity.EntitySkull;

public class SkullPackets {

    private final EntitySkull entitySkull;

    private final PlayerListPacket addToListPacket;
    private final SetEntityDataPacket setEntityDataPacket;
    private final AddPlayerPacket addPlayerPacket;
    private final PlayerListPacket removeFromListPacket;
    private final RemoveEntityPacket removeEntityPacket;

    public SkullPackets(EntitySkull entitySkull) {
        this.entitySkull = entitySkull;

        this.addToListPacket = new PlayerListPacket();
        this.addToListPacket.type = PlayerListPacket.TYPE_ADD;
        this.addToListPacket.entries = new PlayerListPacket.Entry[]{new PlayerListPacket.Entry(
                entitySkull.getUniqueId(), entitySkull.getId(), entitySkull.getName(), entitySkull.getSkin()
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

        this.removeEntityPacket = new RemoveEntityPacket();
        this.removeEntityPacket.eid = entitySkull.getId();
    }

    public void spawnTo(Player player) {
        player.dataPacket(this.addToListPacket);
        player.dataPacket(this.addPlayerPacket);
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
