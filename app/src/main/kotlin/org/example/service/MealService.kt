package org.example.service

import java.util.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.actions.IMealAction
import org.example.model.Meal

class MealService(private val actions: IMealAction) : IMealService {

  override suspend fun createMeal(meal: Meal): Meal {
    val toSave =
        meal.copy(
            id = UUID.randomUUID().toString(),
            date = Clock.System.now().toLocalDateTime(TimeZone.UTC))
    return actions.createMeal(toSave)
  }

  override suspend fun getMeal(id: String): Meal? = actions.getMeal(id)

  override suspend fun listMeals(
      userId: String,
      start: LocalDateTime,
      end: LocalDateTime
  ): List<Meal> = actions.listMeals(userId, start, end)

  override suspend fun deleteMeal(id: String): Boolean = actions.deleteMeal(id)
}
