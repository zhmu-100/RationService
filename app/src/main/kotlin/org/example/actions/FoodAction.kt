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
import org.example.dto.*
import org.example.model.Food

/**
 * Реализация интерфейса [IFoodAction]. Может работать с локальной БД или через API Gateway
 *
 * @see IFoodAction
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
   * Создает новую еду
   *
   * @param food Еда для создания
   * @return Созданная Еда
   */
  override suspend fun createFood(food: Food): Food =
      withContext(Dispatchers.IO) {
        val requestBody =
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
        val url = "$baseUrl/create"
        val response: HttpResponse =
            httpClient.post(url) {
              contentType(ContentType.Application.Json)
              setBody(requestBody)
            }

        val dbResp: DbResponse = response.body()
        if (dbResp.success == true) food else error(dbResp.error ?: "unknown error")
      }

  /**
   * Получает еду по идентификатору
   *
   * @param id Идентификатор еды
   * @return Найденная еда или null, если не найдена
   */
  override suspend fun getFood(id: String): Food? =
      withContext(Dispatchers.IO) {
        val body = DbReadRequest("foods", filters = mapOf("id" to id))
        val rows: List<DbFoodRow> =
            httpClient
                .post("$baseUrl/read") {
                  contentType(ContentType.Application.Json)
                  setBody(body)
                }
                .body()
        rows.firstOrNull()?.toModel()
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
        val ormFilters = emptyMap<String, String>()
        val body = DbReadRequest("foods", filters = ormFilters)

        val allRows: List<DbFoodRow> =
            httpClient
                .post("$baseUrl/read") {
                  contentType(ContentType.Application.Json)
                  setBody(body)
                }
                .body()

        val foods = allRows.map { it.toModel() }
        return@withContext if (nameFilter.isNullOrBlank()) foods
        else foods.filter { it.name.contains(nameFilter, ignoreCase = true) }
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
          sugar = sugar)
}
