package br.com.pedroonietoo.taskManagement.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserDto(@NotBlank String username, @NotBlank String email, @NotBlank String password) {
}
