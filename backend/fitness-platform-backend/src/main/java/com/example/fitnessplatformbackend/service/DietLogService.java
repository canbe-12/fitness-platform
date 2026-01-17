package com.example.fitnessplatformbackend.service;

import com.example.fitnessplatformbackend.dto.diet.*;
import com.example.fitnessplatformbackend.entity.DietLogEntity;
import com.example.fitnessplatformbackend.entity.DietLogItemEntity;
import com.example.fitnessplatformbackend.repository.DietLogRepository;
import com.example.fitnessplatformbackend.repository.FoodRepository;
import com.example.fitnessplatformbackend.repository.UserPlanRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DietLogService {

    private final DietLogRepository dietLogRepository;
    private final FoodRepository foodRepository;
    private final UserPlanRepository userPlanRepository;

    @Transactional
    public DietLogCreateResponse createOrGet(Long userId, DietLogCreateRequest req) {
        // 幂等：同 user + clientRequestId 直接返回已有记录
        var existed = dietLogRepository.findByUserIdAndClientRequestId(userId, req.getClientRequestId());
        if (existed.isPresent()) {
            return new DietLogCreateResponse(existed.get().getId(), existed.get().getDate());
        }

        NutritionSnapshotDto snap = req.getNutritionSnapshot();
        if (snap == null) {
            snap = sumFromItems(req.getItems());
        }

        LocalDateTime now = LocalDateTime.now();
        DietLogEntity log = DietLogEntity.builder()
                .userId(userId)
                .clientRequestId(req.getClientRequestId())
                .date(req.getDate())
                .mealType(req.getMealType())
                .note(req.getNote())
                .kcal(snap.getKcal())
                .proteinG(snap.getProteinG())
                .carbG(snap.getCarbG())
                .fatG(snap.getFatG())
                .fiberG(snap.getFiberG())
                .sodiumMg(snap.getSodiumMg())
                .createdAt(now)
                .updatedAt(now)
                .build();

        List<DietLogItemEntity> items = req.getItems().stream().map(i -> {
            // 验证 foodId：如果为 null、<= 0 或不存在，设置为 null
            Long foodId = i.getFoodId();
            if (foodId != null && foodId > 0) {
                // 验证 foodId 是否存在
                if (!foodRepository.existsById(foodId)) {
                    foodId = null;
                }
            } else {
                foodId = null;
            }
            
            return DietLogItemEntity.builder()
                    .userId(userId)
                    .dietLog(log)
                    .foodId(foodId)
                    .foodName(i.getFoodName())
                    .brand(i.getBrand())
                    .amount(i.getAmount())
                    .unit(i.getUnit())
                    .kcal(i.getKcal())
                    .proteinG(i.getProteinG())
                    .carbG(i.getCarbG())
                    .fatG(i.getFatG())
                    .fiberG(i.getFiberG())
                    .sodiumMg(i.getSodiumMg())
                    .createdAt(now)
                    .build();
        }).toList();

        log.getItems().addAll(items);

        DietLogEntity saved = dietLogRepository.save(log);
        return new DietLogCreateResponse(saved.getId(), saved.getDate());
    }

    public DietLogDayResponse getDay(Long userId, java.time.LocalDate date) {
        List<DietLogEntity> logs = dietLogRepository.findByUserIdAndDate(userId, date);

        var meals = logs.stream().map(l ->
                new DietLogDayResponse.MealSummary(
                        l.getId(),
                        l.getMealType(),
                        l.getKcal(),
                        l.getProteinG(),
                        l.getCarbG(),
                        l.getFatG()
                )
        ).toList();

        NutritionSnapshotDto total = new NutritionSnapshotDto();
        total.setKcal(logs.stream().mapToInt(DietLogEntity::getKcal).sum());
        total.setProteinG(sumDecimal(logs.stream().map(DietLogEntity::getProteinG).toList()));
        total.setCarbG(sumDecimal(logs.stream().map(DietLogEntity::getCarbG).toList()));
        total.setFatG(sumDecimal(logs.stream().map(DietLogEntity::getFatG).toList()));
        total.setFiberG(sumDecimal(logs.stream().map(DietLogEntity::getFiberG).toList()));
        total.setSodiumMg(logs.stream().mapToInt(DietLogEntity::getSodiumMg).sum());

        // Get Target from Plan
        NutritionSnapshotDto target = new NutritionSnapshotDto();
        userPlanRepository.findByUserId(userId).ifPresent(plan -> {
            target.setKcal(plan.getDailyKcalTarget());
            target.setProteinG(plan.getProteinTargetG());
            
            // Estimate Carb (50%) and Fat (30%) based on Kcal
            // 1g Carb = 4kcal, 1g Fat = 9kcal
            if (plan.getDailyKcalTarget() != null) {
                BigDecimal kcal = new BigDecimal(plan.getDailyKcalTarget());
                
                // Carb: (kcal * 0.5) / 4
                BigDecimal carb = kcal.multiply(new BigDecimal("0.5"))
                        .divide(new BigDecimal("4"), 1, RoundingMode.HALF_UP);
                target.setCarbG(carb);

                // Fat: (kcal * 0.3) / 9
                BigDecimal fat = kcal.multiply(new BigDecimal("0.3"))
                        .divide(new BigDecimal("9"), 1, RoundingMode.HALF_UP);
                target.setFatG(fat);
            }
        });

        return new DietLogDayResponse(date, meals, total, target);
    }

    public List<DietLogRangeDaySummary> getRange(Long userId, java.time.LocalDate start, java.time.LocalDate end) {
        List<DietLogEntity> logs = dietLogRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, start, end);

        // 按天汇总
        var map = logs.stream().collect(Collectors.groupingBy(DietLogEntity::getDate));

        return map.entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(e -> {
                    var list = e.getValue();
                    return new DietLogRangeDaySummary(
                            e.getKey(),
                            list.stream().mapToInt(DietLogEntity::getKcal).sum(),
                            sumDecimal(list.stream().map(DietLogEntity::getProteinG).toList()),
                            sumDecimal(list.stream().map(DietLogEntity::getCarbG).toList()),
                            sumDecimal(list.stream().map(DietLogEntity::getFatG).toList())
                    );
                })
                .toList();
    }

    @Transactional
    public void delete(Long userId, Long id) {
        DietLogEntity log = dietLogRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("DietLog not found"));
        dietLogRepository.delete(log);
    }

    private NutritionSnapshotDto sumFromItems(List<DietLogItemDto> items) {
        NutritionSnapshotDto s = new NutritionSnapshotDto();
        s.setKcal(items.stream().mapToInt(DietLogItemDto::getKcal).sum());
        s.setProteinG(sumDecimal(items.stream().map(DietLogItemDto::getProteinG).toList()));
        s.setCarbG(sumDecimal(items.stream().map(DietLogItemDto::getCarbG).toList()));
        s.setFatG(sumDecimal(items.stream().map(DietLogItemDto::getFatG).toList()));
        s.setFiberG(sumDecimal(items.stream().map(DietLogItemDto::getFiberG).toList()));
        s.setSodiumMg(items.stream().mapToInt(DietLogItemDto::getSodiumMg).sum());
        return s;
    }

    private BigDecimal sumDecimal(List<BigDecimal> list) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal v : list) {
            if (v != null) sum = sum.add(v);
        }
        return sum;
    }

    public DietLogDetailResponse getDetail(Long userId, Long id) {
        DietLogEntity log = dietLogRepository.findDetailByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("DietLog not found"));

        NutritionSnapshotDto snap = new NutritionSnapshotDto();
        snap.setKcal(log.getKcal());
        snap.setProteinG(log.getProteinG());
        snap.setCarbG(log.getCarbG());
        snap.setFatG(log.getFatG());
        snap.setFiberG(log.getFiberG());
        snap.setSodiumMg(log.getSodiumMg());

        List<DietLogItemDto> items = log.getItems().stream().map(it -> {
            DietLogItemDto d = new DietLogItemDto();
            d.setFoodId(it.getFoodId());
            d.setFoodName(it.getFoodName());
            d.setBrand(it.getBrand());
            d.setAmount(it.getAmount());
            d.setUnit(it.getUnit());
            d.setKcal(it.getKcal());
            d.setProteinG(it.getProteinG());
            d.setCarbG(it.getCarbG());
            d.setFatG(it.getFatG());
            d.setFiberG(it.getFiberG());
            d.setSodiumMg(it.getSodiumMg());
            return d;
        }).toList();

        return new DietLogDetailResponse(
                log.getId(),
                log.getDate(),
                log.getMealType(),
                log.getNote(),
                snap,
                items
        );
    }

    @Transactional
    public DietLogDetailResponse updateNote(Long userId, Long id, String note) {
        DietLogEntity log = dietLogRepository.findDetailByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("DietLog not found"));

        log.setNote(note);
        log.setUpdatedAt(java.time.LocalDateTime.now());
        dietLogRepository.save(log);

        return getDetail(userId, id);
    }

    public NutritionSnapshotDto getNutritionDay(Long userId, java.time.LocalDate date) {
        List<DietLogEntity> logs = dietLogRepository.findByUserIdAndDate(userId, date);

        NutritionSnapshotDto total = new NutritionSnapshotDto();
        total.setKcal(logs.stream().mapToInt(DietLogEntity::getKcal).sum());
        total.setProteinG(sumDecimal(logs.stream().map(DietLogEntity::getProteinG).toList()));
        total.setCarbG(sumDecimal(logs.stream().map(DietLogEntity::getCarbG).toList()));
        total.setFatG(sumDecimal(logs.stream().map(DietLogEntity::getFatG).toList()));
        total.setFiberG(sumDecimal(logs.stream().map(DietLogEntity::getFiberG).toList()));
        total.setSodiumMg(logs.stream().mapToInt(DietLogEntity::getSodiumMg).sum());
        return total;
    }

    public List<com.example.fitnessplatformbackend.dto.diet.DietNutritionRangeDayResponse> getNutritionRange(
            Long userId, java.time.LocalDate start, java.time.LocalDate end) {

        List<DietLogEntity> logs = dietLogRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, start, end);
        var map = logs.stream().collect(java.util.stream.Collectors.groupingBy(DietLogEntity::getDate));

        return map.entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(e -> {
                    var list = e.getValue();
                    NutritionSnapshotDto total = new NutritionSnapshotDto();
                    total.setKcal(list.stream().mapToInt(DietLogEntity::getKcal).sum());
                    total.setProteinG(sumDecimal(list.stream().map(DietLogEntity::getProteinG).toList()));
                    total.setCarbG(sumDecimal(list.stream().map(DietLogEntity::getCarbG).toList()));
                    total.setFatG(sumDecimal(list.stream().map(DietLogEntity::getFatG).toList()));
                    total.setFiberG(sumDecimal(list.stream().map(DietLogEntity::getFiberG).toList()));
                    total.setSodiumMg(list.stream().mapToInt(DietLogEntity::getSodiumMg).sum());
                    return new com.example.fitnessplatformbackend.dto.diet.DietNutritionRangeDayResponse(e.getKey(), total);
                })
                .toList();
    }

}
