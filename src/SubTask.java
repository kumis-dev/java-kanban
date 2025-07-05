public class SubTask extends Task {
    private int epicId;
    public SubTask(String nameTask, String description, int id, TasksStatus tasksStatus, int epicId) {
        super(nameTask, description, id, tasksStatus);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "epicId=" + getEpicId() + '\'' +
                ", subTaskId=" + getId() + '\'' +
                ", nameTask='" + getNameTask() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", tasksStatus=" + getTasksStatus() +
                '}';
    }

}
