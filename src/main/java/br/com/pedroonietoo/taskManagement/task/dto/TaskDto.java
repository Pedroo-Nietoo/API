package br.com.pedroonietoo.taskManagement.task.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskDto(@NotBlank String title, @NotBlank String description, @NotNull LocalDateTime startAt,
        @Future LocalDateTime endAt, @NotBlank String priority, @NotNull UUID userId) {
}