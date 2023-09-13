package ru.yandex.repository;

import lombok.RequiredArgsConstructor;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.utils.IdGenerator;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SubtaskRepositoryImpl implements TaskRepository<Subtask> {

    private final List<Task> taskStorage;
    private final IdGenerator idGenerator;
    private final Predicate<Task> filterByClass = Subtask.class::isInstance;
    private final Function<Task, Subtask> classMapper = Subtask.class::cast;

    @Override
    public List<Subtask> findAll() {
        return taskStorage.stream()
                .filter(filterByClass)
                .map(classMapper)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Optional<Subtask> findById(int id) {
        return taskStorage.stream()
                .filter(filterByClass)
                .map(classMapper)
                .filter(t -> t.getId() == id)
                .findFirst();
    }

    @Override
    public Subtask create(Subtask subtask) {
        subtask.setId(idGenerator.generateId());
        taskStorage.add(subtask);

        return subtask;
    }

    @Override
    public Subtask update(Subtask subtask) {
        var subtaskOptional = findById(subtask.getId());

        if (subtaskOptional.isPresent()) {
            var updatedSubtask = subtaskOptional.get();
            updatedSubtask.setName(subtask.getName());
            updatedSubtask.setDescription(subtask.getDescription());
            updatedSubtask.setStatus(subtask.getStatus());
            updatedSubtask.setSubtasks(subtask.getSubtasks());

            taskStorage.set(subtask.getId() - 1, updatedSubtask);

            return updatedSubtask;
        } else {
            return create(subtask);
        }
    }

    @Override
    public void delete(int id) {
        findById(id).ifPresent(taskStorage::remove);
    }

    @Override
    public void deleteBatch() {
        taskStorage.removeAll(findAll());
    }
}
