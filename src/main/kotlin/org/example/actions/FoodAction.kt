package org.example.actions

import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.example.dto.*
import org.example.logging.LoggerProvider
import org.example.model.Food
import org.example.model.Mineral
import org.example.model.Vitamin

/**
 * Реализация интерфейса [IFoodAction]. Может работать с локальной БД или через API Gateway
 *
 * @see IFoodAction
 * @constructor Загружает конфигурацию приложения.
 */
class FoodAction : IFoodAction {
  private val logger = LoggerProvider.logger
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
   * Создает новую еду и связывает с ней витамины и минералы.
   *
   * @param food Еда для создания
   * @return Созданная еда
   * @throws IllegalStateException если создать в БД не удалось.
   */
  override suspend fun createFood(food: Food): Food =
      withContext(Dispatchers.IO) {
        logger.logActivity(
            "Создание новой еды",
            additionalData =
                mapOf(
                    "id" to food.id,
                    "name" to food.name,
                    "vitaminsCount" to food.vitamins.size.toString(),
                    "mineralsCount" to food.minerals.size.toString()))

        try {
          val foodRequest =
              DbCreateRequest(
                  table = "foods",
                  data =
                      mapOf(
                          "id" to food.id,
                          "name" to food.name,
                          "description" to food.description,
                          "calories" to food.calories.toString(),
                          "protein" to food.protein.toString(),
                          "carbs" to food.carbs.toString(),
                          "saturated_fats" to food.saturatedFats.toString(),
                          "trans_fats" to food.transFats.toString(),
                          "fiber" to food.fiber.toString(),
                          "sugar" to food.sugar.toString()))

          val response =
              httpClient
                  .post("$baseUrl/create") {
                    contentType(ContentType.Application.Json)
                    setBody(foodRequest)
                  }
                  .body<DbResponse>()

          if (!response.success!!) {
            logger.logError(
                "Ошибка при создании еды в БД: id=${food.id}, name=${food.name}",
                errorMessage = response.error ?: "DB insert failed")
            error(response.error ?: "DB insert failed")
          }

          logger.logActivity(
              "Еда успешно создана в БД",
              additionalData = mapOf("id" to food.id, "name" to food.name))

          // Добавляем витамины
          for (vitamin in food.vitamins) {
            logger.logActivity(
                "Добавление витамина",
                additionalData =
                    mapOf(
                        "foodId" to food.id,
                        "vitaminId" to vitamin.id,
                        "vitaminName" to vitamin.name))

            try {
              // Создаем витамин, если его еще нет
              httpClient.post("$baseUrl/create") {
                contentType(ContentType.Application.Json)
                setBody(
                    DbCreateRequest(
                        "vitamins",
                        mapOf("id" to vitamin.id, "name" to vitamin.name, "unit" to vitamin.unit)))
              }

              // Связываем витамин с едой
              httpClient.post("$baseUrl/create") {
                contentType(ContentType.Application.Json)
                setBody(
                    DbCreateRequest(
                        "food_vitamins",
                        mapOf(
                            "food_id" to food.id,
                            "vitamin_id" to vitamin.id,
                            "amount" to vitamin.amount.toString())))
              }

              logger.logActivity(
                  "Витамин успешно добавлен",
                  additionalData = mapOf("foodId" to food.id, "vitaminId" to vitamin.id))
            } catch (e: Exception) {
              logger.logError(
                  "Ошибка при добавлении витамина: foodId=${food.id}, vitaminId=${vitamin.id}",
                  errorMessage = e.message ?: "Неизвестная ошибка")
              throw e
            }
          }

          // Добавляем минералы
          for (mineral in food.minerals) {
            logger.logActivity(
                "Добавление минерала",
                additionalData =
                    mapOf(
                        "foodId" to food.id,
                        "mineralId" to mineral.id,
                        "mineralName" to mineral.name))

            try {
              // Создаем минерал, если его еще нет
              httpClient.post("$baseUrl/create") {
                contentType(ContentType.Application.Json)
                setBody(
                    DbCreateRequest(
                        "minerals",
                        mapOf("id" to mineral.id, "name" to mineral.name, "unit" to mineral.unit)))
              }

              // Связываем минерал с едой
              httpClient.post("$baseUrl/create") {
                contentType(ContentType.Application.Json)
                setBody(
                    DbCreateRequest(
                        "food_minerals",
                        mapOf(
                            "food_id" to food.id,
                            "mineral_id" to mineral.id,
                            "amount" to mineral.amount.toString())))
              }

              logger.logActivity(
                  "Минерал успешно добавлен",
                  additionalData = mapOf("foodId" to food.id, "mineralId" to mineral.id))
            } catch (e: Exception) {
              logger.logError(
                  "Ошибка при добавлении минерала: foodId=${food.id}, mineralId=${mineral.id}",
                  errorMessage = e.message ?: "Неизвестная ошибка")
              throw e
            }
          }

          logger.logActivity(
              "Еда успешно создана со всеми связями",
              additionalData =
                  mapOf(
                      "id" to food.id,
                      "name" to food.name,
                      "vitaminsCount" to food.vitamins.size.toString(),
                      "mineralsCount" to food.minerals.size.toString()))

          food
        } catch (e: Exception) {
          logger.logError(
              "Ошибка при создании еды: id=${food.id}, name=${food.name}",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

  /**
   * Получает еду по идентификатору
   *
   * @param id Идентификатор еды
   * @return Найденная [Food] или null, если не найдена
   */
  override suspend fun getFood(id: String): Food? =
      withContext(Dispatchers.IO) {
        logger.logActivity("Получение еды по ID", additionalData = mapOf("id" to id))

        try {
          println("Baseurl " + baseUrl)
          val response =
              httpClient.post("$baseUrl/read") {
                contentType(ContentType.Application.Json)
                setBody(DbReadRequest("foods", filters = mapOf("id" to id)))
              }

          val foodRows: List<DbFoodRow> = response.body()
          val dbFood = foodRows.firstOrNull()

          if (dbFood == null) {
            logger.logActivity("Еда не найдена", additionalData = mapOf("id" to id))
            return@withContext null
          }

          logger.logActivity(
              "Еда найдена, получение связанных данных",
              additionalData = mapOf("id" to id, "name" to dbFood.name))

          // Получаем связи с витаминами
          val foodVitaminsJson =
              httpClient
                  .post("$baseUrl/read") {
                    contentType(ContentType.Application.Json)
                    setBody(DbReadRequest("food_vitamins", filters = mapOf("food_id" to id)))
                  }
                  .bodyAsText()

          // Получаем связи с минералами
          val foodMineralsJson =
              httpClient
                  .post("$baseUrl/read") {
                    contentType(ContentType.Application.Json)
                    setBody(DbReadRequest("food_minerals", filters = mapOf("food_id" to id)))
                  }
                  .bodyAsText()

          val foodVitamins = Json.decodeFromString<List<DbFoodVitamin>>(foodVitaminsJson)
          val foodMinerals = Json.decodeFromString<List<DbFoodMineral>>(foodMineralsJson)

          logger.logActivity(
              "Получены связи с витаминами и минералами",
              additionalData =
                  mapOf(
                      "id" to id,
                      "vitaminsCount" to foodVitamins.size.toString(),
                      "mineralsCount" to foodMinerals.size.toString()))

          // Получаем все витамины и минералы
          val vitamins =
              httpClient
                  .post("$baseUrl/read") {
                    contentType(ContentType.Application.Json)
                    setBody(DbReadRequest("vitamins"))
                  }
                  .body<List<DbVitamin>>()

          val minerals =
              httpClient
                  .post("$baseUrl/read") {
                    contentType(ContentType.Application.Json)
                    setBody(DbReadRequest("minerals"))
                  }
                  .body<List<DbMineral>>()

          // Создаем карты для быстрого доступа
          val vitaminMap = vitamins.associateBy { it.id }
          val attachedVitamins =
              foodVitamins.mapNotNull { fv ->
                vitaminMap[fv.vitamin_id]?.let { Vitamin(it.id, it.name, fv.amount, it.unit) }
              }

          val mineralMap = minerals.associateBy { it.id }
          val attachedMinerals =
              foodMinerals.mapNotNull { fm ->
                mineralMap[fm.mineral_id]?.let { Mineral(it.id, it.name, fm.amount, it.unit) }
              }

          val food = dbFood.toModel(vitamins = attachedVitamins, minerals = attachedMinerals)

          logger.logActivity(
              "Еда успешно получена со всеми связями",
              additionalData =
                  mapOf(
                      "id" to id,
                      "name" to food.name,
                      "vitaminsCount" to attachedVitamins.size.toString(),
                      "mineralsCount" to attachedMinerals.size.toString()))

          return@withContext food
        } catch (e: Exception) {
          logger.logError(
              "Ошибка при получении еды: id=$id",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

  /**
   * Возвращает все еды, отсортированные по имени.
   *
   * @param nameFilter Фильтрация еды поиском в названии (`ignoreCase = true`). Если `null` или
   * пустой — возвращаются все.
   * @return Список объектов [Food].
   */
  override suspend fun listFoods(nameFilter: String?): List<Food> =
      withContext(Dispatchers.IO) {
        logger.logActivity(
            "Получение списка еды", additionalData = mapOf("nameFilter" to (nameFilter ?: "null")))

        try {
          // Получаем все еды
          val responseText =
              httpClient
                  .post("$baseUrl/read") {
                    contentType(ContentType.Application.Json)
                    setBody(DbReadRequest("foods"))
                  }
                  .bodyAsText()

          val foods = Json.decodeFromString<List<DbFoodRow>>(responseText)

          logger.logActivity(
              "Получены базовые данные о еде",
              additionalData = mapOf("count" to foods.size.toString()))

          // Получаем связи с витаминами и минералами
          val responseFoodVitamin =
              httpClient
                  .post("$baseUrl/read") {
                    contentType(ContentType.Application.Json)
                    setBody(DbReadRequest("food_vitamins"))
                  }
                  .bodyAsText()

          val responseFoodMineral =
              httpClient
                  .post("$baseUrl/read") {
                    contentType(ContentType.Application.Json)
                    setBody(DbReadRequest("food_minerals"))
                  }
                  .bodyAsText()

          val foodVitamins = Json.decodeFromString<List<DbFoodVitamin>>(responseFoodVitamin)
          val foodMinerals = Json.decodeFromString<List<DbFoodMineral>>(responseFoodMineral)

          // Группируем связи по ID еды
          val vitaminsByFood = foodVitamins.groupBy { it.food_id }
          val mineralsByFood = foodMinerals.groupBy { it.food_id }

          logger.logActivity(
              "Получены связи с витаминами и минералами",
              additionalData =
                  mapOf(
                      "vitaminsCount" to foodVitamins.size.toString(),
                      "mineralsCount" to foodMinerals.size.toString()))

          // Получаем все витамины и минералы
          val responseVitamins =
              httpClient
                  .post("$baseUrl/read") {
                    contentType(ContentType.Application.Json)
                    setBody(DbReadRequest("vitamins"))
                  }
                  .bodyAsText()

          val responseMinerals =
              httpClient
                  .post("$baseUrl/read") {
                    contentType(ContentType.Application.Json)
                    setBody(DbReadRequest("minerals"))
                  }
                  .bodyAsText()

          val vitamins =
              Json.decodeFromString<List<DbVitamin>>(responseVitamins).associateBy { it.id }
          val minerals =
              Json.decodeFromString<List<DbMineral>>(responseMinerals).associateBy { it.id }

          logger.logActivity(
              "Получены данные о витаминах и минералах",
              additionalData =
                  mapOf(
                      "vitaminsCount" to vitamins.size.toString(),
                      "mineralsCount" to minerals.size.toString()))

          // Собираем полные объекты еды
          val result =
              foods.map { dbFood ->
                val foodId = dbFood.id

                val foodVits =
                    vitaminsByFood[foodId].orEmpty().mapNotNull { link ->
                      vitamins[link.vitamin_id]?.let {
                        Vitamin(id = it.id, name = it.name, amount = link.amount, unit = it.unit)
                      }
                    }

                val foodMins =
                    mineralsByFood[foodId].orEmpty().mapNotNull { link ->
                      minerals[link.mineral_id]?.let {
                        Mineral(id = it.id, name = it.name, amount = link.amount, unit = it.unit)
                      }
                    }

                dbFood.toModel().copy(vitamins = foodVits, minerals = foodMins)
              }

          // Применяем фильтр по имени, если он задан
          val filteredResult =
              if (nameFilter.isNullOrBlank()) {
                result
              } else {
                result.filter { it.name.contains(nameFilter, ignoreCase = true) }
              }

          logger.logActivity(
              "Список еды успешно получен",
              additionalData =
                  mapOf(
                      "totalCount" to result.size.toString(),
                      "filteredCount" to filteredResult.size.toString()))

          return@withContext filteredResult
        } catch (e: Exception) {
          logger.logError(
              "Ошибка при получении списка еды: nameFilter=${nameFilter ?: "null"}",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

  /**
   * Удаляет еду из БД по идентификатору.
   *
   * @param id Идентификатор еды.
   * @return `true`, если запись была успешно удалена, иначе `false`.
   */
  override suspend fun deleteFood(id: String): Boolean =
      withContext(Dispatchers.IO) {
        logger.logActivity("Удаление еды", additionalData = mapOf("id" to id))

        try {
          val body = DbDeleteRequest("foods", "id = ?", listOf(id))
          val response =
              httpClient
                  .delete("$baseUrl/delete") {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                  }
                  .body<DbResponse>()

          val success = response.success == true

          if (success) {
            logger.logActivity("Еда успешно удалена", additionalData = mapOf("id" to id))
          } else {
            logger.logActivity(
                "Еда не найдена или не удалена",
                additionalData =
                    mapOf("id" to id, "error" to (response.error ?: "Неизвестная ошибка")))
          }

          success
        } catch (e: Exception) {
          logger.logError(
              "Ошибка при удалении еды: id=$id",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

  /**
   * Преобразует DTO [DbFoodRow] в доменную модель [Food], добавляя в неё витамины и минералы, если
   * они переданы.
   *
   * @param vitamins Список витаминов, связанных с едой.
   * @param minerals Список минералов, связанных с едой.
   * @return Доменный [Food].
   */
  private fun DbFoodRow.toModel(
      vitamins: List<Vitamin> = emptyList(),
      minerals: List<Mineral> = emptyList(),
  ) =
      Food(
          id = id,
          name = name,
          description = description,
          calories = calories,
          protein = protein,
          carbs = carbs,
          saturatedFats = saturated_fats,
          transFats = trans_fats,
          fiber = fiber,
          sugar = sugar,
          vitamins = vitamins,
          minerals = minerals)
}
