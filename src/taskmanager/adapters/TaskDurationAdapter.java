package taskmanager.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TaskDurationAdapter extends TypeAdapter<Duration> {
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    // базовая точка отсчета времени
    private final LocalTime baseTime = LocalTime.of(0, 0, 0);

    @Override
    public void write(final JsonWriter jsonWriter, final Duration duration) throws IOException {
        // прибавляем текущий duration к нулевой точке отсчета, чтобы получить форматируемую дату и время
        String fDuration = baseTime.plus(duration).format(timeFormatter);
        jsonWriter.value(fDuration);
    }

    @Override
    public Duration read(final JsonReader jsonReader) throws IOException {
        // получаем отформатированную строку с текущим временем, учитывая промежуток выполнения самой задачи
        LocalTime parsedTime = LocalTime.parse(jsonReader.nextString(), timeFormatter);
        // возвращаем промежуток между нулевой точкой и текущим временем, чтобы получить duration нужной задачи
        return Duration.between(baseTime, parsedTime);
    }
}
