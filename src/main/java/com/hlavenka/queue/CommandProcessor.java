package com.hlavenka.queue;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hlavenka.entity.User;
import com.hlavenka.repository.UserRepository;

/**
 * Processor for queueing {@code Command} and processing them.
 */
public class CommandProcessor {

    private static Logger logger = Logger.getLogger(CommandProcessor.class.getCanonicalName());

    private BlockingQueue<Command> commandQ;
    private UserRepository userRepository;

    public CommandProcessor(int queueCapacity) {
        commandQ = new LinkedBlockingQueue<>(queueCapacity);
        userRepository = new UserRepository();
    }

    /**
     * Adds the command to the queue
     * @param command - command to be added
     * @see Command
     */
    public void enqueue(Command command) {
        try {
            if (command != null && command.getName() != null) {
                commandQ.put(command);
                logger.log(Level.INFO, String.format("[%s] Added command '%s'", Thread.currentThread().getName(), command.getName()));
            } else {
                logger.log(Level.SEVERE, String.format("[%s] Cannot insert null command to queue", Thread.currentThread().getName()));
            }
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, String.format("[%s] Thread was interrupted while waiting for available queue space to insert command '%s'", Thread.currentThread().getName(), command.getName()), e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Consumes commands from the queue
     * @see Command
     */
    public void processCommand() {
        try {
            Command command = commandQ.take();
            if (command != null && command.getName() != null) {
                executeCommand(command);
            } else {
                logger.log(Level.SEVERE, String.format("[%s] Cannot execute null command", Thread.currentThread().getName()));
            }
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, String.format("[%s] Thread was interrupted while waiting for a command from queue", Thread.currentThread().getName()), e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Identifies the command and executes it
     * @param command - command to be exectued
     * @see Command
     * @see AddUserCommand
     * @see PrintAllCommand
     * @see DeleteAllCommand
     */
    private void executeCommand(Command command) {

        switch (command.getName()) {
            case "Add" -> {
                AddUserCommand addUserCommand = AddUserCommand.class.cast(command);
                User user = addUserCommand.getUser();
                userRepository.addUser(user);
                logger.log(Level.INFO, String.format("[%s] Added user: %s, %s, %s", Thread.currentThread().getName(), user.getId(), user.getGuid(), user.getName()));
            }
            case "PrintAll" -> {
                List<User> users = userRepository.getAllUsers();
                logger.log(Level.INFO, String.format("[%s] Printing all users (%s)", Thread.currentThread().getName(), users.size()));
                users.forEach(user -> logger.log(Level.INFO, String.format("[%s] - %s", Thread.currentThread().getName(), user.toString())));
            }
            case "DeleteAll" -> {
                userRepository.deleteAllUsers();
                logger.log(Level.INFO, String.format("[%s] Deleted all users", Thread.currentThread().getName()));
            }
            default -> logger.log(Level.WARNING, String.format("[%s] Unsupported command: %s", Thread.currentThread().getName(), command.getName()));
        }
    }

}
