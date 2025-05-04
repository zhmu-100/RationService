package org.example

import io.github.cdimascio.dotenv.dotenv
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import org.example.actions.FoodAction
import org.example.actions.MealAction
import org.example.logging.LoggerProvider
import org.example.router.registerFoodRoutes
import org.example.router.registerMealRoutes
import org.example.service.FoodService
import org.example.service.MealService

/** Точка входа в приложение */
fun main() {
  val logger = LoggerProvider.logger
  val dotenv = dotenv()
  val port = dotenv["PORT"]?.toIntOrNull() ?: 8001

  logger.logActivity("Запуск Diet Service")

  try {
    embeddedServer(Netty, port = port) {
          logger.logActivity("Настройка модулей приложения")

          install(ContentNegotiation) {
            json()
            logger.logActivity("Content negotiation настроен")
          }

          val mealAction = MealAction()
          val mealService = MealService(mealAction)
          logger.logActivity("MealService инициализирован")

          val foodAction = FoodAction()
          val foodService = FoodService(foodAction)
          logger.logActivity("FoodService инициализирован")

          registerMealRoutes(mealService, foodService)
          registerFoodRoutes(foodService)
          logger.logActivity("Маршруты настроены")

          environment.monitor.subscribe(ApplicationStopped) {
            logger.logActivity("Остановка Diet Service")
            logger.close()
          }
        }
        .start(wait = true)

    logger.logActivity("Diet Service успешно запущен")
  } catch (e: Exception) {
    logger.logError(
        "Ошибка при запуске Diet Service",
        errorMessage = e.message ?: "Неизвестная ошибка",
        stackTrace = e.stackTraceToString())
    throw e
  }
}
