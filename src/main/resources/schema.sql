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

    image          VARCHAR(255) NOT NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);
