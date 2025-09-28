import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import taskmanager.InMemoryTaskManager;
import taskmanager.Managers;
import taskmanager.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;

import taskmanager.http.*;
import com.google.gson.Gson;
import taskmanager.taskservice.Epic;
import taskmanager.taskservice.SubTask;
import taskmanager.taskservice.Task;
import taskmanager.taskservice.TasksStatus;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest {
    // создаём экземпляр InMemoryTaskManager
    TaskManager manager = new InMemoryTaskManager();
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = Managers.getDefaultGson();

    public HttpTaskManagerTasksTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.removeAllTasks();
        manager.removeAllEpics();
        manager.removeAllSubTasks();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void addTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test", "Testing task", 5,
                TasksStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test", tasksFromManager.get(0).getNameTask(), "Некорректное имя задачи");
    }

    @Test
    public void updateTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test", "Testing task", 0,
                TasksStatus.NEW, Duration.ofMinutes(90), LocalDateTime.now());
        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        // вызываем рест, отвечающий за создание задач
        client.send(request, HttpResponse.BodyHandlers.ofString());
        // берем реальный айди задачи из менеджера задач task.getId();
        Task oldTask = manager.getAllTasks().get(0);
        oldTask.setTasksStatus(TasksStatus.IN_PROGRESS);
        oldTask.setDuration(Duration.ofMinutes(120));

        String updateGson = gson.toJson(oldTask);
        HttpRequest updateRequest = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updateGson))
                .build();

        // после снова получаем response
        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, updateResponse.statusCode());

        Task after = manager.getAllTasks().get(0);
        assertEquals(TasksStatus.IN_PROGRESS, after.getTasksStatus());
        assertEquals(Duration.ofMinutes(120), after.getDuration());
        assertEquals("Test", after.getNameTask());
    }

    @Test
    public void updateEpic() throws IOException, InterruptedException {
        // создаём задачу
        Epic epic = new Epic("Test", "Testing task", 0,
                TasksStatus.NEW, Duration.ofMinutes(120), LocalDateTime.now(), LocalDateTime.now().plusMinutes(250));
        manager.addEpicTask(epic);
        SubTask s1 = new SubTask("s1","d1",0, TasksStatus.NEW, Duration.ofMinutes(10),
                LocalDateTime.now(), epic.getId());
        SubTask s2 = new SubTask("s2","d2",0, TasksStatus.DONE, Duration.ofMinutes(5),
                LocalDateTime.now().plusMinutes(15), epic.getId());
        manager.addSubTask(s1);
        manager.addSubTask(s2);
        // конвертируем её в JSON
        String epicJson = gson.toJson(epic);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        // вызываем рест, отвечающий за создание задач
        client.send(request, HttpResponse.BodyHandlers.ofString());
        // берем реальный айди задачи из менеджера задач task.getId();
        Epic oldEpic = manager.getAllEpics().get(0);
        oldEpic.setTasksStatus(TasksStatus.IN_PROGRESS);
        oldEpic.setDuration(Duration.ofMinutes(700));

        String updateGson = gson.toJson(oldEpic);
        HttpRequest updateRequest = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updateGson))
                .build();

        // после снова получаем response
        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, updateResponse.statusCode());

        Epic after = manager.getAllEpics().get(0);
        assertEquals(TasksStatus.IN_PROGRESS, after.getTasksStatus());
        assertEquals(Duration.ofMinutes(700), after.getDuration());
        assertEquals("Test", after.getNameTask());
    }

    @Test
    public void updateSubTask() throws IOException, InterruptedException {
        // сначала создаем эпик, к которому будет принадлежать подзадача
        Epic epic = new Epic("Parent Epic", "Parent epic for subtask", 0,
                TasksStatus.NEW, Duration.ofMinutes(0), LocalDateTime.now(), LocalDateTime.now());
        manager.addEpicTask(epic);

        // создаём задачу
        SubTask subTask = new SubTask("Test", "Testing task", 0,
                TasksStatus.NEW, Duration.ofMinutes(50), LocalDateTime.now(), epic.getId());
        // конвертируем её в JSON
        String subTaskJson = gson.toJson(subTask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subTaskJson))
                .build();

        // вызываем рест, отвечающий за создание задач
        client.send(request, HttpResponse.BodyHandlers.ofString());
        // берем реальный айди задачи из менеджера задач task.getId();
        SubTask oldSubTask = manager.getAllSubTasks().get(0);
        oldSubTask.setTasksStatus(TasksStatus.IN_PROGRESS);
        oldSubTask.setDuration(Duration.ofMinutes(100));

        String updateGson = gson.toJson(oldSubTask);
        HttpRequest updateRequest = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updateGson))
                .build();

        // после снова получаем response
        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, updateResponse.statusCode());

        SubTask after = manager.getAllSubTasks().get(0);
        assertEquals(TasksStatus.IN_PROGRESS, after.getTasksStatus());
        assertEquals(Duration.ofMinutes(100), after.getDuration());
        assertEquals("Test", after.getNameTask());
    }

    @Test
    public void removeTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test", "Testing task", 0,
                TasksStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");

        HttpRequest postRequest = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        client.send(postRequest, HttpResponse.BodyHandlers.ofString());

        int taskId = manager.getAllTasks().get(0).getId();

        URI deleteURI = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(deleteURI)
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(204, deleteResponse.statusCode());


        List<Task> tasksFromManager = manager.getAllTasks();

        assertEquals(0, tasksFromManager.size(), "Задачи должны быть пустыми");
    }

    @Test
    public void removeEpic() throws IOException, InterruptedException {
        // создаём эпик
        Epic epic = new Epic("Test", "Testing epic", 1,
                TasksStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now(), LocalDateTime.now().plusMinutes(50));
        // конвертируем её в JSON
        String epicJson = gson.toJson(epic);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");

        HttpRequest postRequest = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        client.send(postRequest, HttpResponse.BodyHandlers.ofString());

        int epicId = manager.getAllEpics().get(0).getId();

        URI deleteURI = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(deleteURI)
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(204, deleteResponse.statusCode());


        List<Epic> epicsFromManager = manager.getAllEpics();

        assertEquals(0, epicsFromManager.size(), "Задачи должны быть пустыми");
    }

    @Test
    public void removeSubTask() throws IOException, InterruptedException {
        // Сначала создаем эпик
        Epic epic = new Epic("Epic", "epic", 0,
                TasksStatus.NEW, Duration.ofMinutes(0), LocalDateTime.now(), LocalDateTime.now());
        manager.addEpicTask(epic);

        // создаём задачу
        SubTask subTask = new SubTask("Test", "Testing subtask", 2,
                TasksStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now(), epic.getId());
        // конвертируем её в JSON
        String subTaskJson = gson.toJson(subTask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");

        HttpRequest postRequest = HttpRequest.newBuilder().uri(url).POST(
                HttpRequest.BodyPublishers.ofString(subTaskJson)).build();

        client.send(postRequest, HttpResponse.BodyHandlers.ofString());

        int subTaskId = manager.getAllSubTasks().get(0).getId();

        URI deleteURI = URI.create("http://localhost:8080/subtasks/" + subTaskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(deleteURI)
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(204, deleteResponse.statusCode());


        List<SubTask> subTasksFromManager = manager.getAllSubTasks();

        assertEquals(0, subTasksFromManager.size(), "Задачи должны быть пустыми");
    }

    @Test
    public void addEpic() throws IOException, InterruptedException {
        // создаём эпик
        Epic epic = new Epic("Test 2", "Testing task 2", 6,
                TasksStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now(), LocalDateTime.now().plusMinutes(20));
        // конвертируем его в JSON
        String epicJson = gson.toJson(epic);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        // вызываем рест, отвечающий за создание эпиков
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создался один эпик с корректным именем
        List<Epic> tasksFromManager = manager.getAllEpics();

        assertNotNull(tasksFromManager, "Эпики не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Test 2", tasksFromManager.get(0).getNameTask(), "Некорректное имя эпика");
    }

    @Test
    public void addSubTask() throws IOException, InterruptedException {
        // создаём подзадачу
        SubTask subTask = new SubTask("Test 3", "Testing task 3", 7,
                TasksStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now(), 6);
        // конвертируем её в JSON
        String subTaskJson = gson.toJson(subTask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subTaskJson))
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна подзадача с корректным именем
        List<SubTask> tasksFromManager = manager.getAllSubTasks();

        assertNotNull(tasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Test 3", tasksFromManager.get(0).getNameTask(), "Некорректное имя подзадачи");
    }

    @Test
    public void shouldReceive200Code() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        List<String> paths = List.of("/tasks", "/epics", "/subtasks", "/history", "/prioritized");

        for (String path : paths) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080" + path))
                    .GET()
                    .build();
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), "по пути " + path + " ожидался код 200");
            assertEquals("[]", response.body(), " ожидался пустой список");
        }
    }

    @Test
    public void shouldReceive404CodeWithDeleteTask() throws IOException, InterruptedException {
        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        // указываем несуществующий айди в конце
        URI url = URI.create("http://localhost:8080/tasks/12345");

        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "ожидался код 404");
    }

    @Test
    public void shouldReceive406Code() throws IOException, InterruptedException {
        // при пересечении 2 задач по времени должен выдавать 406 код
        Task task1 = new Task("Test", "Testing task1", 0,
                TasksStatus.NEW, Duration.ofMinutes(60), LocalDateTime.now());
        // конвертируем её в JSON
        String task1Json = gson.toJson(task1);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");

        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(task1Json))
                .build();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Task task2 = new Task("Test", "Testing task1", 0,
                TasksStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now().plusMinutes(30));
        String task2Json = gson.toJson(task2);
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(task2Json))
                .build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(), "ожидался код 406");
    }

    @Test
    public void shouldReceive500Code() throws IOException, InterruptedException {
        String badJson = "{ invalid json here: }";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(badJson))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(500, response.statusCode());
    }

    @Test
    public void unknownPathShouldReturn404() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/unknown"))
                .GET()
                .build();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void historyEmptyShouldReturn200AndEmptyArray() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET().build();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    public void should404InGetWhenIdIsBad() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        // допустим задача эта уже лежит, с неправильным айди он все равно ее не найде
        HttpRequest getRequest = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/tasks/abc"))
                .GET().build();
        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void shouldGetEpicsSubtasksSuccessfully() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        Epic createEpic = new Epic("e1", "d1", 0,
                TasksStatus.NEW, Duration.ZERO, LocalDateTime.now(), LocalDateTime.now());
        HttpRequest epicRequest = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(createEpic))).build();
        HttpResponse<String> epicResponse = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());

        Epic epic = gson.fromJson(epicResponse.body(), Epic.class);
        int epicId = epic.getId();

        SubTask s1 = new SubTask("s1","d1",0, TasksStatus.NEW, Duration.ofMinutes(10),
                LocalDateTime.now().plusMinutes(10), epicId);
        SubTask s2 = new SubTask("s2","d2",0, TasksStatus.DONE, Duration.ofMinutes(20),
                LocalDateTime.now().plusMinutes(40), epicId);

        HttpRequest createS1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(s1)))
                .build();
        HttpRequest createS2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(s2)))
                .build();

        client.send(createS1, HttpResponse.BodyHandlers.ofString());
        client.send(createS2, HttpResponse.BodyHandlers.ofString());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epicId + "/subtasks"))
                .GET().build();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }
}
