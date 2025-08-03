package taskManager;

public class Managers {
    private Managers() {} // запрещаем создание объектов

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

   public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
   }
}
