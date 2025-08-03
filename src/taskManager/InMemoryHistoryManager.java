package taskManager;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    // история получения последних 10 тасков
    private List<Task> taskHistory = new ArrayList<>();

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(taskHistory);
    }

    @Override
    public void addToHistory(Task task) {
        if (taskHistory.size() == 10) {
            taskHistory.remove(0);
        }
        // создаем новый объект, тем самым избегая изменений по ссылке одного объекта в памяти
        Task oldVersion = new Task(task.getNameTask(), task.getDescription(), task.getId(), task.getTasksStatus());
        taskHistory.add(oldVersion);
    }
}
