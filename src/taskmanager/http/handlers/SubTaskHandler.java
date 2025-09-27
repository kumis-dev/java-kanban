package taskmanager.http.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.Managers;
import taskmanager.TaskManager;
import taskmanager.exceptions.NotFoundException;
import taskmanager.exceptions.OverlapException;
import taskmanager.taskservice.SubTask;
import taskmanager.taskservice.Task;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SubTaskHandler extends BaseHttpHandler implements HttpHandler {
    private TaskManager taskManager;

    public SubTaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        // http://localhost:8080/subtasks - итоговый адрес
        Gson gson = Managers.getDefaultGson();
        String[] parts = httpExchange.getRequestURI().getPath().split("/");

        try {
            switch (httpExchange.getRequestMethod()) {
                case "GET":
                    try {
                        if (parts.length == 2) {
                            String subTasks = gson.toJson(taskManager.getAllSubTasks());
                            sendText(httpExchange, subTasks);
                        } else if (parts.length == 3) {
                            int id = parseIdOr404(parts[2]);
                            SubTask subTask = taskManager.getSubTask(id).orElseThrow(NotFoundException::new);
                            sendText(httpExchange, gson.toJson(subTask));
                        } else {
                            sendNotFound(httpExchange);
                        }
                    } catch (NotFoundException e) {
                        sendNotFound(httpExchange);
                    }

                    break;
                case "POST":
                    try {
                        SubTask subTask = gson.fromJson(new InputStreamReader(httpExchange.getRequestBody(),
                                StandardCharsets.UTF_8), SubTask.class);
                        if (subTask.getId() == 0) {
                            taskManager.addSubTask(subTask);
                        } else {
                            taskManager.updateSubTask(subTask);
                        }
                        byte[] respBody = gson.toJson(subTask).getBytes();
                        // 201 - задача изменилась или добавлена
                        httpExchange.sendResponseHeaders(201, respBody.length);
                        httpExchange.getResponseBody().write(respBody);
                        httpExchange.close();
                    } catch (OverlapException e) {
                        sendHasOverlaps(httpExchange);
                    } catch (NotFoundException e) { // пригодится при обновлении задачи
                        sendNotFound(httpExchange);
                    }
                    break;
                case "DELETE":
                    try {
                        if (parts.length == 2) {
                            taskManager.removeAllSubTasks();
                            httpExchange.sendResponseHeaders(204, -1);
                        } else if (parts.length == 3) {
                            int id = parseIdOr404(parts[2]);
                            taskManager.removeSubTask(id);
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
