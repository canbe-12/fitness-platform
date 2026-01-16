package com.example.fitnessplatformbackend.service;

import com.example.fitnessplatformbackend.dto.food.FoodResponse;
import com.example.fitnessplatformbackend.dto.food.FoodUpsertRequest;
import com.example.fitnessplatformbackend.entity.FoodEntity;
import com.example.fitnessplatformbackend.repository.FoodRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodService {
    private final FoodRepository foodRepository;

    public List<FoodResponse> search(String keyword) {
        if (keyword == null || keyword.isBlank()) keyword = "";
        return foodRepository.findTop20ByNameContainingIgnoreCaseOrderByNameAsc(keyword.trim())
                .stream().map(this::toResp).toList();
    }

    public FoodResponse getById(Long id) {
        FoodEntity e = foodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Food not found"));
        return toResp(e);
    }

    @Transactional
    public FoodResponse createCustom(FoodUpsertRequest req) {
        FoodEntity e = FoodEntity.builder()
                .name(req.getName())
                .brand(req.getBrand())
                .unit(req.getUnit())
                .kcal(req.getKcal())
                .proteinG(req.getProteinG())
                .carbG(req.getCarbG())
                .fatG(req.getFatG())
                .fiberG(req.getFiberG())
                .sodiumMg(req.getSodiumMg())
                .isOfficial(false)
                .createdAt(LocalDateTime.now())
                .build();
        return toResp(foodRepository.save(e));
    }

    @Transactional
    public FoodResponse updateCustom(Long id, FoodUpsertRequest req) {
        FoodEntity e = foodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Food not found"));

        if (Boolean.TRUE.equals(e.getIsOfficial())) {
            throw new IllegalStateException("官方食物不允许修改");
        }

        e.setName(req.getName());
        e.setBrand(req.getBrand());
        e.setUnit(req.getUnit());
        e.setKcal(req.getKcal());
        e.setProteinG(req.getProteinG());
        e.setCarbG(req.getCarbG());
        e.setFatG(req.getFatG());
        e.setFiberG(req.getFiberG());
        e.setSodiumMg(req.getSodiumMg());

        return toResp(foodRepository.save(e));
    }

    @Transactional
    public void deleteCustom(Long id) {
        FoodEntity e = foodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Food not found"));

        if (Boolean.TRUE.equals(e.getIsOfficial())) {
            throw new IllegalStateException("官方食物不允许删除");
        }
        foodRepository.delete(e);
    }

    private FoodResponse toResp(FoodEntity e) {
        return FoodResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .brand(e.getBrand())
                .unit(e.getUnit())
                .kcal(e.getKcal())
                .proteinG(e.getProteinG())
                .carbG(e.getCarbG())
                .fatG(e.getFatG())
                .fiberG(e.getFiberG())
                .sodiumMg(e.getSodiumMg())
                .isOfficial(e.getIsOfficial())
                .build();
    }
}
