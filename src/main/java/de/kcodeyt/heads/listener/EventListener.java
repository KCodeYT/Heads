package de.kcodeyt.heads.listener;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerBlockPickEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import de.kcodeyt.heads.Heads;
import de.kcodeyt.heads.blockentity.BlockEntitySkull;
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
            if(skullOwner != null)
                event.setItem(Heads.createItemByOwner(skullOwner));
        }
    }

}
