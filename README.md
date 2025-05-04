# Diet service

## About

Default configuration for this microservice (env file):

```
PORT=8082

DB_MODE=LOCAL        # LOCAL or gateway
DB_HOST=localhost
DB_PORT=8081

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
LOGGER_ACTIVITY_CHANNEL=logger:activity
LOGGER_ERROR_CHANNEL=logger:error
```

Default port for this service is 8082. [App.kt](app/src/main/kotlin/org/example/App.kt)/

### Routes:

Foods Routes:

- GET diet/foods/{id} - Get food by ID
- GET diet/foods - List all foods. Filter is optional. Example: `diet/foods?name_filter=rice`
- POST diet/foods - Create a new food
- DELETE diet/foods/{id} - Delete food by ID

Meals Routes:

- GET diet/meals/{id} - Get meal by ID
- GET diet/meals?user_id={userId} - List all meals for a
  specific user. Time filter can be applied by adding `&start=2022-01-01T00:00&end=2023-12-31T23:59`
- POST diet/meals - Create a new meal
- DELETE diet/meals/{id} - Delete meal by ID

### Foods query examples

**Get by ID**

URL (body is empty):

```
GET http://localhost:8002/diet/foods/7d4f5b91-2be7-476d-92a2-3ca63eaa9f54
```

Response:

```json
{
  "id": "7d4f5b91-2be7-476d-92a2-3ca63eaa9f54",
  "name": "Grilled Chicken Breast",
  "description": "Skinless, boneless, cooked",
  "calories": 165.0,
  "protein": 31.0,
  "carbs": 0.0,
  "saturatedFats": 1.2,
  "transFats": 0.0,
  "fiber": 0.0,
  "sugar": 0.0,
  "vitamins": [
    {
      "id": "9cf38114-0147-4e16-afdss6d",
      "name": "d3",
      "amount": 12.0,
      "unit": "ml"
    }
  ],
  "minerals": [
    {
      "id": "9cfsd114-0147-4e16-a15b-389626d",
      "name": "amega",
      "amount": 13.0,
      "unit": "l"
    }
  ]
}
```

**Get list**

URL (body is empty):

```
GET http://localhost:8002/diet/foods
```

Response:

```json
[
  {
    "id": "7d4f5b91-2be7-476d-92a2-3ca63eaa9f54",
    "name": "Grilled Chicken Breast",
    "description": "Skinless, boneless, cooked",
    "calories": 165.0,
    "protein": 31.0,
    "carbs": 0.0,
    "saturatedFats": 1.2,
    "transFats": 0.0,
    "fiber": 0.0,
    "sugar": 0.0,
    "vitamins": [
      {
        "id": "9cf38114-0147-4e16-afdss6d",
        "name": "d3",
        "amount": 12.0,
        "unit": "ml"
      }
    ],
    "minerals": [
      {
        "id": "9cfsd114-0147-4e16-a15b-389626d",
        "name": "amega",
        "amount": 13.0,
        "unit": "l"
      }
    ]
  },
  {
    "id": "fcb0d064-5d6e-4b30-8f2c-2d5af4168b8e",
    "name": "Brown Rice",
    "description": "Cooked, 1 cup",
    "calories": 216.0,
    "protein": 5.0,
    "carbs": 45.0,
    "saturatedFats": 0.4,
    "transFats": 0.0,
    "fiber": 3.5,
    "sugar": 0.7,
    "vitamins": [
      {
        "id": "9cf38114-0147-4e16-afdss6d",
        "name": "d3",
        "amount": 12.0,
        "unit": "ml"
      }
    ],
    "minerals": [
      {
        "id": "9cfsd114-0147-4e16-a15b-389626d",
        "name": "amega",
        "amount": 13.0,
        "unit": "l"
      }
    ]
  },
  {
    "id": "6fbf5984-34e0-45d2-890e-d11d3abc9168",
    "name": "Greek Yogurt",
    "description": "Plain, non‑fat",
    "calories": 59.0,
    "protein": 10.0,
    "carbs": 3.6,
    "saturatedFats": 0.1,
    "transFats": 0.0,
    "fiber": 0.0,
    "sugar": 3.2,
    "vitamins": [
      {
        "id": "9cf38114-0147-4e16-afdss6d",
        "name": "d3",
        "amount": 12.0,
        "unit": "ml"
      }
    ],
    "minerals": [
      {
        "id": "9cfsd114-0147-4e16-a15b-389626d",
        "name": "amega",
        "amount": 13.0,
        "unit": "l"
      }
    ]
  }
]
```

**Create food**

URL:

```
POST http://localhost:8002/diet/foods
```

Body:

```json
{
  "name": "Greek Yogurt1",
  "description": "Plain, non‑fat",
  "calories": 59.0,
  "protein": 10.0,
  "carbs": 3.6,
  "saturatedFats": 0.1,
  "transFats": 0.0,
  "fiber": 0.0,
  "sugar": 3.2,
  "vitamins": [
    {
      "id": "9cf38114-0147-4e16-afdss6d",
      "name": "d3",
      "amount": 12.0,
      "unit": "ml"
    }
  ],
  "minerals": [
    {
      "id": "9cfsd114-0147-4e16-a15b-389626d",
      "name": "amega",
      "amount": 13.0,
      "unit": "l"
    }
  ]
}
```

Response:

```json
{
  "id": "7a1751db-91ac-4556-b8bb-d71d91bfcae6",
  "name": "Greek Yogurt1",
  "description": "Plain, non‑fat",
  "calories": 59.0,
  "protein": 10.0,
  "carbs": 3.6,
  "saturatedFats": 0.1,
  "transFats": 0.0,
  "fiber": 0.0,
  "sugar": 3.2,
  "vitamins": [
    {
      "id": "9cf38114-0147-4e16-afdss6d",
      "name": "d3",
      "amount": 12.0,
      "unit": "ml"
    }
  ],
  "minerals": [
    {
      "id": "9cfsd114-0147-4e16-a15b-389626d",
      "name": "amega",
      "amount": 13.0,
      "unit": "l"
    }
  ]
}
```

**Delete food**

URL:

```
DELETE http://localhost:8002/diet/foods/d066ee17-5a78-47b0-a6fc-a50846248663
```

Response:

```json
{
  "status": "success",
  "message": "Food deleted"
}
```

### Meals query examples

**Get by ID**

URL (body is empty):

```
GET http://localhost:8002/diet/meals/5586a1c7-32e5-4c6a-b046-7132e3d5d933
```

Response:

```json
{
  "id": "5586a1c7-32e5-4c6a-b046-7132e3d5d933",
  "userId": "u123",
  "name": "Breakfast #1",
  "mealType": "MEAL_TYPE_BREAKFAST",
  "foods": [],
  "date": "2023-04-06T14:42:27.169306700"
}
```

**Get list**

URL (body is empty):

```
GET http://localhost:8002/diet/meals?user_id=u123&start=2022-01-01T00:00&end=2023-12-31T23:59
```

Response:

```json
[
  {
    "id": "9b2eaa9b-6061-427c-96bb-3e1b10f09e7f",
    "userId": "u123",
    "name": "Lunch with colleagues",
    "mealType": "MEAL_TYPE_LUNCH",
    "foods": [],
    "date": "2024-04-06T14:42:27.169306700"
  },
  {
    "id": "86b970cc-4151-4f59-9986-b785ee953d8b",
    "userId": "u123",
    "name": "Lunch combo",
    "mealType": "MEAL_TYPE_LUNCH",
    "foods": [
      {
        "id": "7d4f5b91-2be7-476d-92a2-3ca63eaa9f54",
        "name": "",
        "description": "",
        "calories": 0.0,
        "protein": 0.0,
        "carbs": 0.0,
        "saturatedFats": 0.0,
        "transFats": 0.0,
        "fiber": 0.0,
        "sugar": 0.0,
        "vitamins": [],
        "minerals": []
      },
      {
        "id": "fcb0d064-5d6e-4b30-8f2c-2d5af4168b8e",
        "name": "",
        "description": "",
        "calories": 0.0,
        "protein": 0.0,
        "carbs": 0.0,
        "saturatedFats": 0.0,
        "transFats": 0.0,
        "fiber": 0.0,
        "sugar": 0.0,
        "vitamins": [],
        "minerals": []
      }
    ],
    "date": "2025-04-20T12:11:57.981714400"
  }
]
```

**Create meal**
URL:

```
POST http://localhost:8002/diet/meals
```

Body:

```json
{
  "userId": "u123",
  "name": "Lunch combo1",
  "mealType": "MEAL_TYPE_LUNCH",
  "foods": [
    { "id": "61e8505b-15a9-45b6-b44a-3c31cb08f393" },
    { "id": "7a1751db-91ac-4556-b8bb-d71d91bfcae6" }
  ],
  "date": "2025-04-20T12:00:00"
}
```

Response:

```json
{
  "id": "e7abbd8c-6c7c-42f5-a5ef-46752c22b54e",
  "userId": "u123",
  "name": "Lunch combo1",
  "mealType": "MEAL_TYPE_LUNCH",
  "foods": [
    {
      "id": "61e8505b-15a9-45b6-b44a-3c31cb08f393",
      "name": "Aboba",
      "description": "Plain, non‑fat",
      "calories": 59.0,
      "protein": 10.0,
      "carbs": 3.6,
      "saturatedFats": 0.1,
      "transFats": 0.0,
      "fiber": 0.0,
      "sugar": 3.2,
      "vitamins": [
        {
          "id": "9cf38114-0147-4e16-afdss6d",
          "name": "d3",
          "amount": 12.0,
          "unit": "ml"
        }
      ],
      "minerals": [
        {
          "id": "9cfsd114-0147-4e16-a15b-389626d",
          "name": "amega",
          "amount": 13.0,
          "unit": "l"
        }
      ]
    },
    {
      "id": "7a1751db-91ac-4556-b8bb-d71d91bfcae6",
      "name": "Greek Yogurt1",
      "description": "Plain, non‑fat",
      "calories": 59.0,
      "protein": 10.0,
      "carbs": 3.6,
      "saturatedFats": 0.1,
      "transFats": 0.0,
      "fiber": 0.0,
      "sugar": 3.2,
      "vitamins": [
        {
          "id": "9cf38114-0147-4e16-afdss6d",
          "name": "d3",
          "amount": 12.0,
          "unit": "ml"
        }
      ],
      "minerals": [
        {
          "id": "9cfsd114-0147-4e16-a15b-389626d",
          "name": "amega",
          "amount": 13.0,
          "unit": "l"
        }
      ]
    }
  ],
  "date": "2025-05-01T11:09:54.949956200"
}
```

**Delete meal**
URL (Body is empty):

```
DELETE http://localhost:8002/diet/meals/01c0825a-c7ee-425f-af8a-960a17bfcfa4
```

Response:

```json
{
  "status": "success",
  "message": "Meal deleted"
}
```

## Foods

Foods sections of this service has the following actions:

- Create Food (Food format)
- Get Food (by providing the food ID)
- List Foods (list all foods by providing naming filter or without it)
- Delete Food (delete a specific food by providing the note ID)

Food format:

```proto
message Vitamin {
  string id = 1;
  string name = 2;
  double amount = 3;
  string unit = 4;
}

message VitaminFood {
  string id = 1;
  repeated Vitamin vitamins = 2;
}

message Mineral {
  string id = 1;
  string name = 2;
  double amount = 3;
  string unit = 4;
}

message Food {
  string id = 1;
  string name = 2;
  string description = 3;
  double calories = 4;
  double protein = 5;
  double carbs = 6;
  double saturated_fats = 7;
  double trans_fats = 8;
  double fiber = 9;
  double sugar = 10;
  repeated Vitamin vitamins = 11;
  repeated Mineral minerals = 12;
}
```

Actions for foods:

```proto
service DietService {
  rpc CreateFood(CreateFoodRequest) returns (Food);
  rpc GetFood(GetFoodRequest) returns (Food);
  rpc ListFoods(ListFoodsRequest) returns (ListFoodsResponse);
}

message CreateFoodRequest {
  Food food = 1;
}

message GetFoodRequest {
  string id = 1;
}

message ListFoodsRequest {
  string name_filter = 1;
}

message ListFoodsResponse {
  repeated Food foods = 1;
}
```

## Meals

```proto
enum MealType {
  MEAL_TYPE_UNSPECIFIED = 0;
  MEAL_TYPE_BREAKFAST = 1;
  MEAL_TYPE_LUNCH = 2;
  MEAL_TYPE_DINNER = 3;
  MEAL_TYPE_SNACK = 4;
}

message Meal {
  string id = 1;
  string name = 2;
  MealType meal_type = 3;
  repeated Food foods = 4;
  google.protobuf.Timestamp date = 5;
}
```

Actions for meals:

```proto
service DietService {
  rpc CreateMeal(CreateMealRequest) returns (Meal);
  rpc GetMeal(GetMealRequest) returns (Meal);
  rpc ListMeals(ListMealsRequest) returns (ListMealsResponse);
}

message CreateMealRequest {
  Meal meal = 1;
}

message GetMealRequest {
  string id = 1;
}

message ListMealsRequest {
  google.protobuf.Timestamp start_date = 1;
  google.protobuf.Timestamp end_date = 2;
}

message ListMealsResponse {
  repeated Meal meals = 1;
}
```

## SQL

Create Foods table:

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
```

Create Meals table:

```sql
CREATE TABLE meals (
id         varchar(36) PRIMARY KEY,
userid     TEXT    NOT NULL,
name       TEXT    NOT NULL,
meal_type  TEXT    NOT NULL,
date       varchar(255) NOT NULL
);
```

Create Vitamins table:
```sql
CREATE TABLE vitamins (
    id    VARCHAR(36) PRIMARY KEY,
    name  TEXT NOT NULL,
    unit  TEXT NOT NULL
);
```

Create Minerals table:

```sql
CREATE TABLE minerals (
    id    VARCHAR(36) PRIMARY KEY,
    name  TEXT NOT NULL,
    unit  TEXT NOT NULL
);
```

Create linking tables:

```sql
CREATE TABLE meal_foods (
    meal_id varchar(36) REFERENCES meals(id) ON DELETE CASCADE,
    food_id varchar(36) REFERENCES foods(id) ON DELETE CASCADE,
    PRIMARY KEY (meal_id, food_id)
);
```

```sql
CREATE TABLE food_minerals (
    food_id     VARCHAR(36) REFERENCES foods(id) ON DELETE CASCADE,
    mineral_id  VARCHAR(36) REFERENCES minerals(id) ON DELETE CASCADE,
    amount      DOUBLE PRECISION NOT NULL CHECK (amount >= 0),
    PRIMARY KEY (food_id, mineral_id)
);
```

```sql
CREATE TABLE food_vitamins (
    food_id    VARCHAR(36) REFERENCES foods(id) ON DELETE CASCADE,
    vitamin_id VARCHAR(36) REFERENCES vitamins(id) ON DELETE CASCADE,
    amount     DOUBLE PRECISION NOT NULL CHECK (amount >= 0),
    PRIMARY KEY (food_id, vitamin_id)
);
```