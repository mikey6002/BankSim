package edu.temple.cis.c3238.banksim;

public class TestThread extends Thread {
    private final Bank bank;

    public TestThread(Bank bank) {
        this.bank = bank;
    }

    @Override
    public void run() {
        // Try to acquire all 10 permits
        try {
            bank.semaphore.acquire(10);
            bank.test();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            bank.semaphore.release(10); // Release all 10 permits
        }
    }
}

