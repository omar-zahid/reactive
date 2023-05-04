package com.magnuscode.task;

import java.util.List;

import org.hibernate.ObjectNotFoundException;

import com.magnuscode.user.UserService;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.security.UnauthorizedException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TaskService {

    private final UserService userService;

    public TaskService(UserService userService) {
        this.userService = userService;
    }

    public Uni<Task> findById(long id) {
        return userService.getCurrentUser()
                .chain(user -> Task.<Task>findById(id)
                        .onItem().ifNull().failWith(() -> new ObjectNotFoundException(id, "Task"))
                        .onItem().invoke(task -> {
                            if (!user.equals(task.user)) {
                                throw new UnauthorizedException("You are not allowed to update this task.");
                            }
                        }));
    }

    public Uni<List<Task>> listForUsers() {
        return userService.getCurrentUser()
                .chain(user -> Task.find("user", user).list());
    }

    @WithTransaction
    public Uni<Task> create(Task task) {
        return userService.getCurrentUser()
                .chain(user -> {
                    task.user = user;
                    return task.persistAndFlush();
                });
    }

    @WithTransaction
    public Uni<Task> update(Task task) {
        return findById(task.id)
                .chain(t -> Task.getSession())
                .chain(s -> s.merge(task));
    }

    @WithTransaction
    public Uni<Void> delete(long id) {
        return findById(id)
                .chain(Task::delete);
    }
}
