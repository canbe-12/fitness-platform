DROP DATABASE IF EXISTS fitness_platform;
CREATE DATABASE fitness_platform
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;
USE fitness_platform;

SET sql_mode = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION';

-- ---------- clean ----------
DROP TABLE IF EXISTS diet_log_item;
DROP TABLE IF EXISTS diet_log;
DROP TABLE IF EXISTS exercise_log;
DROP TABLE IF EXISTS body_measurement;
DROP TABLE IF EXISTS user_plan;
DROP TABLE IF EXISTS food;
DROP TABLE IF EXISTS exercise;
DROP TABLE IF EXISTS user_token;
DROP TABLE IF EXISTS `user`;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- 1) user
-- =========================================================
CREATE TABLE `user` (
                        `id`            BIGINT NOT NULL AUTO_INCREMENT,
                        `username`      VARCHAR(50)  NOT NULL,
                        `password_hash` VARCHAR(255) NOT NULL,

                        `nickname`      VARCHAR(50)  NULL,
                        `gender`        VARCHAR(255) NULL,
                        `birthday`      DATE         NULL,
                        `height_cm`     DECIMAL(6,2) NULL,
                        `weight_kg`     DECIMAL(6,2) NULL,

                        `goal`          VARCHAR(255) NULL,
                        `level`         VARCHAR(50)  NULL,

                        `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 2) user_token
-- =========================================================
CREATE TABLE `user_token` (
                              `id`          BIGINT NOT NULL AUTO_INCREMENT,
                              `user_id`     BIGINT NOT NULL,
                              `token`       VARCHAR(512) NOT NULL,
                              `expires_at`  DATETIME NOT NULL,
                              `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              PRIMARY KEY (`id`),
                              KEY `idx_token_user` (`user_id`),
                              CONSTRAINT `fk_token_user`
                                  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
                                      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 3) exercise
-- =========================================================
CREATE TABLE `exercise` (
                            `id`                    BIGINT NOT NULL AUTO_INCREMENT,
                            `name`                  VARCHAR(100) NOT NULL,
                            `muscle_group`          VARCHAR(50)  NULL,
                            `equipment`             VARCHAR(50)  NULL,
                            `default_target_weight` DECIMAL(6,2) NULL,
                            `default_target_reps`   INT          NULL,
                            `met`                   DECIMAL(5,2) NULL,
                            `created_at`            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_exercise_name` (`name`),
                            KEY `idx_exercise_muscle` (`muscle_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 4) user_plan (你的 PlanService/PlanController 需要)
-- =========================================================
CREATE TABLE `user_plan` (
                             `id`                 BIGINT NOT NULL AUTO_INCREMENT,
                             `user_id`            BIGINT NOT NULL,
                             `target`             VARCHAR(20) NOT NULL,     -- PlanTarget(EnumType.STRING)
                             `weekly_workout_days` INT       NOT NULL DEFAULT 0,
                             `daily_kcal_target`   INT       NOT NULL DEFAULT 0,
                             `protein_target_g`    DECIMAL(10,2) NOT NULL DEFAULT 0.00,

                             `created_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             `updated_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                             PRIMARY KEY (`id`),
                             UNIQUE KEY `uk_plan_user` (`user_id`),
                             CONSTRAINT `fk_plan_user`
                                 FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
                                     ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 5) exercise_log
-- =========================================================
CREATE TABLE `exercise_log` (
                                `id`               BIGINT NOT NULL AUTO_INCREMENT,
                                `user_id`          BIGINT NOT NULL,
                                `exercise_id`      BIGINT NOT NULL,
                                `workout_day_id`   BIGINT NULL,                 -- 你实体里是 Long，可留作扩展

                                `planned_date`     DATE    NOT NULL,
                                `logged_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `is_backfill`      TINYINT(1) NOT NULL DEFAULT 0,

                                `planned_weight`   DECIMAL(6,2) NULL,
                                `planned_reps`     INT NULL,
                                `actual_weight`    DECIMAL(6,2) NOT NULL,
                                `actual_reps`      INT NOT NULL,

                                `client_request_id` VARCHAR(64) NOT NULL,
                                `notes`             VARCHAR(255) NULL,

                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_exlog_idempotent` (`user_id`,`client_request_id`,`exercise_id`),
                                KEY `idx_exlog_user_date` (`user_id`,`planned_date`),
                                KEY `idx_exlog_user_exercise` (`user_id`,`exercise_id`,`planned_date`),

                                CONSTRAINT `fk_exlog_user`
                                    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
                                CONSTRAINT `fk_exlog_exercise`
                                    FOREIGN KEY (`exercise_id`) REFERENCES `exercise`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 6) food (营养快照落库的基础表)
-- =========================================================
CREATE TABLE `food` (
                        `id`          BIGINT NOT NULL AUTO_INCREMENT,
                        `name`        VARCHAR(100) NOT NULL,
                        `brand`       VARCHAR(100) NULL,
                        `unit`        VARCHAR(20)  NOT NULL,        -- 你的 FoodEntity 是 unit
                        `kcal`        INT          NOT NULL DEFAULT 0,
                        `protein_g`   DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                        `carb_g`      DECIMAL(10,2) NOT NULL DEFAULT 0.00,  -- 关键：carb_g
                        `fat_g`       DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                        `fiber_g`     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                        `sodium_mg`   INT          NOT NULL DEFAULT 0,
                        `is_official` TINYINT(1)   NOT NULL DEFAULT 0,
                        `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        PRIMARY KEY (`id`),
                        KEY `idx_food_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 7) diet_log (营养快照：每天/每餐汇总)
--    关键：carb_g / log_date / created_at / updated_at
-- =========================================================
CREATE TABLE `diet_log` (
                            `id`               BIGINT NOT NULL AUTO_INCREMENT,
                            `user_id`           BIGINT NOT NULL,
                            `log_date`          DATE   NOT NULL,
                            `meal_type`         VARCHAR(20) NOT NULL,          -- MealType(EnumType.STRING)
                            `client_request_id` VARCHAR(64) NOT NULL,

                            `kcal`        INT NOT NULL DEFAULT 0,
                            `protein_g`   DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                            `carb_g`      DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                            `fat_g`       DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                            `fiber_g`     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                            `sodium_mg`   INT NOT NULL DEFAULT 0,

                            `note`        VARCHAR(255) NULL,

                            `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_dietlog_user_req` (`user_id`,`client_request_id`),
                            KEY `idx_dietlog_user_date` (`user_id`,`log_date`),

                            CONSTRAINT `fk_dietlog_user`
                                FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 8) diet_log_item (明细：一条饮食记录里的每个食物项)
-- =========================================================
CREATE TABLE `diet_log_item` (
                                 `id`          BIGINT NOT NULL AUTO_INCREMENT,
                                 `user_id`     BIGINT NOT NULL,
                                 `diet_log_id` BIGINT NOT NULL,

                                 `food_id`     BIGINT NULL,
                                 `food_name`   VARCHAR(100) NOT NULL,
                                 `brand`       VARCHAR(100) NULL,
                                 `amount`      DECIMAL(10,2) NOT NULL,
                                 `unit`        VARCHAR(20)   NOT NULL,

                                 `kcal`        INT NOT NULL DEFAULT 0,
                                 `protein_g`   DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                                 `carb_g`      DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                                 `fat_g`       DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                                 `fiber_g`     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                                 `sodium_mg`   INT NOT NULL DEFAULT 0,

                                 `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 PRIMARY KEY (`id`),
                                 KEY `idx_dietitem_log` (`diet_log_id`),

                                 CONSTRAINT `fk_dietitem_user`
                                     FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
                                 CONSTRAINT `fk_dietitem_log`
                                     FOREIGN KEY (`diet_log_id`) REFERENCES `diet_log`(`id`) ON DELETE CASCADE,
                                 CONSTRAINT `fk_dietitem_food`
                                     FOREIGN KEY (`food_id`) REFERENCES `food`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 9) body_measurement (体重趋势/围度/体脂)
-- =========================================================
CREATE TABLE `body_measurement` (
                                    `id`              BIGINT NOT NULL AUTO_INCREMENT,
                                    `user_id`         BIGINT NOT NULL,
                                    `measure_date`    DATE   NOT NULL,

                                    `weight_kg`       DECIMAL(6,2) NOT NULL,
                                    `body_fat_percent` DECIMAL(5,2) NULL,
                                    `waist_cm`        DECIMAL(6,2) NULL,
                                    `notes`           VARCHAR(255) NULL,

                                    `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_body_user_date` (`user_id`,`measure_date`),
                                    KEY `idx_body_user_date` (`user_id`,`measure_date`),

                                    CONSTRAINT `fk_body_user`
                                        FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- seed (可选：给点基础数据，方便 swagger 直接测)
-- =========================================================
INSERT INTO `exercise` (`name`,`muscle_group`,`equipment`,`default_target_weight`,`default_target_reps`,`met`)
VALUES
    ('深蹲','腿','杠铃',60.00,8,5.50),
    ('卧推','胸','杠铃',40.00,8,5.00),
    ('跑步','有氧','无',NULL,NULL,7.00);

INSERT INTO `food` (`name`,`brand`,`unit`,`kcal`,`protein_g`,`carb_g`,`fat_g`,`fiber_g`,`sodium_mg`,`is_official`)
VALUES
    ('米饭(熟)','官方','g',116,2.60,25.90,0.30,0.40,1,1),
    ('鸡胸肉(熟)','官方','g',165,31.00,0.00,3.60,0.00,74,1),
    ('鸡蛋','官方','g',143,13.00,0.70,9.50,0.00,142,1);
