import org.junit.jupiter.api.Test;
import taskmanager.HistoryManager;
import taskmanager.Managers;
import taskmanager.TaskManager;
import taskmanager.taskservice.Task;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HistoryManagerTest {


    @Test
    public void shouldNotOverflowHistoryManager() {
        TaskManager taskManager = Managers.getDefault();
        for (Task task : TaskManagerTest.createTasksArray(11)) {
            taskManager.addTask(task);
        }
        for (int id = 1; id <= 11; id++) {
            taskManager.getTask(id);
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
    public void shouldAddToHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        for (Task task : TaskManagerTest.createTasksArray(11)) {
            historyManager.add(task);
        }

        // добавляем дубликат задачи с id 1 - для теста на дубликат
        historyManager.add(TaskManagerTest.createTasksArray(11)[0]);

        List<Task> history = historyManager.getHistory();

        // размер останется 11, а не 12
        assertEquals(11, history.size()); // проверяем что после добавления дубликата история не увеличилась

        // теперь мы перезаписываем задачу с id 1, тем самым удалив ноду старой, теперь задача с id 1 добавится в конец
        assertEquals(1, history.get(0).getId()); // теперь задача с id 2 станет первой, т е сдвинется влево
        assertEquals(0, history.get(history.size() - 1).getId()); // а задача с id 1 добавится в конец
    }

    @Test
    public void shouldRemoveToHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        for (Task task : TaskManagerTest.createTasksArray(11)) {
            historyManager.add(task);
        }

        for (Task task : TaskManagerTest.createTasksArray(11)) {
            historyManager.remove(task.getId());
        }
        // тем самым одновременно закрываем и тест на пустую историю
        assertEquals(0, historyManager.getHistory().size()); // проверяем что все истории успешно удалились
    }

    @Test
    public void shouldRemoveFromBeginningMidAndEndingInHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task[] tasks = TaskManagerTest.createTasksArray(11);

        Arrays.stream(tasks).forEach(task -> historyManager.add(task));

        historyManager.remove(0); // удаляем начало
        assertEquals(10, historyManager.getHistory().size());
        assertEquals(1, historyManager.getHistory().get(0).getId());
        historyManager.remove(5); // удаляем середину
        assertEquals(9, historyManager.getHistory().size());
        assertEquals(7, historyManager.getHistory().get(5).getId());
        historyManager.remove(8); // удаляем текущий конец
        assertEquals(8, historyManager.getHistory().size()); // проверяем количество элементов
        assertEquals(10, historyManager.getHistory().get(7).getId()); // а здесь id после смещения
    }

    @Test
    public void noNullExceptionInHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertDoesNotThrow(() -> historyManager.add(null));
        assertDoesNotThrow(() -> historyManager.remove(42));
    }
}
