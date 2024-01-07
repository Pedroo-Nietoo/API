package br.com.pedroonietoo.taskManagement.task.controller;

import br.com.pedroonietoo.taskManagement.TaskManagementApiApplication;
import br.com.pedroonietoo.taskManagement.errors.ErrorResponse;
import br.com.pedroonietoo.taskManagement.task.dto.TaskDto;
import br.com.pedroonietoo.taskManagement.task.model.TaskModel;
import br.com.pedroonietoo.taskManagement.task.repository.TaskRepository;
import br.com.pedroonietoo.taskManagement.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Task", description = "Everything aboyt tasks")
@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Operation(
            summary = "Creates a task",
            description = "Creates a task in the database.",
            tags = {"Task"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task created successfully", content = {@Content(schema = @Schema(implementation = TaskManagementApiApplication.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "User not found", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error | Something went wrong", content = {@Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = "application/json")})
    })
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

    @Operation(
            summary = "Lists all tasks",
            description = "Lists all tasks in the database.",
            tags = {"Task"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tasks listed successfully", content = {@Content(schema = @Schema(implementation = TaskManagementApiApplication.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error | Something went wrong", content = {@Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = "application/json")})
    })
    @GetMapping("/")
    public ResponseEntity<List<TaskModel>> getTasks() {
        return ResponseEntity.status(HttpStatus.OK).body(taskRepository.findAll());
    }

    @Operation(
            summary = "List a task by id",
            description = "Lists a specific task information by passing the id.",
            tags = {"Task"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task information listed successfully", content = {@Content(schema = @Schema(implementation = TaskManagementApiApplication.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Task not found", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error | Something went wrong", content = {@Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = "application/json")})
    })
    @GetMapping("/{id}")
    public ResponseEntity<Object> getTaskById(@PathVariable(name = "id") UUID id) {
        var task = taskRepository.findById(id);
        if (task.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @Operation(
            summary = "Updates a user's information",
            description = "Updates a user's information in the database.",
            tags = {"Task"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task information updated successfully", content = {@Content(schema = @Schema(implementation = TaskManagementApiApplication.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Task not found", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error | Something went wrong", content = {@Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = "application/json")})
    })
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

    @Operation(
            summary = "Deletes a task",
            description = "Removes a task in the database.",
            tags = {"Task"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task removed successfully", content = {@Content(schema = @Schema(implementation = TaskManagementApiApplication.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Task not found", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error | Something went wrong", content = {@Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = "application/json")})
    })
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