package com.hlavenka;

import java.util.ArrayList;
import java.util.List;

import com.hlavenka.entity.User;
import com.hlavenka.queue.AddUserCommand;
import com.hlavenka.queue.CommandProcessor;
import com.hlavenka.queue.DeleteAllCommand;
import com.hlavenka.queue.PrintAllCommand;

public class Main {

    private static final int QUEUE_CAPACITY = 1000;

    public static void main(String[] args) {
        CommandProcessor cp = new CommandProcessor(QUEUE_CAPACITY);

        Runnable producer = () -> {
            cp.enqueue(new AddUserCommand(new User(1, "a1", "Robert")));
            cp.enqueue(new AddUserCommand(new User(2, "a2", "Martin")));
            cp.enqueue(new PrintAllCommand());
            cp.enqueue(new DeleteAllCommand());
            cp.enqueue(new PrintAllCommand());
        };

        Runnable consumer = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                cp.processCommand();
            }
        };

        // Start x consumers
        List<Thread> consumerThreads = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            Thread consumerThread = new Thread(consumer);
            consumerThread.start();
            consumerThreads.add(consumerThread);
        }

        // Start single producer
        new Thread(producer).start();

        // Wait for demonstration to finish
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        // Stop running consumer threads
        consumerThreads.forEach(Thread::interrupt);
    }
}