package org.example.service

import org.example.model.Food

interface IFoodService {
  suspend fun createFood(food: Food): Food
  suspend fun getFood(id: String): Food?
  suspend fun listFoods(nameFilter: String?): List<Food>
  suspend fun deleteFood(id: String): Boolean
}
