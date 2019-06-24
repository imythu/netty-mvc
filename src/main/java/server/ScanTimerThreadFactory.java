package server;

import java.util.concurrent.ThreadFactory;

/**
 * @author myth
 */
public class ScanTimerThreadFactory implements ThreadFactory {

    private volatile static Thread scanTimerThread;
    private final static String SCAN_TIMER_THREAD_NAME = "SCAN_TIMER_THREAD_NAME";

    @Override
    public Thread newThread(Runnable r) {
        if (scanTimerThread == null) {
            synchronized (ScanTimerThreadFactory.class) {
                if (scanTimerThread == null) {
                    scanTimerThread = new Thread(r, SCAN_TIMER_THREAD_NAME);
                    return scanTimerThread;
                }
            }
        }
        return scanTimerThread;
    }
}
