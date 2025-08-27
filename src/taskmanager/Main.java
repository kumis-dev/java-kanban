package taskmanager;

// класс для реализации пользовательского сценария (доп тз)
public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();
        HistoryManager historyManager = Managers.getDefaultHistory();

        // 2 таска
        Task task1 = new Task("Пойти подышать воздухом", "желательно свежим", 1,
                TasksStatus.NEW);
        Task task2 = new Task("Пойти поесть еды на пикнике", "начнем с шашлыков, закончим омарами",
                2, TasksStatus.NEW);
        // эпик с 3 подзадачами
        Epic epic1 = new Epic("Поработать", "весь день", 3, TasksStatus.NEW);
        SubTask subTask1 = new SubTask("Начать работу", "в 8:00", 4, TasksStatus.NEW, 3);
        SubTask subTask2 = new SubTask("Пойти на обеденный перерыв", "в 12:00", 5,
                TasksStatus.NEW, 3);
        SubTask subTask3 = new SubTask("Закончить работу", "в 17:00", 6,
                TasksStatus.NEW, 3);
        // эпик без саб тасков
        Epic epic = new Epic("epic", "epic description", 7, TasksStatus.NEW);

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        taskManager.addEpicTask(epic1);
        taskManager.addSubTask(subTask1);
        taskManager.addSubTask(subTask2);
        taskManager.addSubTask(subTask3);

        taskManager.addEpicTask(epic);

        // запрашиваем созданные задачи несколько раз в разном порядке
        taskManager.getTask(1);
        System.out.println(taskManager.getHistory());
        taskManager.getSubTask(4);
        System.out.println(taskManager.getHistory());
        taskManager.getEpicTask(3);
        System.out.println(taskManager.getHistory());
        taskManager.getTask(2);
        System.out.println(taskManager.getHistory());
        taskManager.getSubTask(4);
        System.out.println(taskManager.getHistory());
        taskManager.getSubTask(5);
        System.out.println(taskManager.getHistory());
        taskManager.getEpicTask(7);
        System.out.println(taskManager.getHistory());
        taskManager.getSubTask(5);
        System.out.println(taskManager.getHistory());
        taskManager.getSubTask(6);
        System.out.println(taskManager.getHistory());

        System.out.println();

        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }

//      System.out.println(taskManager.getHistory());
        System.out.println();
        // проверяем что подзадача при удалении не будет выводиться
        taskManager.removeTask(1);
        boolean isRemove = true;
        for (Task task : taskManager.getHistory()) {
            if (task.getId() == task1.getId()) {
                System.out.println(task);
                isRemove = false;
                break;
            }
        }

        if (isRemove) {
            System.out.println("Задача успешно удалена");
        }


        System.out.println();
        // проверяем что эпик с 3 подзадачами при удалении не будет выводиться
        taskManager.removeEpicTask(3);
        isRemove = true;
        for (Task task : taskManager.getHistory()) {
            if (task.getId() == epic1.getId()) {
                System.out.println(task);
                isRemove = false;
                break;
            }
        }

        if (isRemove) {
            System.out.println("Эпик с 3 подзадачами успешно удален");
        }

        System.out.println();
        // проверяем что подзадача при удалении не будет выводиться
            taskManager.removeSubTask(4);
        isRemove = true;
        for (Task task : taskManager.getHistory()) {
            if (task.getId() == subTask1.getId()) {
                System.out.println(task);
                isRemove = false;
                break;
            }
        }

        if (isRemove) {
            System.out.println("Подзадача успешно удалена");
        }
    }
}
