package com.example.fitnessplatformbackend.service;

import com.example.fitnessplatformbackend.dto.exercise.ExerciseTrendPoint;
import com.example.fitnessplatformbackend.dto.exerciselog.ExerciseLogResponse;
import com.example.fitnessplatformbackend.dto.exerciselog.ExerciseLogSubmitRequest;
import com.example.fitnessplatformbackend.entity.ExerciseLogEntity;
import com.example.fitnessplatformbackend.repository.ExerciseLogRepository;
import com.example.fitnessplatformbackend.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ExerciseLogService {
    private final ExerciseLogRepository exerciseLogRepository;
    private final ExerciseRepository exerciseRepository;

    @Transactional
    public ExerciseLogResponse submit(Long userId, ExerciseLogSubmitRequest req) {
        // 幂等：重复提交直接返回旧记录
        Optional<ExerciseLogEntity> existed = exerciseLogRepository
                .findByUserIdAndClientRequestIdAndExerciseId(userId, req.getClientRequestId(), req.getExerciseId());
        if (existed.isPresent()) {
            return toResp(existed.get());
        }

        // 验证 exerciseId 是否存在
        Long exerciseId = req.getExerciseId();
        if (exerciseId == null || exerciseId <= 0 || !exerciseRepository.existsById(exerciseId)) {
            throw new IllegalArgumentException("无效的 exerciseId: " + exerciseId);
        }

        ExerciseLogEntity log = new ExerciseLogEntity();
        log.setUserId(userId);
        log.setExerciseId(exerciseId);
        log.setPlannedDate(req.getPlannedDate());
        log.setLoggedAt(LocalDateTime.now());

        boolean backfill = req.getPlannedDate() != null && req.getPlannedDate().isBefore(LocalDate.now());
        log.setIsBackfill(backfill);

        log.setActualWeight(req.getActualWeight());
        log.setActualReps(req.getActualReps());
        log.setPlannedWeight(req.getPlannedWeight());
        log.setPlannedReps(req.getPlannedReps());
        log.setClientRequestId(req.getClientRequestId());
        log.setNotes(req.getNotes());

        ExerciseLogEntity saved = exerciseLogRepository.save(log);
        return toResp(saved);
    }

    public List<ExerciseLogResponse> listByDate(Long userId, LocalDate date) {
        return exerciseLogRepository.findByUserIdAndPlannedDateOrderByLoggedAtAsc(userId, date)
                .stream().map(this::toResp).toList();
    }

    public List<ExerciseLogResponse> listRange(Long userId, LocalDate from, LocalDate to) {
        return exerciseLogRepository.findRange(userId, from, to).stream().map(this::toResp).toList();
    }

    @Transactional
    public void delete(Long userId, Long id) {
        ExerciseLogEntity l = exerciseLogRepository.findById(id).orElseThrow();
        if (!Objects.equals(l.getUserId(), userId)) throw new RuntimeException("Forbidden");
        exerciseLogRepository.delete(l);
    }

    public List<ExerciseTrendPoint> trendMaxWeight(Long exerciseId, LocalDate from, LocalDate to) {
        // 这里 userId 需要从调用方传入（我在 controller 调用了 service 时会用当前用户）
        throw new UnsupportedOperationException("Use trendMaxWeight(userId, exerciseId, from, to)");
    }

    public List<ExerciseTrendPoint> trendMaxWeight(Long userId, Long exerciseId, LocalDate from, LocalDate to) {
        List<Object[]> rows = exerciseLogRepository.trendMaxWeight(userId, exerciseId, from, to);
        List<ExerciseTrendPoint> res = new ArrayList<>();
        for (Object[] r : rows) {
            String d = String.valueOf(r[0]);
            BigDecimal w = (r[1] == null) ? null : new BigDecimal(String.valueOf(r[1]));
            res.add(new ExerciseTrendPoint(d, w));
        }
        return res;
    }

    private ExerciseLogResponse toResp(ExerciseLogEntity l) {
        return ExerciseLogResponse.builder()
                .id(l.getId())
                .exerciseId(l.getExerciseId())
                .plannedDate(l.getPlannedDate())
                .loggedAt(l.getLoggedAt())
                .isBackfill(l.getIsBackfill())
                .actualWeight(l.getActualWeight())
                .actualReps(l.getActualReps())
                .notes(l.getNotes())
                .build();
    }
}
