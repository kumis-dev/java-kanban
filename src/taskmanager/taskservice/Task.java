package taskmanager.taskservice;// этот класс мы реализуем для хранения только 1 задачи

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    private int id; // у каждой задачи должен быть свой уникальный id
    private final String nameTask;
    private final String description;
    private TasksStatus tasksStatus; // здесь будем хранить статус задачи

    private Duration duration; // продолжительность задачи в минутах
    private LocalDateTime startTime; // время начала задачи

    // создадим конструктор для наследников, таких как subTask
    public Task(String nameTask, String description, int id, TasksStatus tasksStatus) {
        this.nameTask = nameTask;
        this.description = description;
        this.id = id;
        this.tasksStatus = tasksStatus;
    }

    // создаем второй конструктор таска с добавлением duration и startTime
    public Task(String nameTask, String description, int id, TasksStatus tasksStatus,
                Duration duration, LocalDateTime startTime) {
        this.nameTask = nameTask;
        this.description = description;
        this.id = id;
        this.tasksStatus = tasksStatus;
        this.duration = duration;
        this.startTime = startTime;
    }

    public String getNameTask() {
        return nameTask;
    }

    public String getDescription() {
        return description;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Task.java
    public void setDuration(Duration duration) {
        this.duration = duration;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public int getId() {
        return id;
    }

    public TasksStatus getTasksStatus() {
        return tasksStatus;
    }

    public void setTasksStatus(TasksStatus tasksStatus) {
        this.tasksStatus = tasksStatus;
    }

    public TaskType getType() {
        return TaskType.TASK;
    }

    // дата времени завершения задачи, расчитывается исходя из startTime и duration
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    public Duration getDuration() {
        return duration;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (this.getClass() != object.getClass()) return false;
        Task task = (Task) object;
        // в сравнении будут поле id, т к сравниваем ток по уникальному идентификатору
        return Objects.equals(this.id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "taskManager.Task{" +
                "id=" + id +
                ", nameTask='" + nameTask + '\'' +
                ", description='" + description + '\'' +
                ", tasksStatus=" + tasksStatus +
                '}';
    }
}
