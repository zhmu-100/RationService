package org.example.router

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.dto.CreateMealRequest
import org.example.logging.LoggerProvider
import org.example.model.Meal
import org.example.service.IFoodService
import org.example.service.IMealService

/**
 * REST роутер для работы с едой
 *
 * Эндпоинты:
 * - GET diet/meals/{id} - Get meal by ID
 * - GET diet/meals?user_id={userId} - List all meals for a specific user. Time filter can be
 * applied by adding `&start=2022-01-01T00:00&end=2023-12-31T23:59`
 * - POST diet/meals - Create a new meal
 * - DELETE diet/meals/{id} - Delete meal by ID
 */
fun Application.registerMealRoutes(mealService: IMealService, foodService: IFoodService) {
  val logger = LoggerProvider.logger

  routing {
    route("/diet/meals") {
      /** Создание нового приема пищи */
      post {
        logger.logActivity("API: Запрос на создание приема пищи")

        try {
          val req = call.receive<CreateMealRequest>()

          logger.logActivity(
              "API: Получены данные для создания приема пищи",
              additionalData =
                  mapOf(
                      "userId" to req.userId,
                      "name" to req.name,
                      "mealType" to req.mealType.name,
                      "foodsCount" to req.foods.size.toString()))

          // Получаем еду по ID
          val foods =
              req.foods.mapNotNull { ref ->
                val food = foodService.getFood(ref.id)
                if (food == null) {
                  logger.logActivity(
                      "API: Еда не найдена при создании приема пищи",
                      additionalData = mapOf("foodId" to ref.id))
                }
                food
              }

          logger.logActivity(
              "API: Получена еда для приема пищи",
              additionalData =
                  mapOf(
                      "requestedFoodsCount" to req.foods.size.toString(),
                      "foundFoodsCount" to foods.size.toString()))

          val meal =
              Meal(
                  id = "",
                  userId = req.userId,
                  name = req.name,
                  mealType = req.mealType,
                  foods = foods,
                  date = req.date ?: Clock.System.now().toLocalDateTime(TimeZone.UTC))

          val created = mealService.createMeal(meal)

          logger.logActivity(
              "API: Прием пищи успешно создан",
              additionalData =
                  mapOf("id" to created.id, "userId" to created.userId, "name" to created.name))

          call.respond(HttpStatusCode.Created, created)
        } catch (e: Exception) {
          logger.logError(
              "API: Ошибка при создании приема пищи",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

      /** Получить прием пищи по ID */
      get("/{id}") {
        val id = call.parameters["id"]

        if (id == null) {
          logger.logActivity("API: Ошибка запроса на получение приема пищи - отсутствует ID")
          call.respond(
              HttpStatusCode.BadRequest, mapOf("status" to "error", "message" to "Missing id"))
          return@get
        }

        logger.logActivity(
            "API: Запрос на получение приема пищи", additionalData = mapOf("id" to id))

        try {
          val meal = mealService.getMeal(id)

          if (meal == null) {
            logger.logActivity("API: Прием пищи не найден", additionalData = mapOf("id" to id))
            call.respond(
                HttpStatusCode.NotFound, mapOf("status" to "error", "message" to "Meal not found"))
            return@get
          }

          logger.logActivity(
              "API: Прием пищи успешно получен",
              additionalData = mapOf("id" to id, "userId" to meal.userId, "name" to meal.name))

          call.respond(meal)
        } catch (e: Exception) {
          logger.logError(
              "API: Ошибка при получении приема пищи: id=$id",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

      /** Получает список всех приемов пищи для пользователя с учетом фильтрации по дате */
      get {
        val userId = call.request.queryParameters["user_id"]

        if (userId.isNullOrBlank()) {
          logger.logActivity("API: Ошибка запроса на получение приемов пищи - отсутствует user_id")
          call.respond(
              HttpStatusCode.BadRequest,
              mapOf("status" to "error", "message" to "user_id required"))
          return@get
        }

        val start =
            call.request.queryParameters["start"]?.let(LocalDateTime::parse)
                ?: LocalDateTime.parse("1970-01-01T00:00")
        val end =
            call.request.queryParameters["end"]?.let(LocalDateTime::parse)
                ?: LocalDateTime.parse("3000-01-01T00:00")

        logger.logActivity(
            "API: Запрос на получение списка приемов пищи",
            additionalData =
                mapOf("userId" to userId, "start" to start.toString(), "end" to end.toString()))

        try {
          val meals = mealService.listMeals(userId, start, end)

          logger.logActivity(
              "API: Список приемов пищи успешно получен",
              additionalData = mapOf("userId" to userId, "count" to meals.size.toString()))

          call.respond(meals)
        } catch (e: Exception) {
          logger.logError(
              "API: Ошибка при получении списка приемов пищи: userId=$userId, start=$start, end=$end",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

      /**
       * Удаление приема пищи
       *
       * ID приема пищи передается в URL
       */
      delete("/{id}") {
        val id = call.parameters["id"]

        if (id == null) {
          logger.logActivity("API: Ошибка запроса на удаление приема пищи - отсутствует ID")
          call.respond(
              HttpStatusCode.BadRequest, mapOf("status" to "error", "message" to "Missing id"))
          return@delete
        }

        logger.logActivity(
            "API: Запрос на удаление приема пищи", additionalData = mapOf("id" to id))

        try {
          val success = mealService.deleteMeal(id)

          if (success) {
            logger.logActivity("API: Прием пищи успешно удален", additionalData = mapOf("id" to id))
            call.respond(
                HttpStatusCode.OK, mapOf("status" to "success", "message" to "Meal deleted"))
          } else {
            logger.logActivity(
                "API: Прием пищи не найден при удалении", additionalData = mapOf("id" to id))
            call.respond(
                HttpStatusCode.NotFound, mapOf("status" to "error", "message" to "Meal not found"))
          }
        } catch (e: Exception) {
          logger.logError(
              "API: Ошибка при удалении приема пищи: id=$id",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }
    }
  }
}
