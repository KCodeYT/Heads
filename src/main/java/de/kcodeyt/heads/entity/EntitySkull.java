package de.kcodeyt.heads.entity;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.EntityMetadata;
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
import cn.nukkit.network.protocol.SetEntityDataPacket;

import java.util.concurrent.TimeUnit;

public class EntitySkull extends EntityHuman {

    private static final CompoundTag EMPTY_COMPOUND = new CompoundTag();
    private static final Vector3 EMPTY_VECTOR = new Vector3();
    private static final AxisAlignedBB EMPTY_BOUNDING_BOX = new SimpleAxisAlignedBB(EMPTY_VECTOR, EMPTY_VECTOR);
    private static final Item[] EMPTY_ITEMS_ARRAY = new Item[0];

    private static final float SCALE = 1.068f + 0.06f;

    private final Vector3 boundBlock;
    private final SetEntityDataPacket packet;
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
        this.packet = new SetEntityDataPacket();
        this.packet.eid = this.id;
        this.packet.metadata = new EntityMetadata();
        this.dataProperties.getMap().forEach((key, data) -> this.packet.metadata.put(data));
        this.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_INVISIBLE, true);
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return EMPTY_BOUNDING_BOX;
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        this.setMaxHealth(1);
        this.setScale(SCALE);
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
        super.spawnTo(player);
        this.level.addPlayerMovement(this, this.x, this.y + this.getBaseOffset(), this.z, this.yaw, this.pitch, this.yaw);
        this.server.getScheduler().scheduleDelayedTask(null, () -> player.dataPacket(this.packet), 3);
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
