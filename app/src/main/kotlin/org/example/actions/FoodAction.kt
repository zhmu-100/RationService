package org.example.actions

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
import org.example.model.Food

/**
 * Реализация интерфейса [IFoodAction].
 * Может работать с локальной БД или через API Gateway
 *
 * @see IFoodAction
 * @constructor Принимает конфигурацию приложения.
 * @property config Конфигурация приложения, используется для определения адреса БД
 */
class FoodAction(config: ApplicationConfig) : IFoodAction {

  private val dbMode = config.propertyOrNull("ktor.database.mode")?.getString() ?: "LOCAL"
  private val dbHost = config.propertyOrNull("ktor.database.host")?.getString() ?: "localhost"
  private val dbPort = config.propertyOrNull("ktor.database.port")?.getString() ?: "8080"

  private val baseUrl =
      if (dbMode == "gateway") {
        "http://$dbHost:$dbPort/api/db"
      } else {
        "http://$dbHost:$dbPort"
      }

  private val httpClient = HttpClient { install(ContentNegotiation) { json() } }

  /**
   * Создаёт новую еду и связанные с ней витамины и минералы.
   *
   * @param food Еда для создания
   * @return Созданная Еда
   * @throws IllegalStateException если вставка в БД не удалась.
   */
  override suspend fun createFood(food: Food): Food =
      withContext(Dispatchers.IO) {
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
        httpClient
            .post("$baseUrl/create") {
              contentType(ContentType.Application.Json)
              setBody(foodRequest)
            }
            .body<DbResponse>()
            .let { if (!it.success!!) error(it.error ?: "DB insert failed") }

        for (vitamin in food.vitamins) {
          httpClient.post("$baseUrl/create") {
            contentType(ContentType.Application.Json)
            setBody(
                DbCreateRequest(
                    "vitamins",
                    mapOf("id" to vitamin.id, "name" to vitamin.name, "unit" to vitamin.unit)))
          }

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
        }

        for (mineral in food.minerals) {
          httpClient.post("$baseUrl/create") {
            contentType(ContentType.Application.Json)
            setBody(
                DbCreateRequest(
                    "minerals",
                    mapOf("id" to mineral.id, "name" to mineral.name, "unit" to mineral.unit)))
          }

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
        }
        food
      }

  /**
   * Получает еду по идентификатору
   *
   * @param id Идентификатор еды
   * @return Найденная [Food] или null, если не найдена
   */
  override suspend fun getFood(id: String): Food? =
      withContext(Dispatchers.IO) {
        val response =
            httpClient.post("$baseUrl/read") {
              contentType(ContentType.Application.Json)
              setBody(DbReadRequest("foods", filters = mapOf("id" to id)))
            }
        val foodRows: List<DbFoodRow> = response.body()
        val food = foodRows.firstOrNull()?.toModel() ?: return@withContext null
        food
      }

  /**
   * Возвращает всю еду, опционально отфильтрованные по имени.
   *
   * @param nameFilter Подстрока для поиска в названиях (`ignoreCase = true`). Если `null` или
   * пустая — возвращаются все.
   * @return Список объектов [Food].
   */
  override suspend fun listFoods(nameFilter: String?): List<Food> =
      withContext(Dispatchers.IO) {
        val responseText =
            httpClient
                .post("$baseUrl/read") {
                  contentType(ContentType.Application.Json)
                  setBody(DbReadRequest("foods"))
                }
                .bodyAsText()
        val allFoods = Json.decodeFromString<List<DbFoodRow>>(responseText)
        return@withContext allFoods.map { it.toModel() }
      }

  /**
   * Удаляет еду по её идентификатору.
   *
   * @param id Идентификатор еды.
   * @return `true`, если запись была успешно удалена, иначе `false`.
   */
  override suspend fun deleteFood(id: String): Boolean =
      withContext(Dispatchers.IO) {
        val body = DbDeleteRequest("foods", "id = ?", listOf(id))
        httpClient
            .delete("$baseUrl/delete") {
              contentType(ContentType.Application.Json)
              setBody(body)
            }
            .body<DbResponse>()
            .success == true
      }

  /** Преобразует DTO [DbFoodRow] в доменный объект [Food]. */
  private fun DbFoodRow.toModel() =
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
          vitamins = vitamins ?: emptyList(),
          minerals = minerals ?: emptyList())
}
