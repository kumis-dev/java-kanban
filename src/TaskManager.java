import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class TaskManager {
    // общие задачи
    private static HashMap<Integer, Task> tasks = new HashMap<>();
    // эпики - главные задачи
    private static HashMap<Integer, Epic> epics = new HashMap<>();
    // саб таски - подзадачи
    private static HashMap<Integer, SubTask> subTasks = new HashMap<>();

    public static int epicTaskId = 1;
    public static int subTaskId = 1;
    public static int taskId = 1; // для обычных задач

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in); // создадим сканнер чтобы пользователь мог вводить задачи
        String nameTask;
        String description;
        SubTask subTask;
        while (true) {
            printMenu();
            int command = scan.nextInt();
            scan.nextLine();
            switch (command) {
                case 1:
                    System.out.println("Введите имя задачи:");
                    nameTask = scan.nextLine();
                    System.out.println("Введите описание задачи:");
                    description = scan.nextLine();
                    addTask(nameTask, description);
                    break;
                case 2:
                    System.out.println("Введите имя эпика (главной задачи):");
                    nameTask = scan.nextLine();
                    // для эпика будет 1 описание
                    System.out.println("Введите описание эпика:");
                    description = scan.nextLine();
                    addEpicTask(nameTask, description);
                    break;
                case 3:
                    System.out.println("Введите имя саб таска (подзадачи):");
                    nameTask = scan.nextLine();
                    // для каждого саб таска по 1 описанию, саб тасков может быть много, но эпик у них один
                    System.out.println("Введите описание саб таска:");
                    description = scan.nextLine();
                    System.out.println("Введите айди нужого эпика для добавления в него саб тасков:");
                    int targetEpicId = scan.nextInt();
                    addSubTask(nameTask, description, targetEpicId);
                    break;
                case 4:
                    System.out.println("Введите айди задачи:");
                    int id = scan.nextInt();
                    removeTask(id);
                    break;
                case 5:
                    System.out.println("Для удаления эпика введите его айди:");
                    System.out.println("Айди будет номером эпика в порядке добавления:");
                    int epicIdToRemove = scan.nextInt();
                    removeEpicTask(epicIdToRemove); // удаляем эпик по его айди
                    break;
                case 6:
                    System.out.println("Для удаления саб таска введите его айди:");
                    System.out.println("Айди будет номером саб таска в порядке добавления:");
                    int subTaskIdToRemove = scan.nextInt();
                    removeSubTask(subTaskIdToRemove); // удаляем саб таск по его айди
                    break;
                case 7:
                    removeAllTasks();
                    epics.clear();
                    subTasks.clear();
                    System.out.println("Все задачи очищены");
                    break;
                case 8:
                    System.out.println("Введите айди задачи, которую хотите обновить:");
                    int updateId = scan.nextInt();
                    scan.nextLine();
                    Task oldTask = getTask(updateId);
                    if (oldTask == null) {
                        System.out.println("Задача не найдена");
                        break;
                    }
                    System.out.println("Введите новое имя задачи:");
                    String newTaskName = scan.nextLine();
                    System.out.println("Введите новое описание задачи:");
                    String newTaskDescription = scan.nextLine();
                    Task updatedTask = new Task(newTaskName, newTaskDescription, updateId, oldTask.getTasksStatus());
                    updateTask(updatedTask);
                    break;
                case 9:
                    System.out.println("Для прекращения ввода нового эпика введите 0");
                    System.out.println("Введите имя эпика (главной задачи):");
                    String newNameEpic = scan.nextLine();
                    if (newNameEpic.equals("0"))
                        break;
                    System.out.println("Введите описание эпика:");
                    String newEpicDescription = scan.nextLine();
                    if (newEpicDescription.equals("0"))
                        break;
                    System.out.println("Введите айди эпика, которое хотите обновить:");
                    int newEpicId = scan.nextInt();
                    scan.nextLine();
                    // получим старый эпик
                    Epic oldEpic = getEpicTask(newEpicId);
                    if (oldEpic == null) {
                        System.out.println("Такой айди эпика не найден");
                        break;
                    }
                    ArrayList<Integer> oldSubTasks = oldEpic.getSubTasks();
                    TasksStatus currentStatus = oldEpic.getTasksStatus();
                    Epic newEpic = new Epic(newNameEpic, newEpicDescription, newEpicId, currentStatus);
                    newEpic.setSubTasks(oldSubTasks);
                    updateEpic(newEpic);
                    break;
                case 10:
                    System.out.println("Для прекращения ввода нового саб таска введите 0");
                    System.out.println("Введите имя эпика (главной задачи):");
                    String newNameSubTask = scan.nextLine();
                    if (newNameSubTask.equals("0"))
                        break;
                    System.out.println("Введите описание эпика:");
                    String newSubTaskDescription = scan.nextLine();
                    if (newSubTaskDescription.equals("0"))
                        break;
                    System.out.println("Введите айди cаб таска, которое хотите обновить:");
                    int newSubTaskId = scan.nextInt();
                    scan.nextLine();
                    // получим старый саб таск по его эпику
                    SubTask oldSubTask = getSubTask(newSubTaskId);
                    if (oldSubTask == null) {
                        System.out.println("Такой айди саб таска не найден");
                        break;
                    }
                    TasksStatus oldStatus = oldSubTask.getTasksStatus();
                    SubTask newSubTask = new SubTask(newNameSubTask, newSubTaskDescription, newSubTaskId,
                            oldStatus, oldSubTask.getEpicId());
                    updateSubTask(newSubTask);
                    break;
                case 11:
                    System.out.println("Введите айди саб таска:");
                    int subTaskIdForProgress = scan.nextInt();
                    subTask = subTasks.get(subTaskIdForProgress);
                    if (subTask != null) {
                        subTask.setTasksStatus(TasksStatus.IN_PROGRESS);
                        Epic epic = epics.get(subTask.getEpicId());
                        if (epic != null) {
                            epic.setTasksStatus(changeStatus(subTask.getEpicId()));
                        }
                        System.out.println("Статус саб таска успешно установлен на IN PROGRESS");
                    } else
                        System.out.println("Такой id саб таска не найден");
                    break;
                case 12:
                    System.out.println("Введите айди саб таска:");
                    int subTaskIdForDone = scan.nextInt();
                    subTask = subTasks.get(subTaskIdForDone);
                    if (subTask != null) {
                        subTask.setTasksStatus(TasksStatus.DONE);
                        Epic epic = epics.get(subTask.getEpicId());
                        if (epic != null) {
                            epic.setTasksStatus(changeStatus(subTask.getEpicId()));
                        }
                        System.out.println("Статус саб таска успешно установлен на DONE");
                    } else
                        System.out.println("Такой id саб таска не найден");
                    break;
                case 13:
                    printEpics(epics);
                    break;
                case 14:
                    printSubTasks(subTasks);
                    break;
                case 15:
                    printTasks(tasks, epics, subTasks);
                    break;
                case 16:
                    System.out.println("Введите ID обычной задачи:");
                    int taskStatusId = scan.nextInt();
                    Task taskToUpdate = tasks.get(taskStatusId);
                    if (taskToUpdate == null) {
                        System.out.println("Задача не найдена");
                        break;
                    }
                    System.out.println("Выберите новый статус задачи:");
                    System.out.println("1 - NEW");
                    System.out.println("2 - IN_PROGRESS");
                    System.out.println("3 - DONE");
                    int statusChoice = scan.nextInt();
                    switch (statusChoice) {
                        case 1:
                            taskToUpdate.setTasksStatus(TasksStatus.NEW);
                            System.out.println("Статус задачи установлен на NEW");
                            break;
                        case 2:
                            taskToUpdate.setTasksStatus(TasksStatus.IN_PROGRESS);
                            System.out.println("Статус задачи установлен на IN_PROGRESS");
                            break;
                        case 3:
                            taskToUpdate.setTasksStatus(TasksStatus.DONE);
                            System.out.println("Статус задачи установлен на DONE");
                            break;
                        default:
                            System.out.println("Некорректный выбор статуса");
                    }
                    break;
                case 17:
                    System.out.println("Спасибо что пользовались трекером задач!");
                    return;
            }
        }


    }

    public static void addTask(String nameTask, String description) {
        Task task = new Task(nameTask, description, taskId, TasksStatus.NEW);
        tasks.put(taskId, task);
        taskId++;
    }

    public static Task getTask(int taskId) {
        return tasks.get(taskId);
    }

    public static void removeTask(int taskId) {
        if (tasks.containsKey(taskId)) {
            tasks.remove(taskId);
            System.out.println("Task успешно удален");
        } else {
            System.out.println("Такого id для Task не существует");
        }
    }

    public static void removeAllTasks() {
        tasks.clear();
        System.out.println("Все обычные задачи удалены");
    }

    public static void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
            System.out.println("Task успешно обновлен");
        } else {
            System.out.println("Task с таким id не существует");
        }
    }

    // сама логика внесения задач
    public static void addEpicTask(String nameTask, String description) {
        // создадим эпик и добавим его
        Epic epic = new Epic(nameTask, description, epicTaskId, TasksStatus.NEW);
        epics.put(epicTaskId, epic);
        epicTaskId++;
    }

    public static void removeEpicTask(int epicTaskId) {
        if (epics.containsKey(epicTaskId)) {
            epics.remove(epicTaskId);
            System.out.println("Epic успешно удален");
        } else System.out.println("Такого id для эпика не существует");
    }

    public static void addSubTask(String nameSubTask, String description, int epicTaskId) {
        SubTask subTask = new SubTask(nameSubTask, description, subTaskId, TasksStatus.NEW, epicTaskId);
        subTasks.put(subTaskId, subTask);
        Epic epic = getEpicTask(epicTaskId);
        if (epic == null) {
            System.out.println("Эпик не найден");
            return;
        }
        ArrayList<Integer> subTasks = epic.getSubTasks();
        subTasks.add(subTaskId); // здесь уже добавим в список саб тасков нужный айди саб таска
        subTaskId++;
    }

    public static void removeSubTask(int subTaskId) {
        if (subTasks.containsKey(subTaskId)) {
            // удаляем и из эпика тоже
            SubTask subTask = subTasks.get(subTaskId);
            Epic epic = epics.get(subTask.getEpicId());
            if (epic != null) {
                epic.getSubTasks().remove(Integer.valueOf(subTaskId));
                epic.setTasksStatus(changeStatus(subTask.getEpicId()));
            }
            subTasks.remove(subTaskId);
            System.out.println("SubTask успешно удален");
        } else System.out.println("Такого id для саб таска не существует");
    }

    public static void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        System.out.println("Эпик обновлен успешно!");
    }

    public static void updateSubTask(SubTask subTask) {
        subTasks.put(subTask.getId(), subTask);
        // исправлено: пересчитываем статус эпика
        Epic epic = epics.get(subTask.getEpicId());
        if (epic != null) {
            epic.setTasksStatus(changeStatus(subTask.getEpicId()));
        }
        System.out.println("Саб таск успешно обновлен");
    }

    // по сути не должно касаться пользователя напрямую, если сделать метод приватным
    // статус эпика зависит от всех подзадач сразу
    private static TasksStatus changeStatus(int epicTaskId) {
        int newCount = 0;
        int inProgressCount = 0;
        int doneCount = 0;

        Epic epic = epics.get(epicTaskId); // получаем обьект эпика
        if (epic == null) {
            return TasksStatus.NEW;
        }
        ArrayList<Integer> subTasksIds = epic.getSubTasks();
        if (subTasksIds.isEmpty())
            return TasksStatus.NEW;
        for (Integer subTaskId : subTasksIds) {
            SubTask currentSubTask = subTasks.get(subTaskId); // берем подзадачу под ее айди
            if (currentSubTask == null) continue;
            TasksStatus status = currentSubTask.getTasksStatus();
            switch (status) {
                case NEW -> newCount++;
                case IN_PROGRESS -> inProgressCount++;
                case DONE -> doneCount++;
            }
        }
        if (subTasksIds.size() == newCount) {
            return TasksStatus.NEW;
        } else if (subTasksIds.size() == doneCount) {
            return TasksStatus.DONE;
        } else return TasksStatus.IN_PROGRESS;
    }

    public static SubTask getSubTask(int subTaskId) {
        return subTasks.get(subTaskId);
    }

    public static Epic getEpicTask(int epicTaskId) {
        return epics.get(epicTaskId); // возвращаем обьект эпика по его id
    }

    public static void printEpics(HashMap<Integer, Epic> epics) {
        if (epics.isEmpty()) {
            System.out.println("Эпиков нету");
            return;
        }

        for (Integer epic : epics.keySet()) {
            System.out.println(epics.get(epic));
        }
    }

    public static void printTasks(HashMap<Integer, Task> tasks, HashMap<Integer, Epic> epics,
                                  HashMap<Integer, SubTask> epicSubTasks) {
        if (tasks.isEmpty() && epics.isEmpty()) {
            System.out.println("Задач нету");
            return;
        }
        for (Integer task : tasks.keySet()) {
            System.out.println(tasks.get(task));
        }

        for (Integer epic : epics.keySet()) {
            System.out.println(epics.get(epic));
            // epics.get(epic) - вернет сам обьект эпик, у него мы получим саб таски
            for (Integer subTaskId : epics.get(epic).getSubTasks()) {
                System.out.println(epicSubTasks.get(subTaskId));
            }
            // реализуем вывод описания конкретного эпика
        }
    }

    public static void printSubTasks(HashMap<Integer, SubTask> subTasks) {
        if (subTasks.isEmpty()) {
            System.out.println("Подзадач нету");
            return;
        }
        for (Integer subTask : subTasks.keySet()) {
            System.out.println(subTasks.get(subTask));
            // реализуем вывод описания конкретного эпика
        }
    }

    public static void printMenu() {
        System.out.println();
        System.out.println("Вас приветствует трекер задач!");
        System.out.println("Выберите что хотите сделать, отправив нужную цифру:");
        System.out.println("1) Добавить обычную задачу");
        System.out.println("2) Добавить эпик");
        System.out.println("3) Добавить саб таск для существующего эпика");
        System.out.println("4) Удалить обычную задачу");
        System.out.println("5) Удалить эпик");
        System.out.println("6) Удалить саб таск для существующего эпика");
        System.out.println("7) Удалить все существующие задачи");
        System.out.println("8) Обновить обычную задачу");
        System.out.println("9) Обновить эпик");
        System.out.println("10) Обновить саб таск");
        System.out.println("11) Пометить саб таск как в прогрессе");
        System.out.println("12) Пометить саб таск сделанным"); // для этого она должна находиться в статусе IN_PROGRESS
        System.out.println("13) Распечатать список эпиков");
        System.out.println("14) Распечатать список саб тасков");
        System.out.println("15) Распечатать список всех задач");
        System.out.println("16) Изменить статус обычной задачи");
        System.out.println("17) Выйти из трекера задач");
        System.out.println();
        // статусы находятся в перечислении TasksStatus
    }


}
