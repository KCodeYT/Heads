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

import cn.nukkit.Server;
import cn.nukkit.scheduler.ServerScheduler;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ScheduledFuture<V> {

    private static final int WAITING = 0;
    private static final int COMPLETED = 1;

    public static <T> ScheduledFuture<T> supplyAsync(Supplier<T> supplier) {
        return new ScheduledFuture<>(supplier, false);
    }

    public static <T> ScheduledFuture<T> completed(T value) {
        return new ScheduledFuture<T>(null, true).apply(value, null);
    }

    private final Queue<BiConsumer<V, Exception>> syncQueue;
    private final Queue<BiConsumer<V, Exception>> asyncQueue;
    private final Object sync;

    private final ServerScheduler scheduler;
    private final Runnable task;

    private volatile V value;
    private volatile Exception exception;
    private volatile int state;

    private ScheduledFuture(Supplier<V> supplier, boolean canBeNull) {
        this.syncQueue = new ConcurrentLinkedDeque<>();
        this.asyncQueue = new ConcurrentLinkedDeque<>();
        this.sync = new Object();

        this.scheduler = Server.getInstance().getScheduler();
        if(supplier != null) {
            this.scheduler.scheduleTask(null, this.task = () -> {
                V value = null;
                Exception exception = null;
                try {
                    value = supplier.get();
                } catch(Exception e) {
                    exception = e;
                }

                this.apply(value, exception);
            }, true);
        } else {
            if(!canBeNull)
                throw new NullPointerException("Supplier cant be null!");
            this.task = null;
        }

        this.state = WAITING;
    }

    private ScheduledFuture<V> apply(V value, Exception e) {
        if(this.state == WAITING || this.task == null) {
            this.state = COMPLETED;
            this.value = value;
            this.exception = e;
            synchronized(this.sync) {
                this.sync.notifyAll();
            }
            if(Server.getInstance().isPrimaryThread()) {
                this.runSyncQueue(value, e);
                this.scheduler.scheduleTask(null, () -> this.runAsyncQueue(value, e), true);
            } else {
                this.runAsyncQueue(value, e);
                this.scheduler.scheduleTask(null, () -> this.runSyncQueue(value, e));
            }
        } else {
            this.state = WAITING;
            this.scheduler.scheduleTask(null, this.task, true);
        }
        return this;
    }

    public ScheduledFuture<V> whenComplete(BiConsumer<V, Exception> consumer) {
        if(this.state != COMPLETED)
            this.syncQueue.add(consumer);
        else if(!Server.getInstance().isPrimaryThread())
            this.scheduler.scheduleTask(null, () -> consumer.accept(this.value, this.exception));
        else
            consumer.accept(this.value, this.exception);
        return this;
    }

    public ScheduledFuture<V> whenCompleteAsync(BiConsumer<V, Exception> consumer) {
        if(this.state != COMPLETED)
            this.asyncQueue.add(consumer);
        else if(Server.getInstance().isPrimaryThread())
            this.scheduler.scheduleTask(null, () -> consumer.accept(this.value, this.exception), true);
        else
            consumer.accept(this.value, this.exception);
        return this;
    }

    public <R> ScheduledFuture<R> thenApply(Function<V, R> function) {
        final ScheduledFuture<R> future = new ScheduledFuture<>(null, true);
        this.whenComplete((v, e0) -> {
            R r = null;
            Exception e1 = e0;
            if(e0 == null) {
                try {
                    r = function.apply(v);
                } catch(Exception e2) {
                    e1 = e2;
                }
            }
            future.apply(r, e1);
        });
        return future;
    }

    public <R> ScheduledFuture<R> thenCompose(Function<V, ScheduledFuture<R>> function) {
        final ScheduledFuture<R> future = new ScheduledFuture<>(null, true);
        this.whenComplete((v0, e0) -> {
            if(e0 == null) {
                try {
                    function.apply(v0).whenComplete(future::apply);
                } catch(Exception e1) {
                    future.apply(null, e1);
                }
            } else {
                future.apply(null, e0);
            }
        });
        return future;
    }

    public V join() throws Exception {
        if(this.state == WAITING)
            synchronized(this.sync) {
                this.sync.wait();
            }
        if(this.exception != null)
            throw this.exception;
        return this.value;
    }

    private void runSyncQueue(V value, Exception exception) {
        while(!this.syncQueue.isEmpty())
            this.syncQueue.poll().accept(value, exception);
    }

    private void runAsyncQueue(V value, Exception exception) {
        while(!this.asyncQueue.isEmpty())
            this.asyncQueue.poll().accept(value, exception);
    }

}
