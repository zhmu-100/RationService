package org.example.router

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.logging.LoggerProvider
import org.example.model.Food
import org.example.service.IFoodService

/**
 * REST роутер для работы с едой
 *
 * Эндпоинты:
 * - GET diet/foods/{id} - Get food by ID
 * - GET diet/foods - List all foods. Filter is optional. Example: `diet/foods?name_filter=rice`
 * - POST diet/foods - Create a new food
 * - DELETE diet/foods/{id} - Delete food by ID
 */
fun Application.registerFoodRoutes(foodService: IFoodService) {
  val logger = LoggerProvider.logger

  routing {
    route("/diet/foods") {
      /** Создание новой еды */
      post {
        logger.logActivity("API: Запрос на создание еды")

        try {
          val food = call.receive<Food>()

          logger.logActivity(
              "API: Получены данные для создания еды",
              additionalData =
                  mapOf(
                      "name" to food.name,
                      "vitaminsCount" to food.vitamins.size.toString(),
                      "mineralsCount" to food.minerals.size.toString()))

          val created = foodService.createFood(food)

          logger.logActivity(
              "API: Еда успешно создана",
              additionalData = mapOf("id" to created.id, "name" to created.name))

          call.respond(created)
        } catch (e: Exception) {
          logger.logError(
              "API: Ошибка при создании еды",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

      /** Получение еды по ID */
      get("{id}") {
        val id = call.parameters["id"]

        if (id == null) {
          logger.logActivity("API: Ошибка запроса на получение еды - отсутствует ID")
          call.respond(HttpStatusCode.BadRequest)
          return@get
        }

        logger.logActivity("API: Запрос на получение еды", additionalData = mapOf("id" to id))

        try {
          val food = foodService.getFood(id)

          if (food == null) {
            logger.logActivity("API: Еда не найдена", additionalData = mapOf("id" to id))
            call.respond(HttpStatusCode.NotFound)
            return@get
          }

          logger.logActivity(
              "API: Еда успешно получена", additionalData = mapOf("id" to id, "name" to food.name))

          call.respond(food)
        } catch (e: Exception) {
          logger.logError(
              "API: Ошибка при получении еды: id=$id",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

      /**
       * Получение списка всех еды с фильтрацией по имени
       *
       * Параметры запроса:
       * - name_filter - Фильтр по имени еды (необязательный)
       */
      get {
        val filter = call.request.queryParameters["name_filter"]

        logger.logActivity(
            "API: Запрос на получение списка еды",
            additionalData = mapOf("nameFilter" to (filter ?: "null")))

        try {
          val foods = foodService.listFoods(filter)

          logger.logActivity(
              "API: Список еды успешно получен",
              additionalData = mapOf("count" to foods.size.toString()))

          call.respond(foods)
        } catch (e: Exception) {
          logger.logError(
              "API: Ошибка при получении списка еды: nameFilter=${filter ?: "null"}",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

      /** Удаление еды по ID */
      delete("{id}") {
        val id = call.parameters["id"]

        if (id == null) {
          logger.logActivity("API: Ошибка запроса на удаление еды - отсутствует ID")
          call.respond(
              HttpStatusCode.BadRequest,
              mapOf("status" to "error", "message" to "Missing id parameter"))
          return@delete
        }

        logger.logActivity("API: Запрос на удаление еды", additionalData = mapOf("id" to id))

        try {
          val ok = foodService.deleteFood(id)

          if (ok) {
            logger.logActivity("API: Еда успешно удалена", additionalData = mapOf("id" to id))
            call.respond(
                HttpStatusCode.OK, mapOf("status" to "success", "message" to "Food deleted"))
          } else {
            logger.logActivity(
                "API: Еда не найдена при удалении", additionalData = mapOf("id" to id))
            call.respond(
                HttpStatusCode.NotFound, mapOf("status" to "error", "message" to "Food not found"))
          }
        } catch (e: Exception) {
          logger.logError(
              "API: Ошибка при удалении еды: id=$id",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }
    }
  }
}
