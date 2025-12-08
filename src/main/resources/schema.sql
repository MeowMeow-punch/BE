-- 기준 단위 (g, ml)
CREATE TYPE base_unit_type AS ENUM ('G', 'ML');

-- 식단 종류
CREATE TYPE meal_status_type AS ENUM ('BREAKFAST', 'LUNCH', 'DINNER', 'SNACK');

-- 식단 생성 타입
CREATE TYPE source_type_type AS ENUM ('USERINPUT', 'RECOMMENDATION');

-- foods 테이블
CREATE TABLE IF NOT EXISTS foods (
    id             BIGSERIAL PRIMARY KEY,
    food_code      VARCHAR(50)  NOT NULL UNIQUE,
    name           VARCHAR(200) NOT NULL,
    category       VARCHAR(50)  NOT NULL,

    base_amount    INTEGER      NOT NULL,
    base_unit      VARCHAR(10)  NOT NULL, -- 'G' 또는 'ML'

    serving_size   NUMERIC(6,2),
    serving_desc   VARCHAR(100),

    kcal           NUMERIC(8,2) NOT NULL,
    carbs          NUMERIC(8,2) NOT NULL,
    protein        NUMERIC(8,2) NOT NULL,
    fat            NUMERIC(8,2) NOT NULL,
    sugar          NUMERIC(8,2) NOT NULL,
    dietary_fiber  NUMERIC(8,2) NOT NULL,
    vit_a          NUMERIC(8,2) NOT NULL,
    vit_c          NUMERIC(8,2) NOT NULL,
    vit_d          NUMERIC(8,2) NOT NULL,
    calcium        NUMERIC(8,2) NOT NULL,
    iron           NUMERIC(8,2) NOT NULL,
    sodium         NUMERIC(8,2) NOT NULL,

    thumbnail_url  VARCHAR(255) NOT NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- 내 식단 테이블
CREATE TABLE IF NOT EXISTS my_diets (
    diet_id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id       CHAR(36)      NOT NULL,

    status        meal_status_type NOT NULL,
    title         VARCHAR(200)  NOT NULL,
    date          DATE          NOT NULL,
    time          TIME,
    thumbnail_url VARCHAR(500),

    source_type   source_type_type NOT NULL DEFAULT 'USERINPUT',

    -- 합산 영양 정보 (식단 1끼 전체)
    kcal          NUMERIC(8,2)  NOT NULL,
    carbs         NUMERIC(8,2)  NOT NULL,
    protein       NUMERIC(8,2)  NOT NULL,
    fat           NUMERIC(8,2)  NOT NULL,

    -- nutrientsDetail 8개
    sugar         NUMERIC(8,2)  NOT NULL,
    vit_a         NUMERIC(8,2),
    vit_c         NUMERIC(8,2),
    vit_d         NUMERIC(8,2),
    calcium       NUMERIC(8,2),
    iron          NUMERIC(8,2),
    dietary_fiber NUMERIC(8,2),
    sodium        NUMERIC(8,2),

    created_at    TIMESTAMP     NOT NULL,
    updated_at    TIMESTAMP     NOT NULL

--   CONSTRAINT fk_my_diets_user
--        FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 식단 추천 테이블
CREATE TABLE IF NOT EXISTS recommended_meals (
    recommendation_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id           CHAR(36)  NOT NULL,        -- FK -> users.user_id
    food_id           BIGINT    NOT NULL,        -- FK -> foods.id

    title             VARCHAR(200) NOT NULL,     -- 카드에 노출할 이름

    kcal              NUMERIC(8,2) NOT NULL,
    carbs             NUMERIC(8,2) NOT NULL,
    protein           NUMERIC(8,2) NOT NULL,
    fat               NUMERIC(8,2) NOT NULL,

    thumbnail_url     VARCHAR(500),

    created_at        TIMESTAMP NOT NULL,
    updated_at        TIMESTAMP NOT NULL,

--    CONSTRAINT fk_recommended_meals_user
--        FOREIGN KEY (user_id) REFERENCES users(user_id),

    CONSTRAINT fk_recommended_meals_food
        FOREIGN KEY (food_id) REFERENCES foods(id)
);

 -- 음식-식단 중간 테이블
CREATE TABLE IF NOT EXISTS my_diet_foods (
    diet_id   BIGINT   NOT NULL,
    food_id   BIGINT   NOT NULL,

    -- 100g/100ml(또는 1인분) 기준 몇 번 먹었는지
    quantity  SMALLINT NOT NULL DEFAULT 1,

    CONSTRAINT fk_my_diet_foods_diet
        FOREIGN KEY (diet_id) REFERENCES my_diets(diet_id),

    CONSTRAINT fk_my_diet_foods_food
        FOREIGN KEY (food_id) REFERENCES foods(id)
);
