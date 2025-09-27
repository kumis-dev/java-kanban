package taskmanager.http.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.Managers;
import taskmanager.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    private TaskManager taskManager;
    // надо сделать с приоритетными задачами теперь


    public HistoryHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        // http://localhost:8080/history - итоговый адрес
        Gson gson = Managers.getDefaultGson();
        String[] parts = httpExchange.getRequestURI().getPath().split("/");

        // парсим в json чтобы потом получить историю
        try {
            switch (httpExchange.getRequestMethod()) {
                case "GET":
                    String history = gson.toJson(taskManager.getHistory());
                    sendText(httpExchange, history);
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
