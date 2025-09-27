package taskmanager;

import taskmanager.exceptions.NotFoundException;
import taskmanager.exceptions.OverlapException;
import taskmanager.taskservice.Epic;
import taskmanager.taskservice.SubTask;
import taskmanager.taskservice.Task;
import taskmanager.taskservice.TasksStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InMemoryTaskManager implements TaskManager {
    // переменные не должны быть статическими

    // общие задачи
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    // эпики - главные задачи
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    // саб таски - подзадачи
    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();
    // реализуем компоратор
    private final Comparator<Task> comparatorTask = Comparator.nullsLast(Comparator.comparing(Task::getStartTime))
            .thenComparing(Comparator.nullsLast(Comparator.comparing(Task::getEndTime))).thenComparingInt(Task::getId);
    // сюда подставим класс компоратора для сравнения по startTime
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(comparatorTask); // множество приоритетных задач


    // переменная счетчика будет только одна
    private int id = 1; // для всех задач общая
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public void addTask(Task task) {
        // устанавливаем только что сгенерированный айди
        task.setId(generateId());
        // Временно добавляем в prioritizedTasks для проверки
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
        // кладем его и саму задачу в хеш мап по обычным таскам
        if (hasNoOverlap()) {
            tasks.put(task.getId(), task);
        } else {
            if (task.getStartTime() != null) {
                prioritizedTasks.remove(task);
            }
            throw new OverlapException("Задача пересекается по времени");
        }

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
        List<SubTask> subTasks = new ArrayList<>(); // затеняем переменную класса на уровне выше - локальной

        if (epic == null)
            throw new NotFoundException();
        if (epic != null) {
            List<Integer> subTasksIds = epic.getSubTasks();
            if (subTasksIds == null) {
                return new ArrayList<>(); // возвращаем пустой список если subTasksIds null
            }
            // теперь используем flatMap, т к он развернет Optional в поток через метод stream класса Optional
            subTasks = subTasksIds.stream().map(this::getSubTask)
                    .flatMap(Optional::stream).collect(Collectors.toList());
        }

        return subTasks;
    }

    @Override
    public void removeTask(int taskId) {
        Task task = tasks.remove(taskId);
        if (task == null) {
            throw new NotFoundException();
        }
        historyManager.remove(taskId);
        if (task != null && task.getStartTime() != null) {
            prioritizedTasks.remove(task);
        }
    }

    @Override
    public void removeAllTasks() {
        prioritizedTasks.removeIf(task -> task.getClass() == Task.class);
        tasks.clear();
    }

    @Override
    public void updateTask(Task task) throws NotFoundException{
        Task oldTask = tasks.get(task.getId());
        if (oldTask != null && oldTask.getStartTime() != null)
            prioritizedTasks.remove(oldTask); // снимаем старую задачу
        // Добавляем новую для проверки
        if (task.getStartTime() != null)
            prioritizedTasks.add(task);

        if (hasNoOverlap()) {
            tasks.put(task.getId(), task);
        } else {
            if (task.getStartTime() != null)
                prioritizedTasks.remove(task);
            throw new OverlapException("Задача пересекается по времени");
        }
    }

    // сама логика внесения задач
    // методы не должны передаваться статически и туда передается именно сам обьект по тз
    @Override
    public void addEpicTask(Epic epic) {
        epic.setId(generateId());
        if (hasNoOverlap()) {
            epics.put(epic.getId(), epic);
        } else {
            throw new OverlapException("Задача пересекается по времени");
        }
    }

    @Override
    public void removeEpicTask(int epicTaskId) {
        Epic epic = epics.get(epicTaskId); // получаем кокретный эпик, чтобы удалить и все его саб таски
        if (epic == null)
            throw new NotFoundException();
        if (epic != null) {
            List<Integer> subTaskIds = epic.getSubTasks(); // получаем айди саб тасков эпика
            // проходимся циклом и удаляем каждый саб таск из хеш мапа саб тасков эпика
            for (Integer subTaskId : subTaskIds) {
                SubTask subTaskToRemove = subTasks.get(subTaskId); // получаем subTask
                if (subTaskToRemove != null && subTaskToRemove.getStartTime() != null) {
                    prioritizedTasks.remove(subTaskToRemove); // удаляем из prioritized
                }
                subTasks.remove(subTaskId);
            }
            epics.remove(epicTaskId);
        }
        historyManager.remove(epicTaskId);
    }

    @Override
    public void removeAllEpics() {
        prioritizedTasks.removeIf(task -> task instanceof SubTask);
        subTasks.clear();
        epics.clear();
    }

    @Override
    public void removeAllSubTasks() {
        subTasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubTasks().clear(); // получаем айди подзадач и очищаем их
            epic.setTasksStatus(TasksStatus.NEW);
            updateEpicTime(epic);
        }
    }

    @Override
    public void addSubTask(SubTask subTask) {
        if (subTask.getId() == subTask.getEpicId()) {
            return; // нельзя добавить подзадачу саму в себя
        }
        subTask.setId(generateId());
        // Временно добавляем в prioritizedTasks для проверки
        if (subTask.getStartTime() != null) {
            prioritizedTasks.add(subTask);
        }
        if (hasNoOverlap()) {
            subTasks.put(subTask.getId(), subTask);
            // получаем айди эпика, к которому привязан саб таск
            int epicId = subTask.getEpicId();
            // получаем сам эпик саб таска
            Epic epic = epics.get(epicId);
            if (epic == null) {
                return;
            }
            List<Integer> subTasks = epic.getSubTasks();
            if (subTasks == null) {
                subTasks = new ArrayList<>();
                // и желательно вернуть обратно в epic
                epic.getSubTasks().addAll(subTasks);
            }
            subTasks.add(subTask.getId()); // здесь уже добавим в список саб тасков нужный айди саб таска
            epic.setTasksStatus(changeStatus(epicId));
            updateEpicTime(epic);
        } else {
            // Если есть пересечения - убираем из prioritizedTasks
            if (subTask.getStartTime() != null) {
                prioritizedTasks.remove(subTask);
            }
            throw new OverlapException("Задача пересекается по времени");
        }
    }

    @Override
    public void removeSubTask(int subTaskId) {
        SubTask subTask = subTasks.get(subTaskId);
        if (subTask == null)
            throw new NotFoundException();
        if (subTask != null) {
            // удаляем и из эпика тоже
            Epic epic = epics.get(subTask.getEpicId());
            if (epic != null) {
                epic.getSubTasks().remove(Integer.valueOf(subTaskId));
                epic.setTasksStatus(changeStatus(subTask.getEpicId()));
            }
            subTasks.remove(subTaskId);
            updateEpicTime(epic);
            if (subTask.getStartTime() != null) {
                prioritizedTasks.remove(subTask);
            }
        }
        historyManager.remove(subTaskId);
    }

    @Override
    public void updateEpic(Epic epic) {
        if (hasNoOverlap()) {
            epics.put(epic.getId(), epic);
            epic.setTasksStatus(changeStatus(epic.getId())); // присваиваем статус эпику
        } else {
            throw new OverlapException("Задача пересекается по времени");
        }
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        // УДАЛЯЕМ СТАРУЮ ВЕРСИЮ subTask из prioritizedTasks!
        SubTask oldSubTask = subTasks.get(subTask.getId());
        if (oldSubTask != null && oldSubTask.getStartTime() != null) {
            prioritizedTasks.remove(oldSubTask);
        }
        // ДОБАВЛЯЕМ НОВУЮ для проверки
        if (subTask.getStartTime() != null) {
            prioritizedTasks.add(subTask);
        }
        if (hasNoOverlap()) {
            subTasks.put(subTask.getId(), subTask);
            // исправлено: пересчитываем статус эпика
            Epic epic = epics.get(subTask.getEpicId());
            if (epic != null) {
                epic.setTasksStatus(changeStatus(subTask.getEpicId()));
            }
            updateEpicTime(epic);
        } else {
            if (subTask.getStartTime() != null) {
                prioritizedTasks.remove(subTask);
            }
            throw new OverlapException("Задача пересекается по времени");
        }
    }

    // логика добавления в историю будет в определенных геттерах
    @Override
    public Optional<Task> getTask(int taskId) {
        Task task = tasks.get(taskId);
        if (task == null)
            throw new NotFoundException();
        if (task != null)
            historyManager.add(task);
        // возвращем копии тасков, чтобы таск менеджер не изменил свои задачи вместе с оригинальными
        return Optional.ofNullable(new Task(
                task.getNameTask(),
                task.getDescription(),
                task.getId(),
                task.getTasksStatus(),
                task.getDuration(),
                task.getStartTime()
        ));
    }

    @Override
    public Optional<SubTask> getSubTask(int subTaskId) {
        SubTask subTask = subTasks.get(subTaskId);
        if (subTask == null)
            throw new NotFoundException();
        if (subTask != null)
            historyManager.add(subTask);
        // возвращем копии саб тасков, чтобы таск менеджер не изменил свои задачи вместе с оригинальными
        return Optional.ofNullable(new SubTask(
                subTask.getNameTask(),
                subTask.getDescription(),
                subTask.getId(),
                subTask.getTasksStatus(),
                subTask.getDuration(),
                subTask.getStartTime(),
                subTask.getEpicId()
        ));
    }

    @Override
    public Optional<Epic> getEpicTask(int epicTaskId) {
        Epic epic = epics.get(epicTaskId);
        if (epic == null) {
            throw new NotFoundException();
        }
        if (epic != null)
            historyManager.add(epic); // добавляем эпик в историю просмотров
        // возвращем копии эпиков, чтобы таск менеджер не изменил свои задачи вместе с оригинальными
        return Optional.ofNullable(new Epic(
                epic.getNameTask(),
                epic.getDescription(),
                epic.getId(),
                epic.getTasksStatus(),
                epic.getDuration(),
                epic.getStartTime(),
                epic.getEndTime()
        )); // возвращаем копию обьекта эпика по его id
    }

    public List<Task> getPrioritizedTasks() { // добавить реализацию геттера задач по приоритетности
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    protected void setIdCounter(int newId) {
        this.id = newId;
    }

    private boolean isOverlap(Task task1, Task task2) {
        LocalDateTime startTime1 = task1.getStartTime();
        LocalDateTime endTime1 = task1.getEndTime();

        LocalDateTime startTime2 = task2.getStartTime();
        LocalDateTime endTime2 = task2.getEndTime();

        return (startTime2.isBefore(endTime1) && startTime1.isBefore(endTime2));
    }

    private boolean hasNoOverlap() {
        List<Task> taskList = getPrioritizedTasks();
        // поток примитивных данных IntStream в радиусе от начала до конца приоритизированных тасков
        // проверяет соотвествуют ли все таски внутри (allMatch), предикату внутри allMatch
        return IntStream.range(0, taskList.size() - 1).allMatch(
                i -> !isOverlap(taskList.get(i), taskList.get(i + 1))
        );
        // то есть если во всех задачах нету пересечений, то true
    }

    private void updateEpicTime(Epic epic) {
        if (epic == null) return;
        List<Integer> subTasks = epic.getSubTasks();

        // для того чтобы получить хеш мапу саб тасков, а не лист айдишников используем this.subTasks::get
        // проходимся по айдишникам саб тасков , получаем из хеш мапы сами саб таски
        // дальше получаем их продолжительность
        // затем складываем из начальной продолжительности в продолжительность саб таска, тем самым уже увеличиваем
        // продолжительность эпика
        Duration duration = subTasks.stream().map(this.subTasks::get)
                .map(SubTask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);
        epic.setDuration(duration);

        LocalDateTime startTime = subTasks.stream().map(this.subTasks::get)
                .map(SubTask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo).orElse(null);
        epic.setStartTime(startTime);

        LocalDateTime endTime = subTasks.stream().map(this.subTasks::get)
                .map(SubTask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo).orElse(null);
        epic.setEndTime(endTime);
    }

    // не должно касаться пользователя напрямую, если сделать метод приватным
    // статус эпика зависит от всех подзадач сразу
    private TasksStatus changeStatus(int epicTaskId) {
        int newCount = 0;
        int doneCount = 0;

        Epic epic = epics.get(epicTaskId); // получаем обьект эпика
        if (epic == null) {
            return TasksStatus.NEW;
        }
        List<Integer> subTasksIds = epic.getSubTasks();
        if (subTasksIds == null || subTasksIds.isEmpty())
            return TasksStatus.NEW;
        // здесь не стал переделывать под стримы, т к ухудшает читаемость
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
