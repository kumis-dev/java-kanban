package taskmanager.http.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.Managers;
import taskmanager.TaskManager;
import taskmanager.exceptions.NotFoundException;
import taskmanager.taskservice.Epic;
import taskmanager.taskservice.SubTask;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {
    private TaskManager taskManager;

    public EpicHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        // http://localhost:8080/epics - итоговый адрес
        Gson gson = Managers.getDefaultGson();
        String[] parts = httpExchange.getRequestURI().getPath().split("/");

        try {
            switch (httpExchange.getRequestMethod()) {
                case "GET":
                    try {
                        if (parts.length == 2) {
                            String epics = gson.toJson(taskManager.getAllEpics());
                            sendText(httpExchange, epics);
                        } else if (parts.length == 3) {
                            int id = parseIdOr404(parts[2]);
                            Epic epic = taskManager.getEpicTask(id).orElseThrow(NotFoundException::new);
                            sendText(httpExchange, gson.toJson(epic));
                        } else if (parts.length == 4 && parts[3].equals("subtasks")) {
                            int id = parseIdOr404(parts[2]);
                            // если эпик не существует — пробросится NotFound через getEpicTask
                            taskManager.getEpicTask(id).orElseThrow(NotFoundException::new);
                            List<SubTask> subTasks = taskManager.getEpicSubtasks(id);
                            sendText(httpExchange, gson.toJson(subTasks));
                        } else {
                            sendNotFound(httpExchange);
                        }
                    } catch (NotFoundException e) {
                        sendNotFound(httpExchange);
                    }

                    break;
                case "POST":
                    try {
                        // распарсим из json чтобы потом добавить эпик
                        Epic epic = gson.fromJson(new InputStreamReader(httpExchange.getRequestBody(),
                                StandardCharsets.UTF_8), Epic.class);
                        if (epic.getId() == 0)
                            taskManager.addEpicTask(epic);
                        else
                            taskManager.updateEpic(epic);
                        byte[] respBody = gson.toJson(epic).getBytes();
                        // 201 - задача изменилась или добавлена
                        httpExchange.sendResponseHeaders(201, respBody.length);
                        httpExchange.getResponseBody().write(respBody);
                        httpExchange.close();
                    } catch (NotFoundException e) {
                        sendNotFound(httpExchange);
                    }
                    break;
                case "DELETE":
                    try {
                        if (parts.length == 2) {
                            taskManager.removeAllEpics();
                            httpExchange.sendResponseHeaders(204, -1);
                        } else if (parts.length == 3) {
                            int id = parseIdOr404(parts[2]);
                            taskManager.removeEpicTask(id);
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
