package de.frosner;

import java.util.*;
import java.util.concurrent.*;
import java.time.Duration;

public class HashedWheelTimer {
    private final Duration tickDuration;
    private final List<ConcurrentLinkedQueue<Timeout>> wheel;
    private volatile int wheelCursor = 0;

    public HashedWheelTimer(int wheelSize, Duration tickDuration) {
        this.tickDuration = tickDuration;
        this.wheel = new ArrayList<>(wheelSize);
        for (int i = 0; i < wheelSize; i++) {
            wheel.add(new ConcurrentLinkedQueue<>());
        }
        start();
    }

    public void newTimeout(Runnable task, Duration delay) {
        long ticks = delay.isZero() ? 0 : delay.plus(tickDuration).dividedBy(tickDuration);
        int stopIndex = (wheelCursor + (int)(ticks % wheel.size())) % wheel.size();
        wheel.get(stopIndex).add(new Timeout(task, ticks / wheel.size()));
    }

    private void start() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            System.out.println("Tick " + wheelCursor);
            ConcurrentLinkedQueue<Timeout> bucket = wheel.get(wheelCursor);
            List<Timeout> pendingTimeouts = new ArrayList<>();
            Timeout timeout;
            while ((timeout = bucket.poll()) != null) {
                System.out.println("Processing task " + timeout.task + " with " + timeout.remainingRounds + " remaining rounds");
                if (timeout.remainingRounds <= 0) {
                    timeout.task.run();
                } else {
                    timeout.remainingRounds--;
                    pendingTimeouts.add(timeout);
                }
            }
            bucket.addAll(pendingTimeouts);
            wheelCursor = (wheelCursor + 1) % wheel.size();
        }, tickDuration.toMillis(), tickDuration.toMillis(), TimeUnit.MILLISECONDS);
    }

    private static class Timeout {
        final Runnable task;
        long remainingRounds;

        Timeout(Runnable task, long remainingRounds) {
            this.task = task;
            this.remainingRounds = remainingRounds;
        }
    }
}