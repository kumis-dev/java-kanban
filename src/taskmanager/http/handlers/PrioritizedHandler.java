package taskmanager.http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.Managers;
import taskmanager.TaskManager;
import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    private TaskManager taskManager;
    // надо сделать с приоритетными задачами теперь

    public PrioritizedHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        // http://localhost:8080/prioritized - итоговый адрес
        Gson gson = Managers.getDefaultGson();
        String[] parts = httpExchange.getRequestURI().getPath().split("/");

        // парсим в json чтобы потом получить приоритетную задачу
        try {
            switch (httpExchange.getRequestMethod()) {
                case "GET":
                    String prioritizedTasks = gson.toJson(taskManager.getPrioritizedTasks());
                    sendText(httpExchange, prioritizedTasks);
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
