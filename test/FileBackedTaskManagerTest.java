import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanager.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {

    FileBackedTaskManager fileBackedTaskManager;

    @BeforeEach
    public void setFileBackedTaskManager() {
        fileBackedTaskManager = new FileBackedTaskManager();
    }

    @Test
    public void saveEmptyFile() throws IOException {
        Files.createTempFile("tasks_", ".csv"); // принимает имя файла и расширение
        // тестируем fileBackedTaskManager.save() через методы в которых он есть, сохраняя приватность save
        Task task = new Task("t", "d", 1, TasksStatus.NEW);
        fileBackedTaskManager.addTask(task);
        fileBackedTaskManager.removeTask(task.getId());
        Epic epic = new Epic("e", "d", 2, TasksStatus.NEW);
        SubTask subTask = new SubTask("st", "d", 3, TasksStatus.NEW, 2);
        fileBackedTaskManager.addEpicTask(epic);
        fileBackedTaskManager.removeEpicTask(epic.getId());
        fileBackedTaskManager.addSubTask(subTask);
        fileBackedTaskManager.removeSubTask(subTask.getId());

        assertTrue(Files.exists(Paths.get("tasks.csv")));
        assertTrue(fileBackedTaskManager.getAllTasks().isEmpty());
        assertTrue(fileBackedTaskManager.getAllEpics().isEmpty());
        assertTrue(fileBackedTaskManager.getAllSubTasks().isEmpty());
    }

    @Test
    public void loadEmptyFile() throws IOException {
        File file = Files.createTempFile("tasks_", ".csv").toFile();
        fileBackedTaskManager = FileBackedTaskManager.loadFromFile(file);
        assertNotNull(fileBackedTaskManager);
        assertTrue(fileBackedTaskManager.getAllTasks().isEmpty());
        assertTrue(fileBackedTaskManager.getAllEpics().isEmpty());
        assertTrue(fileBackedTaskManager.getAllSubTasks().isEmpty());
    }

    @Test
    public void saveTasksInFile() throws IOException {
        Files.createTempFile("test_", ".txt"); // принимает имя файла и расширение
        InMemoryTaskManagerTest inMemoryTaskManagerTest = new InMemoryTaskManagerTest();
        Task[] tasks = inMemoryTaskManagerTest.createTasksArray(3);
        Epic[] epics = inMemoryTaskManagerTest.createEpicsArray(3);
        SubTask[] subTasks = inMemoryTaskManagerTest.createSubTasksArray(3, epics[2].getId());
        fileBackedTaskManager.addTask(tasks[0]);
        fileBackedTaskManager.addEpicTask(epics[0]);
        fileBackedTaskManager.addSubTask(subTasks[0]);
        fileBackedTaskManager.addSubTask(subTasks[1]);
        fileBackedTaskManager.addSubTask(subTasks[2]);
        fileBackedTaskManager.addTask(tasks[2]);
        fileBackedTaskManager.addEpicTask(epics[2]);
        fileBackedTaskManager.addEpicTask(epics[1]);
        fileBackedTaskManager.addTask(tasks[1]);
        assertEquals(fileBackedTaskManager.getAllTasks().size(), 3);
        assertEquals(fileBackedTaskManager.getAllEpics().size(), 3);
        assertEquals(fileBackedTaskManager.getAllSubTasks().size(), 3);
    }

    @Test
    public void loadTasksInFile() throws IOException {
        Files.createTempFile("test_", ".txt"); // принимает имя файла и расширение
        InMemoryTaskManagerTest inMemoryTaskManagerTest = new InMemoryTaskManagerTest();
        Task[] tasks = inMemoryTaskManagerTest.createTasksArray(3);
        Epic[] epics = inMemoryTaskManagerTest.createEpicsArray(3);
        SubTask[] subTasks = inMemoryTaskManagerTest.createSubTasksArray(3, epics[2].getId());
        FileBackedTaskManager fileBackedTaskManager2 = new FileBackedTaskManager();
        fileBackedTaskManager2.addTask(tasks[0]);
        fileBackedTaskManager2.addEpicTask(epics[0]);
        fileBackedTaskManager2.addSubTask(subTasks[0]);
        fileBackedTaskManager2.addSubTask(subTasks[1]);
        fileBackedTaskManager2.addSubTask(subTasks[2]);
        fileBackedTaskManager2.addTask(tasks[2]);
        fileBackedTaskManager2.addEpicTask(epics[2]);
        fileBackedTaskManager2.addEpicTask(epics[1]);
        fileBackedTaskManager2.addTask(tasks[1]);

        // загружаем из файла в новый объект !!!
        fileBackedTaskManager2 = FileBackedTaskManager.loadFromFile(Paths.get("tasks.csv").toFile());
        // после загрузки в менеджере все есть все восстановленные задачи из файла
        assertEquals(3, fileBackedTaskManager2.getAllTasks().size());
        assertEquals(3, fileBackedTaskManager2.getAllEpics().size());
        assertEquals(3, fileBackedTaskManager2.getAllSubTasks().size());
    }
}
