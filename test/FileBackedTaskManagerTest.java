import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanager.*;
import taskmanager.exceptions.ManagerSaveException;
import taskmanager.taskservice.Epic;
import taskmanager.taskservice.SubTask;
import taskmanager.taskservice.Task;
import taskmanager.taskservice.TasksStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {

    static FileBackedTaskManager fileBackedTaskManager;

    @BeforeEach
    public void setFileBackedTaskManager() {
        fileBackedTaskManager = new FileBackedTaskManager();
    }

    public static Task[] createTimeTasksArray(int size) {
        Task[] tasks = new Task[size];
        LocalDateTime t = LocalDateTime.of(2025, 9, 14, 5, 0);

        for (int i = 0; i < size; i++) {
            Duration dur = Duration.ofMinutes(10);            // фикс-продолжительность
            tasks[i] = new Task("t" + i, "d" + i, 0, TasksStatus.NEW, dur, t);
            t = t.plusMinutes(15);                             // 10 мин занято + 5 мин зазор
        }
        return tasks;
    }

    public static Epic[] createTimeEpicsArray(int size) {
        Epic[] epics = new Epic[size];
        LocalDateTime st = LocalDateTime.of(2025, 9, 14, 4, 0);


        for (int i = 0; i < size; i++) {
            Duration dur = Duration.ofMinutes(10);            // фикс-продолжительность
            LocalDateTime et = st.plus(dur);
            epics[i] = new Epic("t" + i, "d" + i, 0, TasksStatus.NEW, dur, st, et);
            st = st.plusMinutes(15);                             // 10 мин занято + 5 мин зазор
        }
        return epics;
    }

    public static SubTask[] createTimeSubTasksArray(int size, int epicId) {
        SubTask[] subTasks = new SubTask[size];
        LocalDateTime t = LocalDateTime.of(2025, 9, 14, 4, 0);

        for (int i = 0; i < size; i++) {
            Duration dur = Duration.ofMinutes(10); // фикс-продолжительность
            subTasks[i] = new SubTask("t" + i, "d" + i, 0, TasksStatus.NEW, dur, t,
                    epicId);
            t = t.plusMinutes(15); // 10 мин занято + 5 мин зазор
        }
        return subTasks;
    }

    @Test
    public void saveEmptyFile() throws IOException {
        Files.createTempFile("tasks_", ".csv"); // принимает имя файла и расширение

        // добавляем и удаляем Task
        Task task = createTimeTasksArray(1)[0];
        fileBackedTaskManager.addTask(task);
        fileBackedTaskManager.removeTask(task.getId());

        // добавляем Epic
        Epic epic = createTimeEpicsArray(1)[0];
        fileBackedTaskManager.addEpicTask(epic);

        // берём реальный id добавленного эпика
        int epicId = fileBackedTaskManager.getAllEpics().get(0).getId();

        // создаём SubTask уже с правильным epicId
        SubTask subTask = createTimeSubTasksArray(1, epicId)[0];
        fileBackedTaskManager.addSubTask(subTask);
        fileBackedTaskManager.removeSubTask(subTask.getId());

        // теперь можно удалить и сам эпик
        fileBackedTaskManager.removeEpicTask(epicId);

        // проверяем файл и пустое состояние менеджера
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
        Task[] tasks = createTimeTasksArray(3);
        Epic[] epics = createTimeEpicsArray(3);
        for (Epic e : epics) fileBackedTaskManager.addEpicTask(e);
        SubTask[] subTasks = createTimeSubTasksArray(3, epics[2].getId());
        for (SubTask s : subTasks) fileBackedTaskManager.addSubTask(s);
        for (Task t : tasks)  fileBackedTaskManager.addTask(t);
        assertEquals(3, fileBackedTaskManager.getAllTasks().size());
        assertEquals(3, fileBackedTaskManager.getAllEpics().size());
        assertEquals(3, fileBackedTaskManager.getAllSubTasks().size());
    }

    @Test
    public void loadTasksInFile() throws IOException {
        Files.createTempFile("test_", ".txt"); // принимает имя файла и расширение
        Task[] tasks = createTimeTasksArray(3);
        Epic[] epics = createTimeEpicsArray(3);
        FileBackedTaskManager fileBackedTaskManager2 = new FileBackedTaskManager();
        for (Epic e : epics) fileBackedTaskManager2.addEpicTask(e);
        int epicId = fileBackedTaskManager2.getAllEpics().get(2).getId();
        SubTask[] subTasks = createTimeSubTasksArray(3, epicId);
        for (SubTask s : subTasks) fileBackedTaskManager2.addSubTask(s);
        for (Task t : tasks)  fileBackedTaskManager2.addTask(t);

        // загружаем из файла в новый объект !!!
        fileBackedTaskManager2 = FileBackedTaskManager.loadFromFile(Paths.get("tasks.csv").toFile());
        // после загрузки в менеджере все есть все восстановленные задачи из файла
        assertEquals(3, fileBackedTaskManager2.getAllTasks().size());
        assertEquals(3, fileBackedTaskManager2.getAllEpics().size());
        assertEquals(3, fileBackedTaskManager2.getAllSubTasks().size());
    }

    @Test
    public void saveAndLoadDoesNotThrow() {
        File tmp = assertDoesNotThrow(() -> Files.createTempFile("test_", ".txt").toFile());
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager();
        // кладем задачу, тем самым сохраняемся
        assertDoesNotThrow( () ->
                fileBackedTaskManager.addTask(createTimeTasksArray(1)[0])
        );
        // теперь загружаемся
        assertDoesNotThrow( () ->
                FileBackedTaskManager.loadFromFile(tmp)
        );
    }

    // с битым путем к файлу
    @Test
    public void saveAndLoadDoesThrowFromBadPath() throws IOException {
        File file = Files.createTempFile("bad_", "csv").toFile();
        Files.delete(file.toPath());
        assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(file));
    }
}
