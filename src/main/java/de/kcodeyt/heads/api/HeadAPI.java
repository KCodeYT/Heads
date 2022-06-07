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

package de.kcodeyt.heads.api;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import de.kcodeyt.heads.util.LocalSkinAPI;
import de.kcodeyt.heads.util.ScheduledFuture;
import de.kcodeyt.heads.util.SkullOwner;
import de.kcodeyt.heads.util.UsernameValidator;
import de.kcodeyt.heads.util.api.SkinAPI;
import de.kcodeyt.heads.util.api.SkinData;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class HeadAPI {

    /**
     * Returns a {@link ScheduledFuture} that will resolve the skull owner and gives
     * the provided {@link Player} the skull if the skull owner was successfully resolved.
     * This future returns a {@link GiveSkullResult} indicating whether the skull owner
     * was successfully resolved and the skull was given to the player.
     *
     * @param player     the player to give the skull to.
     * @param skullOwner the skull owner to resolve.
     * @return the {@link ScheduledFuture} that resolves the skull owner and gives the player the skull
     * and returns if the skull owner was successfully resolved.
     */
    public ScheduledFuture<GiveSkullResult> giveHead(Player player, String skullOwner) {
        return HeadAPI.giveHead(player, skullOwner, HeadSearchMethod.BOTH);
    }

    /**
     * Returns a {@link ScheduledFuture} that will resolve the skull owner by the {@link HeadSearchMethod}
     * and gives the provided {@link Player} the skull if the skull owner was successfully resolved.
     * This future returns a {@link GiveSkullResult} indicating whether the skull owner
     * was successfully resolved and the skull was given to the player.
     *
     * @param player       the player to give the skull to.
     * @param skullOwner   the skull owner to resolve.
     * @param searchMethod the search method used to resolve the skull owner by.
     * @return the {@link ScheduledFuture} that resolves the skull owner and gives the player the skull
     * and returns if the skull owner was successfully resolved.
     */
    public ScheduledFuture<GiveSkullResult> giveHead(Player player, String skullOwner, HeadSearchMethod searchMethod) {
        final boolean javaValid = UsernameValidator.isJavaValid(skullOwner);
        final boolean bedrockValid = UsernameValidator.isBedrockValid(skullOwner);

        switch(searchMethod) {
            case LOCAL_ONLY:
                if(!bedrockValid) return ScheduledFuture.completed(GiveSkullResult.FAILURE_INVALID_NAME);

                return HeadAPI.createSkullItem(skullOwner, searchMethod).thenApply(itemResult -> {
                    if(itemResult == null) return GiveSkullResult.FAILURE_PLAYER_NOT_FOUND;

                    player.getInventory().addItem(itemResult.getItem());
                    return GiveSkullResult.success(itemResult.getName());
                });
            case MOJANG_ONLY:
                if(!javaValid) return ScheduledFuture.completed(GiveSkullResult.FAILURE_INVALID_NAME);

                return HeadAPI.createSkullItem(skullOwner, searchMethod).thenApply(itemResult -> {
                    if(itemResult == null) return GiveSkullResult.FAILURE_PLAYER_NOT_FOUND;

                    player.getInventory().addItem(itemResult.getItem());
                    return GiveSkullResult.success(itemResult.getName());
                });
            case BOTH:
                if(!bedrockValid && !javaValid) return ScheduledFuture.completed(GiveSkullResult.FAILURE_INVALID_NAME);

                if(!bedrockValid)
                    return HeadAPI.createSkullItem(skullOwner, HeadSearchMethod.MOJANG_ONLY).thenApply(itemResult -> {
                        if(itemResult == null) return GiveSkullResult.FAILURE_PLAYER_NOT_FOUND;

                        player.getInventory().addItem(itemResult.getItem());
                        return GiveSkullResult.success(itemResult.getName());
                    });

                if(!javaValid)
                    return HeadAPI.createSkullItem(skullOwner, HeadSearchMethod.LOCAL_ONLY).thenApply(itemResult -> {
                        if(itemResult == null) return GiveSkullResult.FAILURE_PLAYER_NOT_FOUND;

                        player.getInventory().addItem(itemResult.getItem());
                        return GiveSkullResult.success(itemResult.getName());
                    });

                return HeadAPI.createSkullItem(skullOwner, searchMethod).thenApply(itemResult -> {
                    if(itemResult == null) return GiveSkullResult.FAILURE_PLAYER_NOT_FOUND;

                    player.getInventory().addItem(itemResult.getItem());
                    return GiveSkullResult.success(itemResult.getName());
                });
            default:
                throw new IllegalArgumentException("Unknown search method: " + searchMethod);
        }
    }

    /**
     * Returns a {@link ScheduledFuture} that will resolve the skull owner.
     *
     * @param skullOwner the skull owner to resolve.
     * @return the {@link ScheduledFuture} that resolves the skull owner.
     */
    public ScheduledFuture<SkullOwner> resolveSkullOwner(String skullOwner) {
        return HeadAPI.resolveSkullOwner(skullOwner, SkullOwnerResolveMethod.LOCAL_AND_MOJANG);
    }

    /**
     * Returns a {@link ScheduledFuture} that will resolve the skull owner by using the given {@link HeadSearchMethod}.
     *
     * @param skullOwner    the skull owner to resolve.
     * @param resolveMethod the search method used to resolve the skull owner by.
     * @return the {@link ScheduledFuture} that resolves the skull owner.
     */
    public ScheduledFuture<SkullOwner> resolveSkullOwner(String skullOwner, SkullOwnerResolveMethod resolveMethod) {
        final boolean javaValid = UsernameValidator.isJavaValid(skullOwner);
        final boolean bedrockValid = UsernameValidator.isBedrockValid(skullOwner);

        switch(resolveMethod) {
            case LOCAL: {
                if(!bedrockValid) return ScheduledFuture.completed(null);

                final String skinId = LocalSkinAPI.getLatestSkinId(skullOwner);
                if(skinId == null) return ScheduledFuture.completed(null);

                return ScheduledFuture.completed(new SkullOwner(skinId, LocalSkinAPI.getPlayerName(skinId), null));
            }
            case MOJANG: {
                if(!javaValid) return ScheduledFuture.completed(null);

                return SkinAPI.getSkin(skullOwner).thenApply(skinResponse -> {
                    if(skinResponse.isSuccess()) {
                        final SkinData skinData = skinResponse.getSkinData();
                        return new SkullOwner(skinData.getSkinOwnerUniqueId(), skinData.getSkinOwnerName(), skinData.getTexture());
                    } else {
                        return null;
                    }
                });
            }
            case LOCAL_AND_MOJANG: {
                if(!bedrockValid && !javaValid) return ScheduledFuture.completed(null);

                if(!bedrockValid)
                    return SkinAPI.getSkin(skullOwner).thenApply(skinResponse -> {
                        if(skinResponse.isSuccess()) {
                            final SkinData skinData = skinResponse.getSkinData();
                            return new SkullOwner(skinData.getSkinOwnerUniqueId(), skinData.getSkinOwnerName(), skinData.getTexture());
                        } else {
                            return null;
                        }
                    });

                if(!javaValid) {
                    final String skinId = LocalSkinAPI.getLatestSkinId(skullOwner);
                    if(skinId == null) return ScheduledFuture.completed(null);

                    return ScheduledFuture.completed(new SkullOwner(skinId, LocalSkinAPI.getPlayerName(skinId), null));
                }

                final String skinId = LocalSkinAPI.getLatestSkinId(skullOwner);
                if(skinId == null) return SkinAPI.getSkin(skullOwner).thenApply(skinResponse -> {
                    if(skinResponse.isSuccess()) {
                        final SkinData skinData = skinResponse.getSkinData();
                        return new SkullOwner(skinData.getSkinOwnerUniqueId(), skinData.getSkinOwnerName(), skinData.getTexture());
                    } else {
                        return null;
                    }
                });

                return ScheduledFuture.completed(new SkullOwner(skinId, LocalSkinAPI.getPlayerName(skinId), null));
            }
            case TEXTURE: {
                return ScheduledFuture.completed(new SkullOwner(UUID.randomUUID().toString(), null, skullOwner));
            }
            default:
                throw new IllegalArgumentException("Unknown resolve method: " + resolveMethod);
        }
    }

    /**
     * Creates a new fresh {@link Item} instance representing the given {@link SkullOwner}.
     *
     * @param skullOwner the {@link SkullOwner} to create the {@link Item} from.
     * @return the {@link Item} instance that represents the given {@link SkullOwner}.
     */
    public Item createSkullItemByOwner(SkullOwner skullOwner) {
        final Item item = HeadAPI.createSkullItemByType(3).setNamedTag(new CompoundTag().putCompound("SkullOwner", skullOwner.toCompoundTag()));
        if(skullOwner.getName() != null)
            item.setCustomName("§r§f" + skullOwner.getName() + "'s Head");
        return item;
    }

    /**
     * Returns a {@link ScheduledFuture} that will create a new {@link Item} instance holding
     * a new fresh {@link Item} instance representing the resolved skull owner.
     *
     * @param skullOwner the skull owner to resolve.
     * @return the {@link ScheduledFuture} that resolves the skull owner and creates the {@link ItemResult} instance.
     */
    public ScheduledFuture<ItemResult> createSkullItem(String skullOwner) {
        return HeadAPI.createSkullItem(skullOwner, HeadSearchMethod.BOTH);
    }

    /**
     * Returns a {@link ScheduledFuture} that will create a new {@link ItemResult} instance holding
     * a new fresh {@link Item} instance representing the resolved skull owner by the given {@link HeadSearchMethod}
     * and the player name by the resolved skull owner.
     *
     * @param skullOwner   the skull owner to resolve.
     * @param searchMethod the search method used to resolve the skull owner by.
     * @return the {@link ScheduledFuture} that resolves the skull owner and creates the {@link ItemResult} instance.
     */
    public ScheduledFuture<ItemResult> createSkullItem(String skullOwner, HeadSearchMethod searchMethod) {
        return HeadAPI.resolveSkullOwner(skullOwner, searchMethod.getResolveMethod()).thenApply(skullOwnerResult -> {
            if(skullOwnerResult == null) return null;

            return new ItemResult(HeadAPI.createSkullItemByOwner(skullOwnerResult), skullOwnerResult.getName());
        });
    }

    /**
     * Creates a new fresh {@link Item} instance with the given skull type.
     *
     * @param skullType the skull type for the new {@link Item}.
     * @return the {@link Item} instance with the given skull type.
     */
    public Item createSkullItemByType(int skullType) {
        return Item.get(Item.SKULL, skullType, 1);
    }


}
