package br.com.pedroonietoo.taskManagement.task.controller;

import br.com.pedroonietoo.taskManagement.task.dto.TaskDto;
import br.com.pedroonietoo.taskManagement.task.model.TaskModel;
import br.com.pedroonietoo.taskManagement.task.repository.TaskRepository;
import br.com.pedroonietoo.taskManagement.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/")
    public ResponseEntity createTask(@RequestBody @Valid TaskDto taskDto) {
        var task = new TaskModel();
        BeanUtils.copyProperties(taskDto, task);

        var userId = task.getUserId();
        var userExists = userRepository.findById(userId);

        if(userExists.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User id doesn't exists");
        }

        var currentDate = LocalDateTime.now();

        if (currentDate.isAfter(task.getStartAt()) || currentDate.isAfter(task.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Start/end date must be bigger than current date");
        }

        if (task.getStartAt().isAfter(task.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Start date must be before than end date");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(taskRepository.save(task));
    }

    @GetMapping("/")
    public ResponseEntity<List<TaskModel>> getTasks() {
        return ResponseEntity.status(HttpStatus.OK).body(taskRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getTaskById(@PathVariable(name = "id") UUID id) {
        var task = taskRepository.findById(id);
        if (task.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateTask(@PathVariable(name = "id") UUID id, @RequestBody @Valid TaskDto taskDto) {
        Optional<TaskModel> task = taskRepository.findById(id);
        if (task.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        var taskData = task.get();
        BeanUtils.copyProperties(taskDto, taskData);

        var userId = taskData.getUserId();
        var userExists = userRepository.findById(userId);

        if(userExists.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User id doesn't exists");
        }

        return ResponseEntity.status(HttpStatus.OK).body(taskRepository.save(taskData));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteTask(@PathVariable(name = "id") UUID id) {
        var task = taskRepository.findById(id);
        if (task.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }
        taskRepository.delete(task.get());
        return ResponseEntity.status(HttpStatus.OK).body("Task deleted successfully");
    }
}