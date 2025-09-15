package taskmanager.taskservice;

import java.time.Duration;
import java.time.LocalDateTime;

public class SubTask extends Task {
    private int epicId;

    public SubTask(String nameTask, String description, int id, TasksStatus tasksStatus, int epicId) {
        super(nameTask, description, id, tasksStatus);
        this.epicId = epicId;
    }

    public SubTask(String nameTask, String description, int id, TasksStatus tasksStatus,
                   Duration duration, LocalDateTime startTime, int epicId) {
        super(nameTask, description, id, tasksStatus, duration, startTime);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public String toString() {
        return "taskManager.SubTask{" +
                "epicId=" + getEpicId() + '\'' +
                ", subTaskId=" + getId() + '\'' +
                ", nameTask='" + getNameTask() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", tasksStatus=" + getTasksStatus() +
                '}';
    }

}
