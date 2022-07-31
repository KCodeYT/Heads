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

package de.kcodeyt.heads.provider;

import cn.nukkit.Player;
import cn.nukkit.Server;
import de.kcodeyt.heads.entity.EntitySkull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SpawnProvider {

    private static final AtomicInteger LAST_TICK = new AtomicInteger();
    private static final Map<Player, Skulls> SKULLS_MAP = new HashMap<>();

    public static void spawnTo(Player player, EntitySkull entitySkull) {
        final Skulls skulls = SKULLS_MAP.computeIfAbsent(player, k -> new Skulls());
        skulls.waiting.add(entitySkull);
    }

    public static void despawnFrom(Player player, EntitySkull entitySkull) {
        final Skulls skulls = SKULLS_MAP.get(player);
        if(skulls != null) {
            skulls.waiting.remove(entitySkull);
            skulls.visible.remove(entitySkull);
        }

        entitySkull.getSkullPackets().despawnFrom(player);
    }

    public static void tick(Server server) {
        if(LAST_TICK.get() == server.getTick()) return;
        LAST_TICK.set(server.getTick());

        for(final Iterator<Map.Entry<Player, Skulls>> iterator = SKULLS_MAP.entrySet().iterator(); iterator.hasNext(); ) {
            final Map.Entry<Player, Skulls> entry = iterator.next();
            final Player player = entry.getKey();
            final Skulls skulls = entry.getValue();

            if(!player.isOnline()) {
                iterator.remove();
                continue;
            }

            final Set<EntitySkull> allSkulls = new HashSet<>();
            allSkulls.addAll(skulls.waiting);
            allSkulls.addAll(skulls.visible);

            if(allSkulls.isEmpty()) continue;

            final List<EntitySkull> sortedSkulls = new ArrayList<>(allSkulls);
            sortedSkulls.removeIf(Objects::isNull);
            sortedSkulls.sort(Comparator.comparingDouble(e -> e.distanceSquared(player)));

            final int maxSkulls = Math.min(50, sortedSkulls.size());
            for(int i = 0; i < maxSkulls; i++) {
                final EntitySkull entitySkull = sortedSkulls.get(i);

                if(!skulls.visible.contains(entitySkull)) {
                    entitySkull.getSkullPackets().spawnTo(player);
                    skulls.visible.add(entitySkull);
                }
            }

            if(maxSkulls < sortedSkulls.size()) {
                for(int i = maxSkulls; i < sortedSkulls.size(); i++) {
                    final EntitySkull entitySkull = sortedSkulls.get(i);
                    if(skulls.visible.contains(entitySkull))
                        entitySkull.despawnFrom(player);
                }
            }
        }
    }

    private static class Skulls {
        private final List<EntitySkull> waiting = new ArrayList<>();
        private final List<EntitySkull> visible = new ArrayList<>();
    }

}
