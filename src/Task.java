// этот класс мы реализуем для хранения только 1 задачи
import java.util.Objects;

public class Task {
    private int id; // у каждой задачи должен быть свой уникальный id
    private String nameTask;
    private String description;
    private TasksStatus tasksStatus; // здесь будем хранить статус задачи

    // создадим конструктор для наследников, таких как subTask
    public Task(String nameTask, String description, int id, TasksStatus tasksStatus) {
        this.nameTask = nameTask;
        this.description = description;
        this.id = id;
        this.tasksStatus = tasksStatus;
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

    public int getId() {
        return id;
    }

    public TasksStatus getTasksStatus() {
        return tasksStatus;
    }

    public void setTasksStatus(TasksStatus tasksStatus) {
        this.tasksStatus = tasksStatus;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (this.getClass() != object.getClass()
                || object == null) return false;
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
        return "Task{" +
                "id=" + id +
                ", nameTask='" + nameTask + '\'' +
                ", description='" + description + '\'' +
                ", tasksStatus=" + tasksStatus +
                '}';
    }
}
