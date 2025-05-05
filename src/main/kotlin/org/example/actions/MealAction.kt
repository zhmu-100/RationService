package org.example.actions

import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.example.dto.*
import org.example.logging.LoggerProvider
import org.example.model.Food
import org.example.model.Meal
import org.example.model.MealType

/**
 * Реализация интерфейса [IMealAction]. Может работать с локальной БД или через API Gateway
 *
 * @see IMealAction
 */
class MealAction : IMealAction {
  private val logger = LoggerProvider.logger
  private val foodAction = FoodAction()

  private val dotenv = dotenv()
  private val dbMode = dotenv["DB_MODE"] ?: "LOCAL"
  private val dbHost = dotenv["DB_HOST"] ?: "localhost"
  private val dbPort = dotenv["DB_PORT"] ?: "8080"
  private val baseUrl =
      if (dbMode.equals("gateway", true)) {
        "http://$dbHost:$dbPort/api/db"
      } else {
        "http://$dbHost:$dbPort"
      }

  private val httpClient = HttpClient { install(ContentNegotiation) { json() } }

  /**
   * Создает новый прием пищи
   *
   * @param meal Прием пищи для сохранения
   * @return Сохраненный прием пищи
   * @throws Exception если создание не удалось
   */
  override suspend fun createMeal(meal: Meal): Meal =
      withContext(Dispatchers.IO) {
        logger.logActivity(
            "Создание приема пищи",
            additionalData =
                mapOf(
                    "id" to meal.id,
                    "userId" to meal.userId,
                    "name" to meal.name,
                    "mealType" to meal.mealType.name,
                    "foodsCount" to meal.foods.size.toString()))

        try {
          val createMealReq =
              DbCreateRequest(
                  table = "meals",
                  data =
                      mapOf(
                          "id" to meal.id,
                          "userid" to meal.userId,
                          "name" to meal.name,
                          "meal_type" to meal.mealType.name,
                          "date" to meal.date.toString()))

          val mealResp =
              httpClient
                  .post("$baseUrl/create") {
                    contentType(ContentType.Application.Json)
                    setBody(createMealReq)
                  }
                  .body<DbResponse>()

          if (mealResp.success != true) {
            logger.logError(
                "Ошибка при создании приема пищи: id=${meal.id}, userId=${meal.userId}",
                errorMessage = mealResp.error ?: "Неизвестная ошибка")
            throw RuntimeException("Failed to create meal: ${mealResp.error}")
          }

          logger.logActivity(
              "Прием пищи успешно создан, добавление связей с едой",
              additionalData = mapOf("id" to meal.id, "userId" to meal.userId))

          // Связываем прием пищи с едой
          meal.foods.forEach { food ->
            logger.logActivity(
                "Добавление связи с едой",
                additionalData =
                    mapOf("mealId" to meal.id, "foodId" to food.id, "foodName" to food.name))

            val linkReq =
                DbCreateRequest(
                    table = "meal_foods", data = mapOf("meal_id" to meal.id, "food_id" to food.id))

            val linkResp =
                httpClient
                    .post("$baseUrl/create") {
                      contentType(ContentType.Application.Json)
                      setBody(linkReq)
                    }
                    .body<DbResponse>()

            if (linkResp.success != true) {
              logger.logError(
                  "Ошибка при связывании приема пищи с едой: mealId=${meal.id}, foodId=${food.id}",
                  errorMessage = linkResp.error ?: "Неизвестная ошибка")
              throw RuntimeException("Failed to link food ${food.id}: ${linkResp.error}")
            }

            logger.logActivity(
                "Связь с едой успешно добавлена",
                additionalData = mapOf("mealId" to meal.id, "foodId" to food.id))
          }

          logger.logActivity(
              "Прием пищи успешно создан со всеми связями",
              additionalData =
                  mapOf(
                      "id" to meal.id,
                      "userId" to meal.userId,
                      "foodsCount" to meal.foods.size.toString()))

          meal
        } catch (e: Exception) {
          logger.logError(
              "Ошибка при создании приема пищи: id=${meal.id}, userId=${meal.userId}",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

  /**
   * Получает прием пищи по его ID
   *
   * @param id ID приема пищи
   * @return Прием пищи или null, если он не найден
   */
  override suspend fun getMeal(id: String): Meal? =
      withContext(Dispatchers.IO) {
        logger.logActivity("Получение приема пищи по ID", additionalData = mapOf("id" to id))

        try {
          val mealRows: List<DbMealRow> =
              httpClient
                  .post("$baseUrl/read") {
                    contentType(ContentType.Application.Json)
                    setBody(DbReadRequest("meals", filters = mapOf("id" to id)))
                  }
                  .body()

          val mealRow = mealRows.firstOrNull()

          if (mealRow == null) {
            logger.logActivity("Прием пищи не найден", additionalData = mapOf("id" to id))
            return@withContext null
          }

          logger.logActivity(
              "Прием пищи найден, получение связанной еды",
              additionalData =
                  mapOf("id" to id, "userId" to mealRow.userid, "name" to mealRow.name))

          val baseMeal = mealRow.toModel()

          // Получаем связи с едой
          val links: List<JsonObject> =
              httpClient
                  .post("$baseUrl/read") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        DbReadRequest(
                            "meal_foods",
                            columns = listOf("food_id"),
                            filters = mapOf("meal_id" to id)))
                  }
                  .body<List<JsonObject>>()

          val foodIds: List<String> = links.mapNotNull { it["food_id"]?.jsonPrimitive?.content }

          logger.logActivity(
              "Получены связи с едой",
              additionalData = mapOf("id" to id, "foodIdsCount" to foodIds.size.toString()))

          // Получаем еду по ID
          val foods = mutableListOf<Food>()
          for (fid in foodIds) {
            logger.logActivity(
                "Получение еды для приема пищи",
                additionalData = mapOf("mealId" to id, "foodId" to fid))

            foodAction.getFood(fid)?.let {
              foods += it
              logger.logActivity(
                  "Еда добавлена к приему пищи",
                  additionalData = mapOf("mealId" to id, "foodId" to fid, "foodName" to it.name))
            }
          }

          val result = baseMeal.copy(foods = foods)

          logger.logActivity(
              "Прием пищи успешно получен со всеми связями",
              additionalData =
                  mapOf(
                      "id" to id,
                      "userId" to result.userId,
                      "name" to result.name,
                      "foodsCount" to foods.size.toString()))

          result
        } catch (e: Exception) {
          logger.logError(
              "Ошибка при получении приема пищи: id=$id",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

  /**
   * Получает список всех приемов пищи для пользователя с учетом фильтрации по дате
   *
   * @param userId ID пользователя
   * @param start Начало диапазона дат
   * @param end Конец диапазона дат
   * @return List приемов пищи
   */
  override suspend fun listMeals(
      userId: String,
      start: LocalDateTime,
      end: LocalDateTime
  ): List<Meal> =
      withContext(Dispatchers.IO) {
        logger.logActivity(
            "Получение списка приемов пищи",
            additionalData =
                mapOf("userId" to userId, "start" to start.toString(), "end" to end.toString()))

        try {
          val rows: List<DbMealRow> =
              httpClient
                  .post("$baseUrl/read") {
                    contentType(ContentType.Application.Json)
                    setBody(DbReadRequest("meals", filters = mapOf("userid" to userId)))
                  }
                  .body()

          logger.logActivity(
              "Получены базовые данные о приемах пищи",
              additionalData = mapOf("userId" to userId, "count" to rows.size.toString()))

          val meals =
              rows
                  .map { row ->
                    val base = row.toModel()

                    logger.logActivity(
                        "Получение связей с едой для приема пищи",
                        additionalData =
                            mapOf("mealId" to row.id, "userId" to userId, "name" to row.name))

                    // Получаем связи с едой
                    val links: List<JsonObject> =
                        httpClient
                            .post("$baseUrl/read") {
                              contentType(ContentType.Application.Json)
                              setBody(
                                  DbReadRequest(
                                      table = "meal_foods",
                                      columns = listOf("food_id"),
                                      filters = mapOf("meal_id" to row.id)))
                            }
                            .body()

                    val foodIds = links.mapNotNull { it["food_id"]?.jsonPrimitive?.content }

                    logger.logActivity(
                        "Получены связи с едой для приема пищи",
                        additionalData =
                            mapOf("mealId" to row.id, "foodIdsCount" to foodIds.size.toString()))

                    // Получаем еду по ID
                    val foods = mutableListOf<Food>()
                    for (fid in foodIds) {
                      foodAction.getFood(fid)?.let { foods += it }
                    }

                    base.copy(foods = foods)
                  }
                  .filter { it.date >= start && it.date <= end }

          logger.logActivity(
              "Список приемов пищи успешно получен",
              additionalData =
                  mapOf(
                      "userId" to userId,
                      "totalCount" to rows.size.toString(),
                      "filteredCount" to meals.size.toString()))

          meals
        } catch (e: Exception) {
          logger.logError(
              "Ошибка при получении списка приемов пищи: userId=$userId, start=$start, end=$end",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

  /**
   * Удаляет прием пищи
   *
   * @param id ID приема пищи
   * @return true, если удален, иначе false
   */
  override suspend fun deleteMeal(id: String): Boolean =
      withContext(Dispatchers.IO) {
        logger.logActivity("Удаление приема пищи", additionalData = mapOf("id" to id))

        try {
          val response =
              httpClient
                  .delete("$baseUrl/delete") {
                    contentType(ContentType.Application.Json)
                    setBody(DbDeleteRequest("meals", "id = ?", listOf(id)))
                  }
                  .body<DbResponse>()

          val success = response.success == true

          if (success) {
            logger.logActivity("Прием пищи успешно удален", additionalData = mapOf("id" to id))
          } else {
            logger.logActivity(
                "Прием пищи не найден или не удален",
                additionalData =
                    mapOf("id" to id, "error" to (response.error ?: "Неизвестная ошибка")))
          }

          success
        } catch (e: Exception) {
          logger.logError(
              "Ошибка при удалении приема пищи: id=$id",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

  /** Преобразует DTO [DbMealRow] в доменный объект [Meal]. */
  private fun DbMealRow.toModel() =
      Meal(
          id = id,
          userId = userid,
          name = name,
          mealType = MealType.valueOf(meal_type),
          foods = emptyList(),
          date = LocalDateTime.parse(date))
}
