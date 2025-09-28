package taskmanager.http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.Managers;
import taskmanager.TaskManager;
import taskmanager.exceptions.NotFoundException;
import taskmanager.exceptions.OverlapException;
import taskmanager.taskservice.Task;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    private TaskManager taskManager;

    public TaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        // http://localhost:8080/tasks - итоговый адрес
        Gson gson = Managers.getDefaultGson();
        String[] parts = httpExchange.getRequestURI().getPath().split("/");

        try {
            switch (httpExchange.getRequestMethod()) {
                case "GET":
                    try {
                        if (parts.length == 2) {
                            String tasks = gson.toJson(taskManager.getAllTasks());
                            sendText(httpExchange, tasks);
                        } else if (parts.length == 3) {
                            int id = parseIdOr404(parts[2]);
                            Task task = taskManager.getTask(id).orElseThrow(NotFoundException::new);
                            sendText(httpExchange, gson.toJson(task));
                        } else {
                            sendNotFound(httpExchange);
                        }
                    } catch (NotFoundException e) {
                        sendNotFound(httpExchange);
                    }

                    break;
                case "POST":
                    // в случае пересечения отправляем sendHasOverlaps
                    try {
                        // распарсим из json чтобы потом добавить задачу
                        Task task = gson.fromJson(new InputStreamReader(httpExchange.getRequestBody(),
                                StandardCharsets.UTF_8), Task.class);
                        if (task.getId() == 0) {
                            taskManager.addTask(task);
                        } else {
                            taskManager.updateTask(task);
                        }
                        byte[] respBody = gson.toJson(task).getBytes(StandardCharsets.UTF_8);
                        // 201 - задача изменилась или добавлена
                        httpExchange.sendResponseHeaders(201, respBody.length);
                        httpExchange.getResponseBody().write(respBody);
                        httpExchange.close();
                    } catch (OverlapException e) {
                        sendHasOverlaps(httpExchange);
                    } catch (NotFoundException e) {
                        sendNotFound(httpExchange);
                    }
                    break;
                case "DELETE":
                    try {
                        if (parts.length == 2) {
                            taskManager.removeAllTasks();
                            httpExchange.sendResponseHeaders(204, -1);
                        } else if (parts.length == 3) {
                            int id = parseIdOr404(parts[2]);
                            taskManager.removeTask(id);
                            // задача удалена успешно - код 204 (No Content)
                            httpExchange.sendResponseHeaders(204, -1);
                        }
                        httpExchange.close();
                    } catch (NotFoundException e) {
                        sendNotFound(httpExchange);
                    }
                    break;
                default:
                    sendNotFound(httpExchange);
            }
        } catch (JsonSyntaxException e) {
            sendInternalError(httpExchange);
        } catch (Exception e) {
            sendInternalError(httpExchange);
            e.printStackTrace();
        }
    }
}
