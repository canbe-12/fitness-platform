package com.example.fitnessplatformbackend.controller;

import com.example.fitnessplatformbackend.common.ApiResponse;
import com.example.fitnessplatformbackend.dto.task.TaskResponse;
import com.example.fitnessplatformbackend.dto.task.TaskUpsertRequest;
import com.example.fitnessplatformbackend.service.ScheduledTaskService;
import com.example.fitnessplatformbackend.service.SecurityCurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "ScheduledTask", description = "每日任务管理")
public class ScheduledTaskController {

    private final ScheduledTaskService taskService;
    private final SecurityCurrentUserService currentUser;

    @Operation(summary = "获取某日任务")
    @GetMapping
    public ApiResponse<List<TaskResponse>> getTasks(@RequestParam LocalDate date) {
        Long userId = currentUser.getCurrentUserId();
        return ApiResponse.ok(taskService.getTasksByDate(userId, date));
    }
    
    @Operation(summary = "获取日期范围任务 (日历用)")
    @GetMapping("/range")
    public ApiResponse<List<TaskResponse>> getTasksRange(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        Long userId = currentUser.getCurrentUserId();
        return ApiResponse.ok(taskService.getTasksByRange(userId, from, to));
    }

    @Operation(summary = "创建任务")
    @PostMapping
    public ApiResponse<TaskResponse> createTask(@Valid @RequestBody TaskUpsertRequest req) {
        Long userId = currentUser.getCurrentUserId();
        return ApiResponse.ok(taskService.createTask(userId, req));
    }

    @Operation(summary = "更新任务")
    @PutMapping("/{id}")
    public ApiResponse<TaskResponse> updateTask(@PathVariable Long id, @Valid @RequestBody TaskUpsertRequest req) {
        Long userId = currentUser.getCurrentUserId();
        return ApiResponse.ok(taskService.updateTask(userId, id, req));
    }

    @Operation(summary = "删除任务")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTask(@PathVariable Long id) {
        Long userId = currentUser.getCurrentUserId();
        taskService.deleteTask(userId, id);
        return ApiResponse.ok(null);
    }
    
    @Operation(summary = "更新状态")
    @PatchMapping("/{id}/status")
    public ApiResponse<TaskResponse> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Long userId = currentUser.getCurrentUserId();
        return ApiResponse.ok(taskService.updateStatus(userId, id, status));
    }
}
