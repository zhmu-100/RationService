package org.example.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.example.model.MealType

/**
 * Строка в БД для блюда
 *
 * @property id Идентификатор блюда
 * @property name Название блюда
 * @meal_type Тип блюда, см [MealType]
 */
@Serializable
data class DbMealRow(
    val id: String,
    val userid: String,
    val name: String,
    val meal_type: String,
    val date: String
)

/**
 * Строка в БД для пищи
 *
 * @property id Идентификатор пищи
 * @property name Название пищи
 * @property description Описание пищи
 * @property calories Калории
 * @property protein Белки
 * @property carbs Углеводы
 * @property saturated_fats Насыщенные жиры
 * @property trans_fats Транс ждиры
 * @property fiber Клетчатка
 * @property sugar Сахар
 */
@Serializable
data class DbFoodRow(
    val id: String,
    val name: String,
    val description: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val saturated_fats: Double,
    val trans_fats: Double,
    val fiber: Double,
    val sugar: Double
)

/**
 * Запрос на создание блюда
 *
 * @property userId Идентификатор пользователя.
 * @property name Название блюда.
 * @property mealType Тип блюда, см [MealType].
 * @property foods Список идентификаторов пищи.
 * @property date Дата приема.
 */
@Serializable
data class CreateMealRequest(
    val userId: String,
    val name: String,
    val mealType: MealType = MealType.MEAL_TYPE_UNSPECIFIED,
    val foods: List<FoodRef>,
    val date: LocalDateTime? = null
)

/**
 * Ссылка на пищу
 *
 * @property id Идентификатор
 */
@Serializable data class FoodRef(val id: String)
