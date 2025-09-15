package taskmanager;

import taskmanager.exceptions.ManagerSaveException;
import taskmanager.taskservice.Epic;
import taskmanager.taskservice.SubTask;
import taskmanager.taskservice.Task;
import taskmanager.taskservice.TasksStatus;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {

    static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm");
    // каждый раз сохраняем состояния задач в файл

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void addEpicTask(Epic epic) {
        super.addEpicTask(epic);
        save();
    }

    @Override
    public void addSubTask(SubTask subTask) {
        super.addSubTask(subTask);
        save();
    }


    public static FileBackedTaskManager loadFromFile(File file) throws ManagerSaveException {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager();
        int maxId = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            br.readLine(); // пропускаем шапку (заголовки), чтобы не возникло исключения по парсингу строки
            while (br.ready()) {
                Task task = fileBackedTaskManager.fromString(br.readLine());

                maxId = Math.max(maxId, task.getId());
                if (task instanceof Epic) {
                    fileBackedTaskManager.addEpicTask((Epic) task);
                } else if (task instanceof SubTask) {
                    fileBackedTaskManager.addSubTask((SubTask) task);
                    // Проверка нужна только здесь: если task null,
                    // то instanceof Epic/SubTask выше даст false и в те блоки не попадёт.
                    // Поэтому первым условием защищаемся от null, чтобы не добавить несуществующую задачу.
                } else if (task != null) {
                    fileBackedTaskManager.addTask(task);
                }


            }
        } catch (IOException e) {
            throw new ManagerSaveException();
        }

        fileBackedTaskManager.setIdCounter(maxId + 1); // синхронизируем счетчик
        return fileBackedTaskManager;
    }

    public static void main(String[] args) throws IOException {
        Duration duration = Duration.ofMinutes(150);
        Task task1 = new Task("постирать", "черные вещи", 1, TasksStatus.NEW,
                duration, LocalDateTime.now());
        Task task2 = new Task("постирать", "красные вещи", 2, TasksStatus.NEW,
                duration, LocalDateTime.now().plusMinutes(200));
        Epic epic1 = new Epic("Пойти в зал", "отжаться 20 раз", 3, TasksStatus.NEW,
                duration, LocalDateTime.now().plusMinutes(400), LocalDateTime.now().plusMinutes(550));
        Epic epic2 = new Epic("Пойти в зал", "подтянуться 5 раз", 4, TasksStatus.NEW,
                duration, LocalDateTime.now().plusMinutes(800), LocalDateTime.now().plusMinutes(950));
        SubTask subTask1 = new SubTask("Уйти из зала", "закрыть за собой дверь",
                5, TasksStatus.NEW, duration, LocalDateTime.now().plusMinutes(560), 3);
        SubTask subTask2 = new SubTask("Уйти из зала", "не забыть забрать абонемент со стойки",
                6, TasksStatus.NEW, duration, LocalDateTime.now().plusMinutes(960), 3);
        FileBackedTaskManager fileBackTaskMng1 = new FileBackedTaskManager();
        fileBackTaskMng1.addTask(task1);
        fileBackTaskMng1.addTask(task2);
        fileBackTaskMng1.addEpicTask(epic1);
        fileBackTaskMng1.addEpicTask(epic2);
        fileBackTaskMng1.addSubTask(subTask1);
        fileBackTaskMng1.addSubTask(subTask2);
        fileBackTaskMng1.save();
        // проверяем что после сохранения все есть в файле
        // дублирование ради сравнения, без вынесения в метод
        printTableFile();
        System.out.println();
        // и после загрузки файла все также успешно восстанавливается
        FileBackedTaskManager fileBackTaskMng2 = FileBackedTaskManager.loadFromFile(new File("tasks.csv"));
        printTableFile();
        System.out.println(fileBackTaskMng1.getAllTasks().size() == fileBackTaskMng2.getAllTasks().size());
        System.out.println(fileBackTaskMng1.getAllEpics().size() == fileBackTaskMng2.getAllEpics().size());
        System.out.println(fileBackTaskMng1.getAllSubTasks().size() == fileBackTaskMng2.getAllSubTasks().size());
    }

    private static void printTableFile() throws IOException {
        for (String line : Files.readAllLines(Paths.get("tasks.csv"))) {
            String[] cols = line.split(",", -1);
            // Чтобы не было ошибки, делаем длину массива всегда 6 (добавим пустые строки если не хватает)
            String[] fixed = new String[9];
            for (int i = 0; i < fixed.length; i++) {
                fixed[i] = (i < cols.length) ? cols[i].trim() : "";
            }
            //                 id  type  name  status desc epic dur startTime endTime
            System.out.printf("%-5s %-10s %-18s %-12s %-45s %-10s %-15s %-30s %-24s%n",
                    fixed[0], fixed[1], fixed[2], fixed[3], fixed[4],
                    fixed[5], fixed[6], fixed[7], fixed[8]);
        }
    }

    private String toString(Task task) {

        if (task instanceof SubTask) {
            return task.getId() + "," + task.getType() + "," + task.getNameTask()
                    + "," + task.getTasksStatus() + "," + task.getDescription() + ","
                    + ((SubTask) task).getEpicId() + ","
                    + (task.getDuration() != null ? task.getDuration().toMinutes() : "")
                    + "," + task.getStartTime().format(FORMAT) + ",";
        }
        if (task instanceof Epic) {
            return task.getId() + "," + task.getType() + "," + task.getNameTask()
                    + "," + task.getTasksStatus() + "," + task.getDescription()
                    + ",," + (task.getDuration() != null ? task.getDuration().toMinutes() : "")
                    // две запятые чтобы split всегда возвращал 9 элементов
                    + "," + task.getStartTime().format(FORMAT) + "," + task.getEndTime().format(FORMAT);
        }
        return task.getId() + "," + task.getType() + "," + task.getNameTask()
                + "," + task.getTasksStatus() + "," + task.getDescription() + ",,"
                + (task.getDuration() != null ? task.getDuration().toMinutes() : "")
                + "," + task.getStartTime().format(FORMAT) + ",";
    }

    private Task fromString(String value) {
        // на вход пример - 1,TASK,Task1,NEW,Description,duration,startTime,endTime
        String[] task = value.split(",", -1);
        String typeTask = task[1];
        String nameTask = task[2];
        String descTask = task[4];
        int taskId = Integer.parseInt(task[0]);
        TasksStatus taskStatus = TasksStatus.valueOf(task[3].trim());

        Duration duration = !task[6].equals("null")
                && !task[6].isEmpty() ? Duration.ofMinutes(Long.parseLong(task[6])) : null;
        LocalDateTime startTime = !task[7].equals("null")
                && !task[7].isEmpty() ? LocalDateTime.parse(task[7], FORMAT) : null;
        LocalDateTime endTime = !task[8].equals("null")
                && !task[8].isEmpty() ? LocalDateTime.parse(task[8], FORMAT) : null;

        try {
            switch (typeTask) {
                case "TASK" -> {
                    return new Task(nameTask, descTask, taskId, taskStatus, duration, startTime);
                }
                case "EPIC" -> {
                    return new Epic(nameTask, descTask, taskId, taskStatus, duration, startTime, endTime);
                }
                case "SUBTASK" -> {
                    int epicId = Integer.parseInt(task[5]);
                    return new SubTask(nameTask, descTask, taskId, taskStatus, duration, startTime, epicId);
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: статус задачи \"" + task[3] + "\" не найден среди допустимых значений.");
        } catch (NullPointerException e) {
            System.out.println("Ошибка: один из параметров оказался null.");
            e.printStackTrace();
        }
        return null;
    }

    private void save() {
        try (FileWriter fw = new FileWriter("tasks.csv")) {
            fw.write("id,type,name,status,description,epic,duration,startTime,endTime" + "\n");

            for (Task task : getAllTasks()) {
                fw.write(toString(task) + "\n");
            }

            for (Epic epic : getAllEpics()) {
                fw.write(toString(epic) + "\n");
            }

            for (SubTask subTask : getAllSubTasks()) {
                fw.write(toString(subTask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException(); // генерим непроверяемое исключение
        }
    }
}
