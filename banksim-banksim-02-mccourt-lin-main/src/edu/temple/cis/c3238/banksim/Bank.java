package edu.temple.cis.c3238.banksim;

import java.util.concurrent.Semaphore;

/**
 * @author Cay Horstmann
 * @author Modified by Paul Wolfgang
 * @author Modified by Charles Wang
 * @author Modified by Alexa Delacenserie
 * @author Modified by Tarek Elseify
 */

public class Bank {

    public static final int NTEST = 10;
    private final Account[] accounts;
    private long numTransactions = 0;
    private final int initialBalance;
    private final int numAccounts;
    protected Semaphore semaphore = new Semaphore(10);
    private boolean open = true;

    public Bank(int numAccounts, int initialBalance) {
        this.initialBalance = initialBalance;
        this.numAccounts = numAccounts;
        accounts = new Account[numAccounts];
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = new Account(i, initialBalance, this);
        }
        numTransactions = 0;
    }

    public synchronized boolean isOpen() {
        return open;
    }

    public void openBank() {
        open = true;
    }

    public void closeBank() {
        synchronized (this) {
            open = false;
        }
        for (Account account : accounts) {
            synchronized (account) {
                account.notifyAll();
            }
        }
    }

    public void transfer(int from, int to, int amount) {
        accounts[from].waitForSufficientFunds(amount);
        try {
            semaphore.acquire(); // Acquire a permit
            // System.out.printf("%s acquiring semaphore...\n", Thread.currentThread()); //added for testing

            if (accounts[from].withdraw(amount)) {
                accounts[to].deposit(amount);
            }

            if (shouldTest()) {
                TestThread testThread = new TestThread(this);
                testThread.start();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            semaphore.release(); // Release the permit
            // System.out.printf("%s releasing semaphore...\n", Thread.currentThread());//added fir testing
        }
    }

    public synchronized void test() {
        int totalBalance = 0;
        for (Account account : accounts) {
            System.out.printf("%-30s %s%n",
                    Thread.currentThread().toString(), account.toString());
            totalBalance += account.getBalance();
        }
        System.out.printf("%-30s Total balance: %d\n", Thread.currentThread().toString(), totalBalance);
        if (totalBalance != numAccounts * initialBalance) {
            System.out.printf("%-30s Total balance changed!\n", Thread.currentThread().toString());
            System.exit(0);
        } else {
            System.out.printf("%-30s Total balance unchanged.\n", Thread.currentThread().toString());
        }
    }

    public int getNumAccounts() {
        return numAccounts;
    }

    public synchronized boolean shouldTest() {
        return ++numTransactions % NTEST == 0;
    }

}