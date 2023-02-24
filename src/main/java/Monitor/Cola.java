package Monitor;

import java.util.concurrent.Semaphore;

public class Cola {
    private Semaphore semaphore;
    private int token;

    public Cola() {
        semaphore = new Semaphore(0, true);
        token = 0;
    }

    public void increment() {
        token++;
    }

    public void acquire() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void release() {
        token--;
        semaphore.release();
    }

    public boolean isNotEmpty() {
        return token != 0;
    }
}
