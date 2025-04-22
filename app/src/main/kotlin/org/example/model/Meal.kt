package org.example.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * Enum для типов приема пищи
 *
 * @property MEAL_TYPE_UNSPECIFIED Неопределенный тип
 * @property MEAL_TYPE_BREAKFAST Завтрак
 * @property MEAL_TYPE_LUNCH Обед
 * @property MEAL_TYPE_DINNER Ужин
 * @property MEAL_TYPE_SNACK Перекус
 */
@Serializable
enum class MealType {
  MEAL_TYPE_UNSPECIFIED,
  MEAL_TYPE_BREAKFAST,
  MEAL_TYPE_LUNCH,
  MEAL_TYPE_DINNER,
  MEAL_TYPE_SNACK
}

/**
 * Data class, который описывает прием пищи
 *
 * @property id Идентификатор приема пищи
 * @property userId Идентификатор пользователя
 * @property name Название приема пищи
 * @property mealType Тип приема пищи, см [MealType]
 * @property foods Список пищи
 * @property date Дата приема пищи
 */
@Serializable
data class Meal(
    val id: String = "",
    val userId: String,
    val name: String,
    val mealType: MealType = MealType.MEAL_TYPE_UNSPECIFIED,
    val foods: List<Food>,
    val date: LocalDateTime
)
