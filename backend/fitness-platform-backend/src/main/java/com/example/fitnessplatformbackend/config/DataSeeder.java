package com.example.fitnessplatformbackend.config;

import com.example.fitnessplatformbackend.entity.FoodEntity;
import com.example.fitnessplatformbackend.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final FoodRepository foodRepository;

    @Override
    public void run(String... args) throws Exception {
        if (foodRepository.count() > 0) {
            return;
        }

        log.info("Seeding common foods...");

        List<FoodEntity> foods = List.of(
            buildFood("米饭", "通用", "碗(150g)", 174, 3.8, 38.0, 0.4, 0.6, 2),
            buildFood("鸡胸肉(生)", "通用", "100g", 165, 31.0, 0.0, 3.6, 0.0, 74),
            buildFood("鸡蛋", "通用", "个(50g)", 74, 6.5, 0.6, 5.0, 0.0, 65),
            buildFood("香蕉", "通用", "根(120g)", 105, 1.3, 27.0, 0.4, 3.1, 1),
            buildFood("苹果", "通用", "个(200g)", 104, 0.5, 27.6, 0.3, 4.8, 2),
            buildFood("纯牛奶", "通用", "盒(250ml)", 163, 8.0, 12.0, 9.0, 0.0, 100),
            buildFood("西蓝花", "通用", "100g", 34, 2.8, 7.0, 0.4, 2.6, 33),
            buildFood("牛肉(瘦)", "通用", "100g", 250, 26.0, 0.0, 15.0, 0.0, 72),
            buildFood("三文鱼", "通用", "100g", 208, 20.0, 0.0, 13.0, 0.0, 59),
            buildFood("全麦面包", "通用", "片(35g)", 89, 4.0, 15.0, 1.0, 2.5, 150),
            buildFood("燕麦片", "通用", "100g", 389, 16.9, 66.3, 6.9, 10.6, 2),
            buildFood("酸奶(无糖)", "通用", "100g", 62, 3.5, 4.0, 3.2, 0.0, 50)
        );

        foodRepository.saveAll(foods);
        log.info("Seeded {} foods.", foods.size());
    }

    private FoodEntity buildFood(String name, String brand, String unit, int kcal, double p, double c, double f, double fiber, int sodium) {
        return FoodEntity.builder()
                .name(name)
                .brand(brand)
                .unit(unit)
                .kcal(kcal)
                .proteinG(BigDecimal.valueOf(p))
                .carbG(BigDecimal.valueOf(c))
                .fatG(BigDecimal.valueOf(f))
                .fiberG(BigDecimal.valueOf(fiber))
                .sodiumMg(sodium)
                .isOfficial(true)
                .build();
    }
}
