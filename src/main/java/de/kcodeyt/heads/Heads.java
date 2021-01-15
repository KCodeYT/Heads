package de.kcodeyt.heads;

import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.plugin.PluginBase;
import de.kcodeyt.heads.block.BlockSkull;
import de.kcodeyt.heads.blockentity.BlockEntitySkull;
import de.kcodeyt.heads.command.DebugHeadCommand;
import de.kcodeyt.heads.entity.EntitySkull;
import de.kcodeyt.heads.listener.EventListener;
import de.kcodeyt.heads.util.HeadInput;
import de.kcodeyt.heads.util.ItemResult;
import de.kcodeyt.heads.util.SkullOwner;
import de.kcodeyt.heads.util.api.SkinAPI;
import de.kcodeyt.heads.util.api.SkinData;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class Heads extends PluginBase {

    @Override
    @SuppressWarnings("deprecation")
    public void onLoad() {
        Entity.registerEntity("Skull", EntitySkull.class, true);
        BlockEntity.registerBlockEntity(BlockEntity.SKULL, BlockEntitySkull.class);

        Block.list[Block.SKULL_BLOCK] = BlockSkull.class;
        int dataBits;
        try {
            Class.forName("cn.nukkit.api.PowerNukkitOnly");
            dataBits = Block.DATA_BITS;
        } catch(ClassNotFoundException e) {
            dataBits = 4;
        }
        for(int data = 0; data < (1 << dataBits); ++data)
            Block.fullList[(Block.SKULL_BLOCK << dataBits) | data] = new BlockSkull(data);
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new EventListener(), this);
        this.getServer().getCommandMap().register("heads", new DebugHeadCommand());
    }

    public static CompletableFuture<ItemResult> createItem(HeadInput input) {
        switch(input.getType()) {
            case PLAYER:
                return SkinAPI.getSkin(input.getName()).thenApply(skinResponse -> {
                    if(skinResponse.isSuccess()) {
                        final SkinData skinData = skinResponse.getSkinData();
                        return new ItemResult(Heads.createItemByOwner(new SkullOwner(skinData.getSkinOwnerUniqueId(), skinData.getSkinOwnerName(), skinData.getTexture())), skinData.getSkinOwnerName());
                    }
                    throw new CompletionException(new Throwable());
                });
            case TEXTURE:
                return CompletableFuture.completedFuture(new ItemResult(Heads.createItemByOwner(new SkullOwner(input.getUniqueId(), null, input.getTexture())), null));
        }
        return CompletableFuture.completedFuture(null);
    }

    public static Item createItemByOwner(SkullOwner skullOwner) {
        final Item item = Item.get(Item.SKULL, 3, 1);
        item.setCustomBlockData(new CompoundTag().putCompound("Owner", skullOwner.toCompoundTag()));
        if(skullOwner.getName() != null)
            item.setCustomName("§r§f" + skullOwner.getName() + "'s Head");
        return item;
    }

}
