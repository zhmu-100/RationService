package org.example.actions

import org.example.model.Food

interface IFoodAction {
  suspend fun createFood(food: Food): Food
  suspend fun getFood(id: String): Food?
  suspend fun listFoods(nameFilter: String?): List<Food>
  suspend fun deleteFood(id: String): Boolean
}
