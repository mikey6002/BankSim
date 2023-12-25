package edu.temple.cis.c3238.banksim;

/**
 * @author Cay Horstmann
 * @author Modified by Paul Wolfgang
 * @author Modified by Charles Wang
 * @author Modified by Alexa Delacenserie
 * @author Modified by Tarek Elseify
 */
public class Account {

    private volatile int balance;
    private final int id;
    private Bank myBank;

    public Account(int id, int initialBalance, Bank bank) {
        this.id = id;
        this.balance = initialBalance;
        this.myBank = bank;
    }

    public synchronized int getBalance() {
        return balance;
    }

    public synchronized void waitForSufficientFunds(int amount) {
        while (amount >= balance) {
            try {
                wait();
            } catch (InterruptedException ex) { /* ignore */ }
        }
    }

    public synchronized boolean withdraw(int amount) {
        if (myBank.isOpen() && amount <= balance) {
            int currentBalance = balance;
            Thread.yield(); // Try to force collision
            int newBalance = currentBalance - amount;
            balance = newBalance;
            return true;
        } else {
            return false;
        }
    }

    public synchronized void deposit(int amount) {
        int currentBalance = balance;
        Thread.yield(); // Try to force collision
        int newBalance = currentBalance + amount;
        balance = newBalance;
        notifyAll();
    }

    @Override
    public String toString() {
        return String.format("Account[%d] balance %d", id, balance);
    }
}