package org.example.service

import kotlinx.datetime.LocalDateTime
import org.example.model.Meal

interface IMealService {
  suspend fun createMeal(meal: Meal): Meal
  suspend fun getMeal(id: String): Meal?
  suspend fun listMeals(userId: String, start: LocalDateTime, end: LocalDateTime): List<Meal>
  suspend fun deleteMeal(id: String): Boolean
}
