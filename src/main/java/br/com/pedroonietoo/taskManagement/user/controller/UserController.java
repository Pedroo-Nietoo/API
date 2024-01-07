package br.com.pedroonietoo.taskManagement.user.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.pedroonietoo.taskManagement.TaskManagementApiApplication;
import br.com.pedroonietoo.taskManagement.errors.ErrorResponse;
import br.com.pedroonietoo.taskManagement.user.dto.UserDto;
import br.com.pedroonietoo.taskManagement.user.model.UserModel;
import br.com.pedroonietoo.taskManagement.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Tag(name = "User", description = "Everything aboyt users")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Operation(
            summary = "Creates a user",
            description = "Creates a user in the database.",
            tags = {"User"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully", content = {@Content(schema = @Schema(implementation = TaskManagementApiApplication.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "409", description = "User with same e-mail already exists", content = {@Content(schema = @Schema(), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error | Something went wrong", content = {@Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = "application/json")})
    })
    @PostMapping("/")
    public ResponseEntity<Object> createUser(@RequestBody @Valid UserDto userDto) {
        var userModel = new UserModel();
        BeanUtils.copyProperties(userDto, userModel);
        var user = userRepository.findByEmail(userModel.getEmail());
        if (user != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("E-mail already in use");
        }
        if(!validate(userModel.getEmail())) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Invalid e-mail");
        }

        var passwordHashed = BCrypt.withDefaults().hashToString(12, userModel.getPassword().toCharArray());
        userModel.setPassword(passwordHashed);
        return ResponseEntity.status(HttpStatus.CREATED).body(userRepository.save(userModel));
    }

    @Operation(
            summary = "Lists all users",
            description = "Lists all users in the database.",
            tags = {"User"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users listed successfully", content = {@Content(schema = @Schema(implementation = TaskManagementApiApplication.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error | Something went wrong", content = {@Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = "application/json")})
    })
    @GetMapping("/")
    public ResponseEntity<List<UserModel>> getUsers() {
        List<UserModel> usersList = userRepository.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(usersList);
    }

    @Operation(
            summary = "List a user by id",
            description = "Lists a specific user information by passing the id.",
            tags = {"User"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User information listed successfully", content = {@Content(schema = @Schema(implementation = TaskManagementApiApplication.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "User not found", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error | Something went wrong", content = {@Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = "application/json")})
    })
    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable(value = "id") UUID id) {
        Optional<UserModel> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(user.get());
    }

    @Operation(
            summary = "Updates a user's information",
            description = "Updates a user's information in the database.",
            tags = {"User"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User information updated successfully", content = {@Content(schema = @Schema(implementation = TaskManagementApiApplication.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "User not found", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "409", description = "User with same e-mail already exists", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error | Something went wrong", content = {@Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = "application/json")})
    })
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable(name = "id") UUID id, @RequestBody @Valid UserDto userDto) {
        Optional<UserModel> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        var userData = user.get();
        BeanUtils.copyProperties(userDto, userData);
        var passwordHashed = BCrypt.withDefaults().hashToString(12, userData.getPassword().toCharArray());
        userData.setPassword(passwordHashed);
        return ResponseEntity.status(HttpStatus.OK).body(userRepository.save(userData));
    }

    @Operation(
            summary = "Deletes a user",
            description = "Removes a user in the database.",
            tags = {"User"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User removed successfully", content = {@Content(schema = @Schema(implementation = TaskManagementApiApplication.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "User not found", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error | Something went wrong", content = {@Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = "application/json")})
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable(name = "id") UUID id) {
        var user = userRepository.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        userRepository.delete(user.get());
        return ResponseEntity.status(HttpStatus.OK).body("User removed successfully");
    }

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean validate(String email) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.matches();
    }
}