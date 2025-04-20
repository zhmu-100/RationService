package org.example.service

import java.util.UUID
import org.example.actions.IFoodAction
import org.example.model.Food

class FoodService(private val actions: IFoodAction) : IFoodService {

  override suspend fun createFood(food: Food): Food {
    val newFood = food.copy(id = UUID.randomUUID().toString())
    return actions.createFood(newFood)
  }

  override suspend fun getFood(id: String): Food? = actions.getFood(id)

  override suspend fun listFoods(nameFilter: String?): List<Food> = actions.listFoods(nameFilter)

  override suspend fun deleteFood(id: String): Boolean = actions.deleteFood(id)
}
