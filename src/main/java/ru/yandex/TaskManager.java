package ru.yandex;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.yandex.util.IdGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@Getter
public class TaskManager {

    private final IdGenerator idGenerator;
    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epics;
    private final Map<Integer, Subtask> subtasks;


    public Task getTask(int id) {
        return tasks.get(id);
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    public List<Subtask> getSubtasksByEpicId(int epicId) {
        List<Subtask> result = new ArrayList<>();

        epics.get(epicId).getSubtaskIds().forEach(id -> result.add(subtasks.get(id)));

        return result;
    }

    public Task createTask(Task task) {
        tasks.put(idGenerator.generateId(), task);

        return task;
    }

    public Epic createEpic(Epic epic) {
        epics.put(idGenerator.generateId(), epic);

        return epic;
    }

    public Subtask createSubtask(int epicId, Subtask subtask) {
        int id = idGenerator.generateId();

        subtask.setId(id);
        subtasks.put(id, subtask);
        var epic = epics.get(epicId);

        updateEpicStatus(epic);
        epic.getSubtaskIds().add(id);

        return subtask;
    }

    private void updateEpicStatus(Epic epic) {
        var subtasks = getSubtasksByEpicId(epic.getId());
        var hasAllSubtasksStatusNew = epic.getSubtaskIds().isEmpty() || subtasks.stream().allMatch(s -> s.getStatus().equals(Task.Status.NEW));
        var hasAllSubtasksStatusDone = subtasks.stream().anyMatch(s -> s.getStatus().equals(Task.Status.DONE));

        if (hasAllSubtasksStatusNew) {
            epic.setStatus(Task.Status.NEW);
        } else if (hasAllSubtasksStatusDone) {
            epic.setStatus(Task.Status.DONE);
        } else {
            epic.setStatus(Task.Status.IN_PROGRESS);
        }

    }

    public Task updateTask(Task taskToUpdate) {
        var task = Objects.requireNonNull(tasks.get(taskToUpdate.getId()));

        task.setName(taskToUpdate.getName());
        task.setDescription(taskToUpdate.getDescription());
        task.setStatus(taskToUpdate.getStatus());

        return task;
    }

    public Epic updateEpic(Epic epicToUpdate) {
        var epic = Objects.requireNonNull(epics.get(epicToUpdate.getId()));

        epic.setName(epicToUpdate.getName());
        epic.setDescription(epicToUpdate.getDescription());

        return epic;
    }

    public Subtask updateSubtask(Subtask subtaskToUpdate) {
        var subtask = subtasks.get(subtaskToUpdate.getId());

        subtask.setName(subtaskToUpdate.getName());
        subtask.setDescription(subtaskToUpdate.getDescription());
        subtask.setStatus(subtaskToUpdate.getStatus());

        var epic = epics.get(subtaskToUpdate.getEpicId());
        updateEpicStatus(epic);

        return subtask;
    }

    public void deleteTask(int id) {
        var epic = epics.get(subtasks.get(id).getEpicId());
        updateEpicStatus(epic);

        tasks.remove(id);
    }

    public void deleteEpic(int id) {
        epics.remove(id);

        for (Subtask subtask : subtasks.values()) {
            if (subtask.getEpicId() == id) {
                subtasks.remove(id);
            }
        }
    }

    public void deleteSubtask(int id) {
        var epicId = subtasks.get(id).getEpicId();
        epics.get(epicId).getSubtaskIds().remove(((Integer) id));
        subtasks.remove(id);
    }

    public void deleteTasks() {
        tasks.clear();
    }

    // WTF ?? - "@Нужно удалять только подзадачи этого эпика, а не все подзадачи"
    // Зачем мне удалять только подзадачи этого эпика, если я удаляю ВСЕ эпики,
    // следовательно и все ПОДЗАДАЧИ, входящие в ЭПИКИ подзадачи должны быть  подвергнуты удалению
    public void deleteEpics() {
        subtasks.clear();
        epics.clear();
    }

    public void deleteSubtasks() {
        subtasks.values().forEach(s -> updateEpicStatus(epics.get(s.getEpicId())));
        subtasks.clear();
    }

}
