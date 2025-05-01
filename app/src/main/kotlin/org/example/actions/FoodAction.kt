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
import org.example.model.Mineral
import org.example.model.Vitamin

/**
 * ���������� ���������� [IFoodAction]. ����� �������� � ��������� �� ��� ����� API Gateway
 *
 * @see IFoodAction
 * @constructor ��������� ������������ ����������.
 * @property config ������������ ����������, ������������ ��� ����������� ������ ��
 */
class FoodAction(config: ApplicationConfig) : IFoodAction {

  private val dbMode = config.propertyOrNull("ktor.database.mode")?.getString() ?: "LOCAL"
  private val dbHost = config.propertyOrNull("ktor.database.host")?.getString() ?: "localhost"
  private val dbPort = config.propertyOrNull("ktor.database.port")?.getString() ?: "8081"

  private val baseUrl =
      if (dbMode == "gateway") {
        "http://$dbHost:$dbPort/api/db"
      } else {
        "http://$dbHost:$dbPort"
      }

  private val httpClient = HttpClient { install(ContentNegotiation) { json() } }

  /**
   * ������ ����� ��� � ��������� � ��� �������� � ��������.
   *
   * @param food ��� ��� ��������
   * @return ��������� ���
   * @throws IllegalStateException ���� ������� � �� �� �������.
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
   * �������� ��� �� ��������������
   *
   * @param id ������������� ���
   * @return ��������� [Food] ��� null, ���� �� �������
   */
  override suspend fun getFood(id: String): Food? =
      withContext(Dispatchers.IO) {
        val response =
            httpClient.post("$baseUrl/read") {
              contentType(ContentType.Application.Json)
              setBody(DbReadRequest("foods", filters = mapOf("id" to id)))
            }
        val foodRows: List<DbFoodRow> = response.body()
        val dbFood = foodRows.firstOrNull() ?: return@withContext null

        val foodVitaminsJson =
            httpClient
                .post("$baseUrl/read") {
                  contentType(ContentType.Application.Json)
                  setBody(DbReadRequest("food_vitamins", filters = mapOf("food_id" to id)))
                }
                .bodyAsText()
        val foodMineralsJson =
            httpClient
                .post("$baseUrl/read") {
                  contentType(ContentType.Application.Json)
                  setBody(DbReadRequest("food_minerals", filters = mapOf("food_id" to id)))
                }
                .bodyAsText()

        val foodVitamins = Json.decodeFromString<List<DbFoodVitamin>>(foodVitaminsJson)
        val foodMinerals = Json.decodeFromString<List<DbFoodMineral>>(foodMineralsJson)

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

        return@withContext dbFood.toModel(vitamins = attachedVitamins, minerals = attachedMinerals)
      }

  /**
   * ���������� ��� ���, ����������� ��������������� �� �����.
   *
   * @param nameFilter ��������� ��� ������ � ��������� (`ignoreCase = true`). ���� `null` ���
   * ������ � ������������ ���.
   * @return ������ �������� [Food].
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
        val foods = Json.decodeFromString<List<DbFoodRow>>(responseText)

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

        val vitaminsByFood = foodVitamins.groupBy { it.food_id }
        val mineralsByFood = foodMinerals.groupBy { it.food_id }

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

        return@withContext foods.map { dbFood ->
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
      }

  /**
   * ������� ��� �� � ��������������.
   *
   * @param id ������������� ���.
   * @return `true`, ���� ������ ���� ������� �������, ����� `false`.
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

  /**
   * ����������� DTO [DbFoodRow] � �������� ������ [Food], �������� � ��� �������� � ��������, ����
   * ��� ��������.
   *
   * @param vitamins ������ ���������, ��������� � ����.
   * @param minerals ������ ���������, ��������� � ����.
   * @return ��������� [Food].
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
