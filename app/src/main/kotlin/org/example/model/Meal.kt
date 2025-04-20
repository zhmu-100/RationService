package org.example.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
enum class MealType {
  MEAL_TYPE_UNSPECIFIED,
  MEAL_TYPE_BREAKFAST,
  MEAL_TYPE_LUNCH,
  MEAL_TYPE_DINNER,
  MEAL_TYPE_SNACK
}

@Serializable
data class Meal(
    val id: String = "",
    val userId: String,
    val name: String,
    val mealType: MealType = MealType.MEAL_TYPE_UNSPECIFIED,
    val foods: List<Food>,
    val date: LocalDateTime
)
