import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import taskmanager.Managers;
import taskmanager.TaskManager;
import taskmanager.taskservice.Epic;
import taskmanager.taskservice.SubTask;
import taskmanager.taskservice.Task;
import taskmanager.taskservice.TasksStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest {
    Epic epic1 = new Epic("Пойти в зал", "отжаться 20 раз", 2, TasksStatus.NEW);
    Epic epic2 = new Epic("Пойти в зал", "подтянуться 5 раз", 2, TasksStatus.NEW);
    SubTask subTask1 = new SubTask("Уйти из зала", "закрыть за собой дверь",
            3, TasksStatus.NEW, 2);
    SubTask subTask2 = new SubTask("Уйти из зала", "не забыть забрать абонемент со стойки",
            3, TasksStatus.NEW, 2);
    static TaskManager taskManager;

    @BeforeAll
    public static void create11Tasks() {
        taskManager = Managers.getDefault();
        for (int i = 1; i <= 11; i++) {
            Task task = new Task("t" + i, "d" + i, i, TasksStatus.NEW);
            taskManager.addTask(task);
            taskManager.getTask(i);
        }
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

    public static Task[] createTasksArray(int size) {
        Task[] tasks = new Task[size];
        for (int i = 0; i < size; i++) {
            Task t = new Task("t" + i, "d" + i, i, TasksStatus.NEW);
            tasks[i] = t;
        }
        return tasks;
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
    public void shouldAddOtherTypesInMemoryTaskManagerAndFindTheirId() {
        TaskManager taskManager = Managers.getDefault();
        taskManager.addTask(new Task("Новая задача", "Дефолтная", 1, TasksStatus.NEW));
        taskManager.addEpicTask(epic1);
        taskManager.addSubTask(subTask1);
        assertEquals(1, taskManager.getTask(1).get().getId()); // через get распаковывам с Optional
        assertEquals(2, taskManager.getEpicTask(2).get().getId());
        assertEquals(3, taskManager.getSubTask(3).get().getId());
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
        assertEquals("t1", taskManager.getTask(1).get().getNameTask());
        assertEquals("d1", taskManager.getTask(1).get().getDescription());
        assertEquals(1, taskManager.getTask(1).get().getId());
        assertEquals(TasksStatus.NEW, taskManager.getTask(1).get().getTasksStatus());
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
        Optional<Epic> updatedEpic = taskManager.getEpicTask(epic.getId());
        assertTrue(updatedEpic.isPresent());
        assertEquals(TasksStatus.IN_PROGRESS, updatedEpic.get().getTasksStatus());
    }

    @Test
    public void shouldEpicStatusIsNewWhenAllSubTasksStatusIsNew() {
        TaskManager taskManager = Managers.getDefault();

        // Создаём Epic и добавляем его
        Epic epic = new Epic("Epic", "desc", 100, TasksStatus.NEW);
        taskManager.addEpicTask(epic);

        // Добавляем 2 подзадачи: одна NEW, другая DONE
        SubTask subTask1 = new SubTask("sub1", "d1", 101, TasksStatus.NEW, epic.getId());
        SubTask subTask2 = new SubTask("sub2", "d2", 102, TasksStatus.NEW, epic.getId());

        taskManager.addSubTask(subTask1);
        taskManager.addSubTask(subTask2);

        // Проверяем, что статус Epic — NEW
        Optional<Epic> updatedEpic = taskManager.getEpicTask(epic.getId());
        assertTrue(updatedEpic.isPresent());
        assertEquals(TasksStatus.NEW, updatedEpic.get().getTasksStatus());
    }

    @Test
    public void shouldEpicStatusIsIn_ProgressWhenAllSubTasksStatusIsIn_Progress() {
        TaskManager taskManager = Managers.getDefault();

        // Создаём Epic и добавляем его
        Epic epic = new Epic("Epic", "desc", 100, TasksStatus.IN_PROGRESS);
        taskManager.addEpicTask(epic);

        // Добавляем 2 подзадачи: одна NEW, другая DONE
        SubTask subTask1 = new SubTask("sub1", "d1", 101, TasksStatus.IN_PROGRESS, epic.getId());
        SubTask subTask2 = new SubTask("sub2", "d2", 102, TasksStatus.IN_PROGRESS, epic.getId());

        taskManager.addSubTask(subTask1);
        taskManager.addSubTask(subTask2);

        // Проверяем, что статус Epic — DONE
        Optional<Epic> updatedEpic = taskManager.getEpicTask(epic.getId());
        assertTrue(updatedEpic.isPresent());
        assertEquals(TasksStatus.IN_PROGRESS, updatedEpic.get().getTasksStatus());
    }

    @Test
    public void shouldEpicStatusIsDoneWhenAllSubTasksStatusIsDone() {
        TaskManager taskManager = Managers.getDefault();

        // Создаём Epic и добавляем его
        Epic epic = new Epic("Epic", "desc", 100, TasksStatus.DONE);
        taskManager.addEpicTask(epic);

        // Добавляем 2 подзадачи: одна NEW, другая DONE
        SubTask subTask1 = new SubTask("sub1", "d1", 101, TasksStatus.DONE, epic.getId());
        SubTask subTask2 = new SubTask("sub2", "d2", 102, TasksStatus.DONE, epic.getId());

        taskManager.addSubTask(subTask1);
        taskManager.addSubTask(subTask2);

        // Проверяем, что статус Epic — DONE
        Optional<Epic> updatedEpic = taskManager.getEpicTask(epic.getId());
        assertTrue(updatedEpic.isPresent());
        assertEquals(TasksStatus.DONE, updatedEpic.get().getTasksStatus());
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
    public void shouldChangeIntoTaskNotInfluenceOnTaskManager() {
        TaskManager taskManager = Managers.getDefault();
        Task[] tasks = createTasksArray(2);
        taskManager.addTask(tasks[0]);
        taskManager.addTask(tasks[1]);
        int firstTaskID = tasks[0].getId();
        Optional<Task> task1 = taskManager.getTask(1);
        task1.get().setTasksStatus(TasksStatus.DONE); // через get используем распаковку
        assertEquals(taskManager.getTask(firstTaskID).get().getTasksStatus(), TasksStatus.NEW);
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
    void shouldSubtaskBeLinkedWithEpic() {
        TaskManager tm = Managers.getDefault();
        Epic epic = new Epic("E", "d", 0, TasksStatus.NEW);
        tm.addEpicTask(epic);
        SubTask st = new SubTask("S", "d", 0, TasksStatus.NEW, epic.getId());
        tm.addSubTask(st);
        SubTask subTask = tm.getSubTask(st.getId()).get();
        // связь в саб таск не нарушена, айди эпика саб таска и самого эпика совпадают
        assertEquals(epic.getId(), subTask.getEpicId());
        // связь в эпике не нарушена
        List<Integer> id = epic.getSubTasks();
        int id2 = st.getId();
        assertTrue(epic.getSubTasks().contains(st.getId()));
    }
}
