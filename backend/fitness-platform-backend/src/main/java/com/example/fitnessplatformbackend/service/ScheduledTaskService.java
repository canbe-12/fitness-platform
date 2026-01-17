package com.example.fitnessplatformbackend.service;

import com.example.fitnessplatformbackend.dto.task.TaskResponse;
import com.example.fitnessplatformbackend.dto.task.TaskUpsertRequest;
import com.example.fitnessplatformbackend.entity.ScheduledTaskEntity;
import com.example.fitnessplatformbackend.repository.ScheduledTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduledTaskService {

    private final ScheduledTaskRepository taskRepository;

    public List<TaskResponse> getTasksByDate(Long userId, LocalDate date) {
        return taskRepository.findByUserIdAndTaskDateOrderByPriorityDesc(userId, date).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<TaskResponse> getTasksByRange(Long userId, LocalDate from, LocalDate to) {
        return taskRepository.findByUserIdAndTaskDateBetween(userId, from, to).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse createTask(Long userId, TaskUpsertRequest req) {
        ScheduledTaskEntity entity = new ScheduledTaskEntity();
        entity.setUserId(userId);
        entity.setTaskDate(req.getTaskDate());
        entity.setTitle(req.getTitle());
        entity.setDescription(req.getDescription());
        entity.setStatus("PENDING");
        entity.setPriority(req.getPriority() != null ? req.getPriority() : 0);
        
        ScheduledTaskEntity saved = taskRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional
    public TaskResponse updateTask(Long userId, Long taskId, TaskUpsertRequest req) {
        ScheduledTaskEntity entity = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        if (!entity.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        entity.setTaskDate(req.getTaskDate());
        entity.setTitle(req.getTitle());
        entity.setDescription(req.getDescription());
        if (req.getStatus() != null) {
            entity.setStatus(req.getStatus());
        }
        if (req.getPriority() != null) {
            entity.setPriority(req.getPriority());
        }

        return toResponse(taskRepository.save(entity));
    }

    @Transactional
    public void deleteTask(Long userId, Long taskId) {
        ScheduledTaskEntity entity = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        if (!entity.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        taskRepository.delete(entity);
    }
    
    @Transactional
    public TaskResponse updateStatus(Long userId, Long taskId, String status) {
        ScheduledTaskEntity entity = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        if (!entity.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        entity.setStatus(status);
        return toResponse(taskRepository.save(entity));
    }

    private TaskResponse toResponse(ScheduledTaskEntity entity) {
        TaskResponse resp = new TaskResponse();
        BeanUtils.copyProperties(entity, resp);
        return resp;
    }
}
