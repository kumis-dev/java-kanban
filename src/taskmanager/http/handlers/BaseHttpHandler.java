package taskmanager.http.handlers;

import java.io.IOException;
import com.sun.net.httpserver.HttpExchange;
import taskmanager.exceptions.NotFoundException;

import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {
    protected void sendText(HttpExchange httpExchange, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        httpExchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        httpExchange.sendResponseHeaders(200, resp.length); // сначала отправляем стартовую строку
        httpExchange.getResponseBody().write(resp); // затем записываем в тело новые данные
        httpExchange.close();
    }

    protected void sendNotFound(HttpExchange httpExchange) throws IOException {
        String error = "Task not found!";
        httpExchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        httpExchange.sendResponseHeaders(404, error.getBytes(StandardCharsets.UTF_8).length);
        httpExchange.getResponseBody().write(error.getBytes(StandardCharsets.UTF_8));
        httpExchange.close();
    }

    protected void sendHasOverlaps(HttpExchange httpExchange) throws IOException {
        String error = "Task has overlap!";
        httpExchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        httpExchange.sendResponseHeaders(406, error.getBytes(StandardCharsets.UTF_8).length);
        httpExchange.getResponseBody().write(error.getBytes(StandardCharsets.UTF_8));
        httpExchange.close();
    }

    protected void sendInternalError(HttpExchange httpExchange) throws IOException {
        String error = "Internal server error";
        httpExchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        httpExchange.sendResponseHeaders(500, error.getBytes(StandardCharsets.UTF_8).length);
        httpExchange.getResponseBody().write(error.getBytes(StandardCharsets.UTF_8));
        httpExchange.close();
    }

    protected int parseIdOr404(String id) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new NotFoundException();
        }
    }
}
