package taskManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    // переменные не должны быть статическими

    // общие задачи
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    // эпики - главные задачи
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    // саб таски - подзадачи
    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();


    // переменная счетчика будет только одна
    private int id = 1; // для всех задач общая
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public void addTask(Task task) {
        // устанавливаем только что сгенерированный айди
        task.setId(generateId());
        // кладем его и саму задачу в хеш мап по обычным таскам
        tasks.put(task.getId(), task);
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values()); // передаем в ArrayList коллекцию значений HashMap tasks
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values()); // передаем в ArrayList коллекцию значений HashMap epics
    }

    @Override
    public List<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks.values()); // передаем в ArrayList коллекцию значений HashMap subTasks
    }

    // получение всех саб тасков определенного эпика
    @Override
    public List<SubTask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        // здесь будем хранить сами саб таски эпика что получили
        ArrayList<SubTask> subTasks = new ArrayList<>(); // затеняем переменную класса на уровне выше - локальной
        if (epic != null) {
            ArrayList<Integer> subTasksIds = epic.getSubTasks();
            for (Integer subTaskId : subTasksIds) {
                subTasks.add(getSubTask(subTaskId));
                // вытаскиваем из общего пула саб тасков и пуляем в список саб тасков эпика
            }
        }
        return subTasks;
    }

    @Override
    public void removeTask(int taskId) {
        tasks.remove(taskId);
    }

    @Override
    public void removeAllTasks() {
        tasks.clear();
    }

    @Override
    public void updateTask(Task task) {
        if (task != null)
            tasks.put(task.getId(), task);
    }

    // сама логика внесения задач
    // методы не должны передаваться статически и туда передается именно сам обьект по тз
    @Override
    public void addEpicTask(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
    }

    @Override
    public void removeEpicTask(int epicTaskId) {
        Epic epic = epics.get(epicTaskId); // получаем кокретный эпик, чтобы удалить и все его саб таски
        if (epic != null) {
            ArrayList<Integer> subTaskIds = epic.getSubTasks(); // получаем айди саб тасков эпика
            // проходимся циклом и удаляем каждый саб таск из хеш мапа саб тасков эпика
            for (Integer subTaskId : subTaskIds) {
                subTasks.remove(subTaskId);
            }
            epics.remove(epicTaskId);
        }
    }

    @Override
    public void removeAllEpics() {
        subTasks.clear();
        epics.clear();
    }

    @Override
    public void removeAllSubTasks() {
        subTasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubTasks().clear(); // получаем айди подзадач и очищаем их
            epic.setTasksStatus(TasksStatus.NEW);
        }
    }

    @Override
    public void addSubTask(SubTask subTask) {
        if (subTask.getId() == subTask.getEpicId()) {
            return; // нельзя добавить подзадачу саму в себя
        }
        subTask.setId(generateId());
        subTasks.put(subTask.getId(), subTask);
        // получаем айди эпика, к которому привязан саб таск
        int epicId = subTask.getEpicId();
        // получаем сам эпик саб таска
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }
        ArrayList<Integer> subTasks = epic.getSubTasks();
        subTasks.add(subTask.getId()); // здесь уже добавим в список саб тасков нужный айди саб таска
        epic.setTasksStatus(changeStatus(epicId));
    }

    @Override
    public void removeSubTask(int subTaskId) {
        SubTask subTask = subTasks.get(subTaskId);
        if (subTask != null) {
            // удаляем и из эпика тоже
            Epic epic = epics.get(subTask.getEpicId());
            if (epic != null) {
                epic.getSubTasks().remove(Integer.valueOf(subTaskId));
                epic.setTasksStatus(changeStatus(subTask.getEpicId()));
            }
            subTasks.remove(subTaskId);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        epic.setTasksStatus(changeStatus(epic.getId())); // присваиваем статус эпику
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        subTasks.put(subTask.getId(), subTask);
        // исправлено: пересчитываем статус эпика
        Epic epic = epics.get(subTask.getEpicId());
        if (epic != null) {
            epic.setTasksStatus(changeStatus(subTask.getEpicId()));
        }
    }

    // логика добавления в историю будет в определенных геттерах
    @Override
    public Task getTask(int taskId) {
        Task task = tasks.get(taskId);
        if (task != null)
            historyManager.addToHistory(task);
        return task;
    }

    @Override
    public SubTask getSubTask(int subTaskId) {
        SubTask subTask = subTasks.get(subTaskId);
        if (subTask != null)
            historyManager.addToHistory(subTask);
        return subTask;
    }

    @Override
    public Epic getEpicTask(int epicTaskId) {
        Epic epic = epics.get(epicTaskId);
        if (epic != null)
            historyManager.addToHistory(epic); // добавляем эпик в историю просмотров
        return epic; // возвращаем обьект эпика по его id
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }





    // по сути не должно касаться пользователя напрямую, если сделать метод приватным
    // статус эпика зависит от всех подзадач сразу
    private TasksStatus changeStatus(int epicTaskId) {
        int newCount = 0;
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
                case TasksStatus.NEW -> newCount++;
                case TasksStatus.IN_PROGRESS -> {

                }
                case TasksStatus.DONE -> doneCount++;
            }
        }
        if (subTasksIds.size() == newCount) {
            return TasksStatus.NEW;
        } else if (subTasksIds.size() == doneCount) {
            return TasksStatus.DONE;
        } else return TasksStatus.IN_PROGRESS;
    }

    private int generateId() {
        return id++;
    }
}
