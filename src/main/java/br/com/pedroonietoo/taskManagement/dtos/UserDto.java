package br.com.pedroonietoo.taskManagement.dtos;

import jakarta.validation.constraints.NotBlank;

public record UserDto(@NotBlank String username, @NotBlank String email, @NotBlank String password) {
}
