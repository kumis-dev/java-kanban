package taskManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InMemoryHistoryManager implements HistoryManager {

    // Создаем узлы списка истории
    private static class Node {
        Node prev;
        Task data; // сама задача вместо элемента
        Node next;
        public Node (Node prev, Task data, Node next) {
            this.prev = prev;
            this.data = data;
            this.next = next;
        }
    }

    // Создаем узлы хранилок и хеш мапу, связывающую айди и саму ноду (задачу)
    private Node head; // поле с указателем на первый элемент истории
    private Node tail; // поле с указателем на последний элемент истории
    private final Map<Integer, Node> historyMap = new HashMap<>();

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void add(Task task) {
        if (task == null) return; // если таска не существует, выходим, добавлять нечего
        // здесь надо реализовать логику 2 шагом, по которой мы защитимся от дублей
        Node node = historyMap.remove(task.getId());
        if (node != null) {
            removeNode(node);
        }
        linkLast(task); // после вызова link last - поле tail указывает на свежесозданный узел
        historyMap.put(task.getId(), tail);
    }

    @Override
    public void remove(int id) {
        Node node = historyMap.remove(id); // удаляем узел мапы, который вернули по айди ноды
        if (node == null) return;
        removeNode(node); // вырезаем его из двусвязного списка
    }

    // linkLast будет добавлять задачу в конец этого списка
    // т е нужен для того чтобы новая задача показывалась на самом вверху
    private void linkLast(Task task) {
        final Node t = tail;
        final Node newNode = new Node(t, task, null);
        tail = newNode;
        if (t == null)
            head = newNode;
        else
            t.next = newNode;
    }

    private List<Task> getTasks() {
        ArrayList<Task> taskHistory = new ArrayList<>();
        // Проходим цепочку от head до tail и собираем задачи в список-результат
        for (Node node = head; node != null; node = node.next) {
            taskHistory.add(node.data); // кладем таску (дату узла) в итоговый массив
        }
        return taskHistory;
    }

    private void removeNode(Node node) {
        if (node == null) return;

        final Node next = node.next; // обновляем ссылки на пред элемент и след элемент от текущей ноды сразу
        final Node prev = node.prev;

        // обновляем ссылки на ноды корректно
        if (prev == null) {
            head = next;
        } else {
            prev.next = next;
            node.prev = null;
        }

        if (next == null) {
            tail = prev;
        } else {
            next.prev = prev;
            node.next = null;
        }

        node.data = null;
    }
}
