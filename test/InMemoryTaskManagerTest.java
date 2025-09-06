import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import taskmanager.*;

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

        for (Task task : createTasksArray(11)) {
            taskManager.addTask(task);
        }

        taskManager.getTask(1); // вставили в конец (... 11 -> 1)
        taskManager.getTask(2); // также (... 11 -> 1 -> 2)
        taskManager.getTask(1); // 1) удаляем И 2) опять в конец 1) (... 11 -> 2) (... 11 -> 2-> 1)
        taskManager.getTask(3); // (... 11 -> 2 -> 1 -> 3)
        taskManager.getTask(2); // finally (4 -> 5 -> 6 -> 7 -> 8 -> 9 -> 10 -> 11 -> 1 -> 3 -> 2)

        List<Task> historyTaskManager = taskManager.getHistory();

        assertEquals(11, historyTaskManager.size()); // проверяем что таск менеджер теперь не фиксированный
        assertEquals(4, historyTaskManager.get(0).getId()); // здесь будет 1ый элемент
        assertEquals(5, historyTaskManager.get(1).getId()); // и по аналогии
        assertEquals(6, historyTaskManager.get(2).getId());
        assertEquals(1, historyTaskManager.get(historyTaskManager.size() - 3).getId()); // этот тоже проверим
        assertEquals(3, historyTaskManager.get(historyTaskManager.size() - 2).getId()); // предпоследний узел
        assertEquals(2, historyTaskManager.get(historyTaskManager.size() - 1).getId()); // последний узел
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

    @Test
    public void shouldAddToHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        for (Task task : createTasksArray(11)) {
            historyManager.add(task);
        }

        // добавляем дубликат задачи с id 1
        historyManager.add(createTasksArray(11)[0]);

        List<Task> history = historyManager.getHistory();

        // размер останется 11, а не 12
        assertEquals(11, history.size()); // проверяем что после добавления дубликата история не увеличилась

        // теперь мы перезаписываем задачу с айди 1, тем самым удалив ноду старой, теперь задача с id 1 добавится в конец
        assertEquals(1, history.get(0).getId()); // теперь задача с id 2 станет первой, т е сдвинется влево
        assertEquals(0, history.get(history.size() - 1).getId()); // а задача с id 1 добавится в конец
    }

    @Test
    public void shouldRemoveToHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        for (Task task : createTasksArray(11)) {
            historyManager.add(task);
        }

        for (Task task : createTasksArray(11)) {
            historyManager.remove(task.getId());
        }

        assertEquals(0, historyManager.getHistory().size()); // проверяем что все истории успешно удалились
    }

    @Test
    public void shouldTasksIsEmpty() {
        TaskManager taskManager = Managers.getDefault();

        for (Epic epic : createEpicsArray(10)) {
            taskManager.addEpicTask(epic);
        }

        for (SubTask subTask : createSubTasksArray(5, 1)) {
            taskManager.addSubTask(subTask);
        }

        taskManager.removeAllTasks();
        taskManager.removeAllEpics();
        taskManager.removeAllSubTasks();

        assertEquals(0, taskManager.getAllEpics().size());
        assertEquals(0, taskManager.getAllSubTasks().size());
        assertEquals(0, taskManager.getAllTasks().size());
        assertEquals(0, taskManager.getHistory().size());
    }

    @Test
    public void shouldNotAddIrrelevantSubTaskIdFromEpic() {
        TaskManager taskManager = Managers.getDefault();
        Epic[] epics = createEpicsArray(1);
        taskManager.addEpicTask(epics[0]);
        SubTask[] subTasks = createSubTasksArray(2, epics[0].getId());
        taskManager.addSubTask(subTasks[0]);
        taskManager.addSubTask(subTasks[1]);

        assertEquals(List.of(subTasks[0].getId(), subTasks[1].getId()), epics[0].getSubTasks());
        taskManager.removeSubTask(subTasks[0].getId()); // удаляем задачу по ее фактическому айди
        assertEquals(List.of(subTasks[1].getId()), epics[0].getSubTasks());
    }

    @Test
    public void shouldChangeIntoTaskNotInfluenceOnTaskManager() {
        TaskManager taskManager = Managers.getDefault();
        Task[] tasks = createTasksArray(2);
        taskManager.addTask(tasks[0]);
        taskManager.addTask(tasks[1]);
        int firstTaskID = tasks[0].getId();
        Task task1 = taskManager.getTask(1);
        task1.setTasksStatus(TasksStatus.DONE);
        assertEquals(taskManager.getTask(firstTaskID).getTasksStatus(), TasksStatus.NEW);
    }

    @Test
    public void noNullException() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertDoesNotThrow(() -> historyManager.add(null));
        assertDoesNotThrow(() -> historyManager.remove(42));
    }

    public Epic[] createEpicsArray(int size) {
        Epic[] epics = new Epic[size];
        for (int i = 0; i < size; i++) {
            Epic epic = new Epic("epic" + i, "d" + i, i, TasksStatus.NEW);
            epics[i] = epic;
        }
        return epics;
    }

    public SubTask[] createSubTasksArray(int size, int epicId) {
        SubTask[] subTasks = new SubTask[size];
        for (int i = 0; i < size; i++) {
            SubTask subTask = new SubTask("subTask" + (i + 1), "d" + (i + 1), 0, TasksStatus.NEW,
                    epicId);
            subTasks[i] = subTask;
        }
        return subTasks;
    }

    public Task[] createTasksArray(int size) {
        Task[] tasks = new Task[size];
        for (int i = 0; i < size; i++) {
            Task t = new Task("t" + i, "d" + i, i, TasksStatus.NEW);
            tasks[i] = t;
        }
        return tasks;
    }
}