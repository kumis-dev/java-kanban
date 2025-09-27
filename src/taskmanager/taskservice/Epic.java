package taskmanager.taskservice;// taskManager.Epic - большая задача, Subtask - подзадача

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    // епик всегда один - и он уникален
    // создадим список subTasks класса taskManager.Epic
    private ArrayList<Integer> subTasks = new ArrayList<>(); // создаем список уникальных id больших задач

    public List<Integer> getSubTasks() {
        if (subTasks == null) {
            subTasks = new ArrayList<>();
        }
        return subTasks;
    }

    public Epic(String nameTask, String description, int id, TasksStatus tasksStatus) {
        super(nameTask, description, id, tasksStatus);
    }

    private LocalDateTime endTime;        // максимальный endTime сабтасков

    @Override
    public LocalDateTime getEndTime() {
        return endTime; // просто возвращаем расчетное время, расчитывать его будем в таск менеджере
    }

    public Epic(String nameTask, String description, int id, TasksStatus tasksStatus, Duration duration,
                LocalDateTime startTime, LocalDateTime endTime) {
        super(nameTask, description, id, tasksStatus, duration, startTime);
        this.endTime = endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public String toString() {
        return "taskManager.Epic{" +
                "epicId=" + getId() +
                ", nameTask='" + getNameTask() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", subTasks=" + subTasks +
                ", tasksStatus=" + getTasksStatus() +
                '}';
    }
}
