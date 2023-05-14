package de.frosner;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

public class HashedWheelTimerTest {
    @Test
    public void testTimer() throws InterruptedException {
        HashedWheelTimer timer = new HashedWheelTimer(10, Duration.ofSeconds(1)); // Wheel size 10, tick duration 1s
        AtomicBoolean value1 = new AtomicBoolean(false);
        AtomicBoolean value2 = new AtomicBoolean(false);

        timer.newTimeout(() -> value1.set(true), Duration.ofSeconds(11));
        timer.newTimeout(() -> value2.set(true), Duration.ofSeconds(5));

        Thread.sleep(2000);
        assertFalse(value1.get());
        assertFalse(value2.get());
        Thread.sleep(2000);
        assertFalse(value1.get());
        assertFalse(value2.get());
        Thread.sleep(2000);
        assertFalse(value1.get());
        assertTrue(value2.get());
        Thread.sleep(2000);
        assertFalse(value1.get());
        assertTrue(value2.get());
        Thread.sleep(2000);
        assertFalse(value1.get());
        assertTrue(value2.get());
        Thread.sleep(2000);
        assertTrue(value1.get());
        assertTrue(value2.get());
    }
}