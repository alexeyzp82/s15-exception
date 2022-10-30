package com.softserve.itacademy.controller;

import com.softserve.itacademy.exception.EntityNotFoundException;
import com.softserve.itacademy.exception.NullEntityReferenceException;
import com.softserve.itacademy.model.Task;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.service.TaskService;
import com.softserve.itacademy.service.ToDoService;
import com.softserve.itacademy.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/todos")
public class ToDoController {

    private final ToDoService todoService;
    private final TaskService taskService;
    private final UserService userService;

    public ToDoController(ToDoService todoService, TaskService taskService, UserService userService) {
        this.todoService = todoService;
        this.taskService = taskService;
        this.userService = userService;
    }

    @GetMapping("/create/users/{owner_id}")
    public String create(@PathVariable("owner_id") long ownerId, Model model) {
        model.addAttribute("todo", new ToDo());
        model.addAttribute("ownerId", ownerId);
        return "create-todo";
    }

    @PostMapping("/create/users/{owner_id}")
    public String create(@PathVariable("owner_id") long ownerId, @Validated @ModelAttribute("todo") ToDo todo, BindingResult result) {
        if (todo.getTitle().isEmpty()) {
            throw new NullEntityReferenceException("This entity is Empty");
        }
        if (result.hasErrors()) {
            throw new NullEntityReferenceException("You have entered something incorrectly");
        }

        todo.setCreatedAt(LocalDateTime.now());
        todo.setOwner(userService.readById(ownerId));
        todoService.create(todo);
        return "redirect:/todos/all/users/" + ownerId;
    }

    @GetMapping("/{id}/tasks")
    public String read(@PathVariable long id, Model model) {
        if (todoService.existById(id)) {
            ToDo todo = todoService.readById(id);
            List<Task> tasks = taskService.getByTodoId(id);
            List<User> users = userService.getAll().stream()
                    .filter(user -> user.getId() != todo.getOwner().getId()).collect(Collectors.toList());
            model.addAttribute("todo", todo);
            model.addAttribute("tasks", tasks);
            model.addAttribute("users", users);
            return "todo-tasks";
        } else {
            throw new EntityNotFoundException("This entity has not been found");
        }


    }

    @GetMapping("/{todo_id}/update/users/{owner_id}")
    public String update(@PathVariable("todo_id") long todoId, @PathVariable("owner_id") long ownerId, Model model) {
        ToDo todo = todoService.readById(todoId);
        model.addAttribute("todo", todo);
        return "update-todo";
    }

    @PostMapping("/{todo_id}/update/users/{owner_id}")
    public String update(@PathVariable("todo_id") long todoId, @PathVariable("owner_id") long ownerId,
                         @Validated @ModelAttribute("todo") ToDo todo, BindingResult result) {
        if (todo.getTitle().isEmpty()) {
            throw new NullEntityReferenceException("This entity is Empty");
        }
        if (result.hasErrors()) {
            todo.setOwner(userService.readById(ownerId));
            return "update-todo";
        }
        ToDo oldTodo = todoService.readById(todoId);
        todo.setOwner(oldTodo.getOwner());
        todo.setCollaborators(oldTodo.getCollaborators());
        todoService.update(todo);
        return "redirect:/todos/all/users/" + ownerId;
    }

    @GetMapping("/{todo_id}/delete/users/{owner_id}")
    public String delete(@PathVariable("todo_id") long todoId, @PathVariable("owner_id") long ownerId) {

        if (todoService.existById(todoId)) {
            todoService.delete(todoId);
            return "redirect:/todos/all/users/" + ownerId;
        }
        else {
            throw new EntityNotFoundException("This entity has not been found");
        }

    }

    @GetMapping("/all/users/{user_id}")
    public String getAll(@PathVariable("user_id") long userId, Model model) {
        if (userService.existById(userId)) {
            List<ToDo> todos = todoService.getByUserId(userId);
            model.addAttribute("todos", todos);
            model.addAttribute("user", userService.readById(userId));
            return "todos-user";
        } else  {
            throw  new EntityNotFoundException("This entity has not been found");
        }

    }

    @GetMapping("/{id}/add")
    public String addCollaborator(@PathVariable long id, @RequestParam("user_id") long userId) {

            ToDo todo = todoService.readById(id);
            List<User> collaborators = todo.getCollaborators();
            collaborators.add(userService.readById(userId));
            todo.setCollaborators(collaborators);
            todoService.update(todo);
            return "redirect:/todos/" + id + "/tasks";


    }

    @GetMapping("/{id}/remove")
    public String removeCollaborator(@PathVariable long id, @RequestParam("user_id") long userId) {

        ToDo todo = todoService.readById(id);
        List<User> collaborators = todo.getCollaborators();
        collaborators.remove(userService.readById(userId));
        todo.setCollaborators(collaborators);
        todoService.update(todo);
        return "redirect:/todos/" + id + "/tasks";
    }
}
