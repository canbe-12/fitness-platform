package com.example.fitnessplatformbackend.controller;

import com.example.fitnessplatformbackend.dto.common.ApiResponse;
import com.example.fitnessplatformbackend.dto.food.FoodResponse;
import com.example.fitnessplatformbackend.dto.food.FoodUpsertRequest;
import com.example.fitnessplatformbackend.service.FoodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    @GetMapping
    public ApiResponse<List<FoodResponse>> search(@RequestParam(value = "keyword", required = false) String keyword) {
        return ApiResponse.ok(foodService.search(keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<FoodResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(foodService.getById(id));
    }

    @PostMapping
    public ApiResponse<FoodResponse> create(@Valid @RequestBody FoodUpsertRequest req) {
        return ApiResponse.ok(foodService.createCustom(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<FoodResponse> update(@PathVariable Long id, @Valid @RequestBody FoodUpsertRequest req) {
        return ApiResponse.ok(foodService.updateCustom(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        foodService.deleteCustom(id);
        return ApiResponse.ok(null);
    }
}
