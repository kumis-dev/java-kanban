package taskManager;

import java.util.List;

public interface TaskManager {
    void addTask(Task task);
    Task getTask(int taskId);
    List<Task> getAllTasks();
    List<Epic> getAllEpics();
    List<SubTask> getAllSubTasks();
    List<SubTask> getEpicSubtasks(int epicId);
    void removeTask(int taskId);
    void removeAllTasks();
    void updateTask(Task task);
    void addEpicTask(Epic epic);
    void removeEpicTask(int epicTaskId);
    void removeAllEpics();
    void removeAllSubTasks();
    void addSubTask(SubTask subTask);
    void removeSubTask(int subTaskId);
    void updateEpic(Epic epic);
    void updateSubTask(SubTask subTask);
    SubTask getSubTask(int subTaskId);
    Epic getEpicTask(int epicTaskId);
    List<Task> getHistory();
}
