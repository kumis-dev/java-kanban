package taskmanager;

import taskmanager.taskservice.Epic;
import taskmanager.taskservice.SubTask;
import taskmanager.taskservice.Task;

import java.util.List;
import java.util.Optional;

public interface TaskManager {
    void addTask(Task task);

    Optional<Task> getTask(int taskId);

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

    Optional<SubTask> getSubTask(int subTaskId);

    Optional<Epic> getEpicTask(int epicTaskId);

    List<Task> getHistory();
}
