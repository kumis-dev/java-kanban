import org.junit.jupiter.api.Test;
import taskmanager.*;
import taskmanager.taskservice.Epic;
import taskmanager.taskservice.SubTask;
import taskmanager.taskservice.Task;
import taskmanager.taskservice.TasksStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest extends TaskManagerTest {

    @Test
    public void shouldEpicNotAddToSubTasksHimself() {
        InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();
        Epic epic = new Epic("Какой-то эпик", "его описание", 1, TasksStatus.NEW);
        inMemoryTaskManager.addEpicTask(epic); // добавим созданный эпик

        // добавим подзадачу для проверки
        SubTask subTask = new SubTask("Саб-таск", "описание подзадачи",
                2, TasksStatus.NEW, 1);
        inMemoryTaskManager.addSubTask(subTask);

        // epic.getSubTasks().add(epic.getId()); - так эпик добавит сам себя в список подзадач

        List<Integer> subTasksId = epic.getSubTasks(); // получаем айди саб тасков эпика
        assertFalse(subTasksId.contains(epic.getId()), "taskManager.Epic содержит себя в списке подзадач!");
    }

    @Test
    public void shouldSubTaskNotAddToEpicHimself() {
        InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();
        // этот эпик не должен влиять на эпик сабтаска ниже, создам его чтобы проверить теорию
        Epic epic = new Epic("Какой-то эпик", "его описание", 2, TasksStatus.NEW);
        inMemoryTaskManager.addEpicTask(epic); // добавим созданный эпик

        // добавим подзадачу для проверки
        SubTask subTask = new SubTask("Саб-таск", "описание подзадачи",
                2, TasksStatus.NEW, 1); // при сабТаскАйди = 2 и эпикАйди = 2 тест бы не прошел проверку
        inMemoryTaskManager.addSubTask(subTask);

        assertNotEquals(subTask.getId(), subTask.getEpicId(), "taskManager.SubTask содержит себя в эпике!");
    }

    @Test
    public void shouldReturnNewTaskManagers() {
        assertNotNull(Managers.getDefault());
        assertNotNull(Managers.getDefaultHistory());
    }

    @Test
    public void shouldOverlapTasks() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Task task1 = new Task("постирать", "черные вещи", 1, TasksStatus.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2025, 9, 15, 10, 0));
        Task task2 = new Task("постирать", "красные вещи", 2, TasksStatus.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 9, 15, 10, 30));
        tm.addTask(task1);
        tm.addTask(task2); // не должен быть добавлен т к пересекается
        assertEquals(1, tm.getAllTasks().size());
    }

    @Test
    public void shouldOverlapEpics() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        // Эпик-1: 10:00–11:00
        Epic epic1 = new Epic("Жим лёжа", "10 подходов", 0, TasksStatus.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2025, 9, 15, 10, 0),
                LocalDateTime.of(2025, 9, 15, 11, 0));
        // Эпик-2: 10:30–11:30 - пересекается
        Epic epic2 = new Epic("Подтягивания", "5 подходов", 0, TasksStatus.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2025, 9, 15, 10, 30),
                LocalDateTime.of(2025, 9, 15, 11, 30));
        tm.addEpicTask(epic1);
        int before = tm.getAllEpics().size();     // 1
        tm.addEpicTask(epic2);      // добавляется, несмотря на пересечение, так как для эпиков пересечения разрешены
        assertEquals(before + 1, tm.getAllEpics().size(),
                "Пересекающийся эпик должен добавляться");
    }

    @Test
    public void shouldOverlapSubTasks() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Duration oneHour = Duration.ofMinutes(60);
        Duration halfHour = Duration.ofMinutes(30);
        Epic epic1 = new Epic(
                "Пойти в зал: отжаться 20 раз",
                "Сделать 20 отжиманий",
                3,
                TasksStatus.NEW,
                oneHour,
                LocalDateTime.of(2025, 9, 15, 10, 0),
                LocalDateTime.of(2025, 9, 15, 11, 0));
        SubTask subTask1 = new SubTask("Уйти из зала", "закрыть за собой дверь",
                5, TasksStatus.NEW, halfHour,
                LocalDateTime.of(2025, 9, 15, 10, 15), epic1.getId());
        SubTask subTask2 = new SubTask("Уйти из зала", "не забыть забрать абонемент со стойки",
                6, TasksStatus.NEW, halfHour,
                LocalDateTime.of(2025, 9, 15, 10, 30), epic1.getId());
        tm.addEpicTask(epic1);
        tm.addSubTask(subTask1);
        tm.addSubTask(subTask2); // не должен быть добавлен т к пересекается
        assertEquals(1, tm.getAllSubTasks().size());
    }
}