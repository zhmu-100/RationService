package org.example

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

/**
 * Точка входа в приложение
 *
 * Дефолтный порт - 8002, см в. [application.conf]
 */
fun main() {
  embeddedServer(Netty, port = 8002) {
        install(ContentNegotiation) { json() }

        val mealAction = MealAction(environment.config)
        val mealService = MealService(mealAction)
        registerMealRoutes(mealService)

        val foodAction = FoodAction(environment.config)
        val foodService = FoodService(foodAction)
        registerFoodRoutes(foodService)
      }
      .start(wait = true)
}
