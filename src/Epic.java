// Epic - большая задача, Subtask - подзадача
import java.util.ArrayList;

public class Epic extends Task {
    // епик всегда один - и он уникален
    // создадим список subTasks класса Epic
    private ArrayList<Integer> subTasks = new ArrayList<>(); // создаем список уникальных id больших задач

    public void setSubTasks(ArrayList<Integer> subTasks) {
        this.subTasks = subTasks;
    }

    public ArrayList<Integer> getSubTasks() {
        return subTasks;
    }

    public Epic(String nameTask, String description, int id, TasksStatus tasksStatus) {
        super(nameTask, description, id, tasksStatus);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "epicId=" + getId() +
                ", nameTask='" + getNameTask() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", subTasks=" + subTasks +
                ", tasksStatus=" + getTasksStatus() +
                '}';
    }
}
