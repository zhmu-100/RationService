package org.example.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.example.model.MealType

@Serializable
data class DbMealRow(
    val id: String,
    val userid: String,
    val name: String,
    val meal_type: String,
    val date: String
)

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

@Serializable
data class CreateMealRequest(
    val userId: String,
    val name: String,
    val mealType: MealType = MealType.MEAL_TYPE_UNSPECIFIED,
    val foods: List<FoodRef>,
    val date: LocalDateTime? = null
)

@Serializable data class FoodRef(val id: String)
