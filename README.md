# Diet Service

```sql

CREATE TABLE foods (
id              varchar(36) PRIMARY KEY,
name            TEXT    NOT NULL,
description     TEXT,
calories        DOUBLE PRECISION NOT NULL CHECK (calories       >= 0),
protein         DOUBLE PRECISION NOT NULL CHECK (protein        >= 0),
carbs           DOUBLE PRECISION NOT NULL CHECK (carbs          >= 0),
saturated_fats  DOUBLE PRECISION NOT NULL CHECK (saturated_fats >= 0),
trans_fats      DOUBLE PRECISION NOT NULL CHECK (trans_fats     >= 0),
fiber           DOUBLE PRECISION NOT NULL CHECK (fiber          >= 0),
sugar           DOUBLE PRECISION NOT NULL CHECK (sugar          >= 0)
);

CREATE TABLE meals (
id         varchar(36) PRIMARY KEY,
userid     TEXT    NOT NULL,
name       TEXT    NOT NULL,
meal_type  TEXT    NOT NULL,
date       varchar(255) NOT NULL
);

CREATE TABLE meal_foods (
    meal_id varchar(36) REFERENCES meals(id) ON DELETE CASCADE,
    food_id varchar(36) REFERENCES foods(id) ON DELETE CASCADE,
    PRIMARY KEY (meal_id, food_id)
);
```

```sql
INSERT INTO foods (id, name, description, calories, protein, carbs,
                   saturated_fats, trans_fats, fiber, sugar)
VALUES
  ('7d4f5b91-2be7-476d-92a2-3ca63eaa9f54', 'Grilled Chicken Breast',
   'Skinless, boneless, cooked', 165, 31, 0, 1.2, 0, 0, 0),
  ('fcb0d064-5d6e-4b30-8f2c-2d5af4168b8e', 'Brown Rice',
   'Cooked, 1 cup', 216, 5, 45, 0.4, 0, 3.5, 0.7),
  ('c1ed0ad7-43ce-4f3e-a2cc-9e5904d9d3c4', 'Apple',
   'Medium, raw', 95, 0.5, 25, 0.1, 0, 4.4, 19);

INSERT INTO meals (id, userid, name, meal_type, date) VALUES
  ('5586a1c7-32e5-4c6a-b046-7132e3d5d933', 'u123',
   'Breakfast #1', 'MEAL_TYPE_BREAKFAST', '2023-04-06T14:42:27.169306700'),
  ('9b2eaa9b-6061-427c-96bb-3e1b10f09e7f', 'u123',
   'Lunch with colleagues', 'MEAL_TYPE_LUNCH', '2024-04-06T14:42:27.169306700'),
  ('18c2b040-9b57-4e9e-9e4b-cc9e79f03f1f', 'u456',
   'Post‑workout snack', 'MEAL_TYPE_SNACK', '2025-04-06T14:42:27.169306700');

```