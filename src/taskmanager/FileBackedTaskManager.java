package taskmanager;

import taskmanager.exceptions.ManagerSaveException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {

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
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // пропускаем шапку (заголовки), чтобы не возникло исключения по парсингу строки
            while (br.ready()) {
                Task task = fileBackedTaskManager.fromString(br.readLine());
                if (task instanceof Epic) {
                    fileBackedTaskManager.addEpicTask((Epic) task);
                } else if (task instanceof SubTask) {
                    fileBackedTaskManager.addSubTask((SubTask) task);
                // Проверка нужна только здесь: если task null,
                // то instanceof Epic/SubTask выше даст false и в те блоки не попадёт.
                // Поэтому здесь защищаемся от null, чтобы не добавить несуществующую задачу.
                } else if (task != null) {
                    fileBackedTaskManager.addTask(task);
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
        return fileBackedTaskManager;
    }

    public static void main(String[] args) throws IOException {
        Task task1 = new Task("постирать", "черные вещи", 1, TasksStatus.NEW);
        Task task2 = new Task("постирать", "красные вещи", 2, TasksStatus.NEW);
        Epic epic1 = new Epic("Пойти в зал", "отжаться 20 раз", 3, TasksStatus.NEW);
        Epic epic2 = new Epic("Пойти в зал", "подтянуться 5 раз", 4, TasksStatus.NEW);
        SubTask subTask1 = new SubTask("Уйти из зала", "закрыть за собой дверь",
                5, TasksStatus.NEW, 3);
        SubTask subTask2 = new SubTask("Уйти из зала", "не забыть забрать абонемент со стойки",
                6, TasksStatus.NEW, 3);
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
        List<String> lines = Files.readAllLines(Paths.get("tasks.csv"));
        for (String line : lines) {
            String[] cols = line.split(",");
            // Чтобы не было ошибки, делаем длину массива всегда 6 (добавим пустые строки если не хватает)
            String[] fixed = new String[6];
            for (int i = 0; i < fixed.length; i++) {
                fixed[i] = (i < cols.length) ? cols[i].trim() : "";
            }
            System.out.printf("%-5s %-10s %-18s %-12s %-45s %2s%n", fixed);
        }
        System.out.println();
        // и после загрузки файла все также успешно восстанавливается
        FileBackedTaskManager fileBackTaskMng2 = FileBackedTaskManager.loadFromFile(new File("tasks.csv"));
        List<String> lines2 = Files.readAllLines(Paths.get("tasks.csv"));
        for (String line : lines2) {
            String[] cols = line.split(",");
            // Чтобы не было ошибки, делаем длину массива всегда 6 (добавим пустые строки если не хватает)
            String[] fixed = new String[6];
            for (int i = 0; i < fixed.length; i++) {
                fixed[i] = (i < cols.length) ? cols[i].trim() : "";
            }
            System.out.printf("%-5s %-10s %-18s %-12s %-45s %2s%n", fixed);
        }
        System.out.println(fileBackTaskMng1.getAllTasks().size() == fileBackTaskMng2.getAllTasks().size());
        System.out.println(fileBackTaskMng1.getAllEpics().size() == fileBackTaskMng2.getAllEpics().size());
        System.out.println(fileBackTaskMng1.getAllSubTasks().size() == fileBackTaskMng2.getAllSubTasks().size());
    }

    private String toString(Task task) {
        if (task instanceof SubTask) {
            return task.getId() + "," + task.getType() + "," + task.getNameTask()
                    + "," + task.getTasksStatus() + "," + task.getDescription() + ","
                    + ((SubTask) task).getEpicId();
        }
        return task.getId() + "," + task.getType() + "," + task.getNameTask()
                + "," + task.getTasksStatus() + "," + task.getDescription();
    }

    private Task fromString(String value) {
        // на вход пример - 1,TASK,Task1,NEW,Description task1,
        String[] task = value.split(",");
        String typeTask = task[1];
        try {
            switch (typeTask) {
                case "TASK" -> {
                    return new Task(task[2], task[4], Integer.parseInt(task[0]), TasksStatus.valueOf(task[3]));
                }
                case "EPIC" -> {
                    return new Epic(task[2], task[4], Integer.parseInt(task[0]), TasksStatus.valueOf(task[3]));
                }
                case "SUBTASK" -> {
                    return new SubTask(task[2], task[4], Integer.parseInt(task[0]), TasksStatus.valueOf(task[3]),
                            Integer.parseInt(task[5]));
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
            fw.write("id,type,name,status,description,epic" + "\n");

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
