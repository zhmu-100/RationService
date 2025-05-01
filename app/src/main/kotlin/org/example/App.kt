package org.example

import io.github.cdimascio.dotenv.dotenv
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import org.example.actions.FoodAction
import org.example.actions.MealAction
import org.example.router.registerFoodRoutes
import org.example.router.registerMealRoutes
import org.example.service.FoodService
import org.example.service.MealService

/** Точка входа в приложение */
fun main() {
  val dotenv = dotenv()
  val port = dotenv["PORT"]?.toIntOrNull() ?: 8001
  embeddedServer(Netty, port = port) {
        install(ContentNegotiation) { json() }

        val mealAction = MealAction()
        val mealService = MealService(mealAction)

        val foodAction = FoodAction()
        val foodService = FoodService(foodAction)

        registerMealRoutes(mealService, foodService)
        registerFoodRoutes(foodService)
      }
      .start(wait = true)
}
