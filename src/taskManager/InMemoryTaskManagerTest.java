package taskManager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {
    Epic epic1 = new Epic("Пойти в зал", "отжаться 20 раз", 2, TasksStatus.NEW);
    Epic epic2 = new Epic("Пойти в зал", "подтянуться 5 раз", 2, TasksStatus.NEW);
    SubTask subTask1 = new SubTask("Уйти из зала", "закрыть за собой дверь",
            3, TasksStatus.NEW, 2);
    SubTask subTask2 = new SubTask("Уйти из зала", "не забыть забрать абонемент со стойки",
            3, TasksStatus.NEW, 2);
    static TaskManager taskManager;

    @BeforeAll
    public static void createTasks() {
        taskManager = Managers.getDefault();
        for (int i = 1; i <= 11; i++) {
            Task task = new Task("t" + i, "d" + i, i, TasksStatus.NEW);
            taskManager.addTask(task);
            taskManager.getTask(i);
        }
    }

    @Test
    public void shouldBeTaskEqualsId() {
        Task task1 = new Task("постирать", "черные вещи", 1, TasksStatus.NEW);
        Task task2 = new Task("постирать", "красные вещи", 1, TasksStatus.NEW);
        assertEquals(task1.getId(), task2.getId());
    }

    @Test
    public void shouldBeEpicEqualsId() {
        assertEquals(epic1.getId(), epic2.getId());
    }

    @Test
    public void shouldBeSubTaskEqualsId() {
        assertEquals(subTask1.getId(), subTask2.getId());
    }

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

        ArrayList<Integer> subTasksId = epic.getSubTasks(); // получаем айди саб тасков эпика
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
    public void shouldAddOtherTypesInMemoryTaskManagerAndFindTheirId() {
        TaskManager taskManager = Managers.getDefault();
        taskManager.addTask(new Task("Новая задача", "Дефолтная", 1, TasksStatus.NEW));
        taskManager.addEpicTask(epic1);
        taskManager.addSubTask(subTask1);
        assertEquals(1, taskManager.getTask(1).getId());
        assertEquals(2, taskManager.getEpicTask(2).getId());
        assertEquals(3, taskManager.getSubTask(3).getId());
    }

    @Test
    public void shouldSpecifiedIdAndGeneratedIdNotConflict() {
        TaskManager taskManager = Managers.getDefault();
        Task task1 = new Task("тест", "описание", 17, TasksStatus.NEW);
        taskManager.addTask(task1);

        Task task2 = new Task("задача", "описание 2", 2, TasksStatus.NEW);
        taskManager.addTask(task2);

        assertNotEquals(task1.getId(), task2.getId(), "Айди задач конфликтуют");
    }

    @Test
    public void shouldAlwaysAssignManagerGeneratedId() {
        TaskManager taskManager = Managers.getDefault();
        Task task1 = new Task("тест", "описание", 17, TasksStatus.NEW);
        taskManager.addTask(task1);
        // после добавления в менеджер задач заданное айди сменится на сгенерированное самим менеджером задач

        assertNotEquals(17, task1.getId()); // проверяем что заданное айди не равно реальному
    }

    @Test
    public void shouldNotChangeFieldsTaskManager() {
        TaskManager taskManager = Managers.getDefault();
        Task task1 = new Task("t1", "d1", 1, TasksStatus.NEW);
        taskManager.addTask(task1);
        // проверяем все ли совпадает именно у задачи внутри таск менеджера
        assertEquals("t1", taskManager.getTask(1).getNameTask());
        assertEquals("d1", taskManager.getTask(1).getDescription());
        assertEquals(1, taskManager.getTask(1).getId());
        assertEquals(TasksStatus.NEW, taskManager.getTask(1).getTasksStatus());
    }

    @Test
    public void shouldNotOverflowHistoryManager() {
        List<Task> historyTaskManager = taskManager.getHistory();

        assertEquals(historyTaskManager.get(0).getId(), 2);
        // первый элемент с id 1 удалился и поэтому заменится следующим
        assertEquals(historyTaskManager.get(historyTaskManager.size() - 1).getId(), 11);
        // последний элемент с id 11 должен был успешно добавить и удалить первый элемент с его id
    }

    @Test
    public void shouldSavePreviousVersionHistoryManager() {
        TaskManager taskManager = Managers.getDefault();
        Task task1 = new Task("t1", "d1", 1, TasksStatus.NEW);
        taskManager.addTask(task1);
        taskManager.getTask(1);

        Task task2 = new Task("t2", "d2", 2, TasksStatus.NEW);
        taskManager.addTask(task2);
        taskManager.getTask(2);

        task1.setTasksStatus(TasksStatus.IN_PROGRESS);
        taskManager.updateTask(task1);
        taskManager.getTask(1);

        task2.setTasksStatus(TasksStatus.DONE);
        taskManager.updateTask(task2);
        taskManager.getTask(2);

        List<Task> historyTaskManager = taskManager.getHistory(); // вызываем историю уже после взаимодействия с тасками

        assertNotEquals(historyTaskManager.get(0).getTasksStatus(), historyTaskManager.get(2).getTasksStatus());
        assertNotEquals(historyTaskManager.get(1).getTasksStatus(), historyTaskManager.get(3).getTasksStatus());
    }

    @Test
    public void shouldRemoveEpicTaskWhenEpicIsNull() {
        TaskManager taskManager = Managers.getDefault();

        // попытка удаления эпика, которого нет
        assertDoesNotThrow(() -> taskManager.removeEpicTask(999));
    }

    @Test
    public void shouldRemoveSubTaskWhenSubTaskIsNull() {
        TaskManager taskManager = Managers.getDefault();

        // попытка удаления саб таска, которого нет
        assertDoesNotThrow(() -> taskManager.removeSubTask(777));
    }

    @Test
    public void shouldRemoveTaskWhenTaskIsNull() {
        TaskManager taskManager = Managers.getDefault();

        // попытка удаления таска, которого нет
        assertDoesNotThrow(() -> taskManager.removeTask(888));
    }

    @Test
    public void shouldEpicStatusIsIn_ProgressWhenSubTask1StatusIsNewAndSubTask2StatusIsDone() {
        TaskManager taskManager = Managers.getDefault();

        // Создаём Epic и добавляем его
        Epic epic = new Epic("Epic", "desc", 100, TasksStatus.NEW);
        taskManager.addEpicTask(epic);

        // Добавляем 2 подзадачи: одна NEW, другая DONE
        SubTask subTask1 = new SubTask("sub1", "d1", 101, TasksStatus.NEW, epic.getId());
        SubTask subTask2 = new SubTask("sub2", "d2", 102, TasksStatus.DONE, epic.getId());

        taskManager.addSubTask(subTask1);
        taskManager.addSubTask(subTask2);

        // Проверяем, что статус Epic — IN_PROGRESS
        Epic updatedEpic = taskManager.getEpicTask(epic.getId());
        assertEquals(TasksStatus.IN_PROGRESS, updatedEpic.getTasksStatus());
    }
}