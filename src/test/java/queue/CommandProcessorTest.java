package queue;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.ReflectionUtils;

import com.hlavenka.entity.User;
import com.hlavenka.queue.AddUserCommand;
import com.hlavenka.queue.Command;
import com.hlavenka.queue.CommandProcessor;
import com.hlavenka.queue.DeleteAllCommand;
import com.hlavenka.queue.PrintAllCommand;
import com.hlavenka.repository.UserRepository;

class CommandProcessorTest {

    private CommandProcessor commandProcessor;

    private UserRepository userRepositoryMock;
    private BlockingQueue<Command> commandQMock;

    @BeforeEach
    public void setUp() throws Exception {
        commandProcessor = new CommandProcessor(10);

        userRepositoryMock = mock(UserRepository.class);

        Field userRepositoryField = ReflectionUtils.findFields(CommandProcessor.class, f -> f.getName().equals("userRepository"),
                ReflectionUtils.HierarchyTraversalMode.TOP_DOWN).get(0);
        userRepositoryField.setAccessible(true);
        userRepositoryField.set(commandProcessor, userRepositoryMock);

        commandQMock = mock(BlockingQueue.class);

        Field commandQMockField = ReflectionUtils.findFields(CommandProcessor.class, f -> f.getName().equals("commandQ"),
                ReflectionUtils.HierarchyTraversalMode.TOP_DOWN).get(0);
        commandQMockField.setAccessible(true);
        commandQMockField.set(commandProcessor, commandQMock);
    }

    @ParameterizedTest
    @DisplayName("Putting commands to queue")
    @MethodSource("provideCommandArguments")
    void enqueue_successful(Command command) throws Exception {
        commandProcessor.enqueue(command);

        if (command != null) {
            verify(commandQMock).put(command);
        }
        verifyNoMoreInteractions(commandQMock);
    }

    @ParameterizedTest
    @DisplayName("Throwing InterruptedException when putting commands to queue")
    @MethodSource("provideCommandArguments")
    void enqueue_throwException(Command command) throws Exception {
        doThrow(new InterruptedException()).when(commandQMock).put(command);
        commandProcessor.enqueue(command);

        if (command != null) {
            assertTrue(Thread.currentThread().isInterrupted());
        } else {
            assertFalse(Thread.currentThread().isInterrupted());
        }
    }

    @ParameterizedTest
    @DisplayName("Processing commands from queue")
    @MethodSource("provideCommandArguments")
    void processCommand_successful(Command command) throws Exception {
        when(commandQMock.take()).thenReturn(command);
        commandProcessor.processCommand();

        verify(commandQMock).take();
        verifyNoMoreInteractions(commandQMock);

        if (command instanceof AddUserCommand addUserCommand) {
            verify(userRepositoryMock).addUser(addUserCommand.getUser());
        } else if (command instanceof PrintAllCommand) {
            verify(userRepositoryMock).getAllUsers();
        } else if (command instanceof DeleteAllCommand) {
            verify(userRepositoryMock).deleteAllUsers();
        }
        verifyNoMoreInteractions(userRepositoryMock);
    }

    @ParameterizedTest
    @DisplayName("Throwing InterruptedException when processing commands from queue")
    @MethodSource("provideCommandArguments")
    void processCommand_throwException(Command command) throws Exception {
        doThrow(new InterruptedException()).when(commandQMock).take();
        commandProcessor.processCommand();

        assertTrue(Thread.currentThread().isInterrupted());
    }

    private static Stream<Arguments> provideCommandArguments() {
        return Stream.of(
                Arguments.of(new AddUserCommand(new User(1, "guid1", "John"))),
                Arguments.of(new PrintAllCommand()),
                Arguments.of(new DeleteAllCommand()),
                Arguments.of((Command)null));
    }
}
