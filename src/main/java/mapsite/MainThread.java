package mapsite;

import necesse.engine.GameLog;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class MainThread {
    private static final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private static final AtomicBoolean ready = new AtomicBoolean(false);

    private static volatile Thread tickThread;

    private MainThread() {}

    public static void markReady() {
        ready.set(true);
    }

    public static void drainOnce(int maxTasks) {
        // Mark current thread as the tick thread
        tickThread = Thread.currentThread();

        for (int i = 0; i < maxTasks; i++) {
            Runnable r = queue.poll();
            if (r == null) return;
            try {
                r.run();
            } catch (Throwable t) {
                GameLog.warn.println("[MapSite] Exception in MainThread task: " + t.getMessage());
                t.printStackTrace(GameLog.warn);
            }
        }
    }

    public static void run(Runnable r) {
        queue.add(r);
    }

    public static <T> T call(Callable<T> c) {
        if (!ready.get()) {
            throw new RuntimeException("MapSite MainThread not ready (tick drainer not installed yet)");
        }

        if (Thread.currentThread() == tickThread) {
            try {
                return c.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        FutureTask<T> ft = new FutureTask<>(c);
        queue.add(ft);
        try {
            return ft.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("MapSite main-thread call timed out", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
