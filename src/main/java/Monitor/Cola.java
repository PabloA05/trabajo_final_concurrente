package Monitor;

import java.util.concurrent.Semaphore;

public class Cola {
    private Semaphore semaphore;
    private int hilosColas;

    public Cola() {
        semaphore = new Semaphore(0, true);
        hilosColas = 0;
    }

    public void increment() {
        hilosColas++;
    }

    public void acquire() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void release() {
        hilosColas--;
        semaphore.release();
    }

    public boolean isNotEmpty() {
        return hilosColas != 0;
    }
}
