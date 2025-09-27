package taskmanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import taskmanager.adapters.TaskDurationAdapter;
import taskmanager.adapters.TaskTimeAdapter;

import java.time.Duration;
import java.time.LocalDateTime;

public class Managers {
    private Managers() {

    } // запрещаем создание объектов

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static Gson getDefaultGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new TaskTimeAdapter())
                .registerTypeAdapter(Duration.class, new TaskDurationAdapter())
                .create();
    }
}
