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

package de.kcodeyt.heads.entity;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.IntTag;
import cn.nukkit.nbt.tag.ListTag;
import de.kcodeyt.heads.provider.SkullProvider;
import de.kcodeyt.heads.provider.SpawnProvider;
import de.kcodeyt.heads.util.SkullPackets;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

public class EntitySkull extends EntityHuman {

    private static final CompoundTag EMPTY_COMPOUND = new CompoundTag();
    private static final Vector3 EMPTY_VECTOR = new Vector3();
    private static final AxisAlignedBB EMPTY_BOUNDING_BOX = new SimpleAxisAlignedBB(EMPTY_VECTOR, EMPTY_VECTOR);
    private static final Item[] EMPTY_ITEMS_ARRAY = new Item[0];

    private final Vector3 boundBlock;
    @Getter
    private final SkullPackets skullPackets;

    private int closeTimer;

    public EntitySkull(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.closeTimer = 0;
        final ListTag<IntTag> boundBlock = nbt.getList("BoundBlock", IntTag.class);
        this.boundBlock = boundBlock.size() == 3 ? new Vector3(
                boundBlock.get(0).getData(),
                boundBlock.get(1).getData(),
                boundBlock.get(2).getData()
        ) : this.getLevelBlock();
        this.skullPackets = new SkullPackets(this);
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return EMPTY_BOUNDING_BOX;
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        this.setMaxHealth(1);
        this.setScale(SkullProvider.getSkullScale());
        this.dataProperties.putFloat(Entity.DATA_BOUNDING_BOX_HEIGHT, 0f);
        this.dataProperties.putFloat(Entity.DATA_BOUNDING_BOX_WIDTH, 0f);
    }

    @Override
    public void setRotation(double yaw, double pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public float getWidth() {
        return 0f;
    }

    @Override
    public float getHeight() {
        return 0f;
    }

    @Override
    public Item[] getDrops() {
        return EMPTY_ITEMS_ARRAY;
    }

    @Override
    public void spawnTo(Player player) {
        if(this.hasSpawned.containsKey(player.getLoaderId())) return;
        this.hasSpawned.put(player.getLoaderId(), player);
        SpawnProvider.spawnTo(player, this);
    }

    @Override
    public void despawnFrom(Player player) {
        if(this.hasSpawned.remove(player.getLoaderId()) == null) return;
        SpawnProvider.despawnFrom(player, this);
    }

    @Override
    public void close() {
        if(!this.server.isPrimaryThread()) {
            this.server.getScheduler().scheduleTask(null, this::close);
            return;
        }

        super.close();
    }

    @Override
    public void saveNBT() {
        this.namedTag = EMPTY_COMPOUND;
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        if(source instanceof EntityDamageByEntityEvent) {
            final Entity entity = ((EntityDamageByEntityEvent) source).getDamager();
            if(entity instanceof Player) {
                final Player player = (Player) entity;
                player.lastBreak = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1);
                this.getLevel().useBreakOn(this.boundBlock, player.getInventory().getItemInHand(), player, true);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        SpawnProvider.tick(this.server);
        this.spawnToAll();

        if(this.level.getBlock(this.boundBlock).getId() != Block.SKULL_BLOCK) {
            if(this.closeTimer++ > 2) {
                this.close();
                return false;
            }
        } else if(this.closeTimer > 0)
            this.closeTimer--;
        return false;
    }

}
