package org.example.actions

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
import org.example.model.Meal
import org.example.model.MealType

class MealAction(private val config: ApplicationConfig) : IMealAction {

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

  override suspend fun createMeal(meal: Meal): Meal =
      withContext(Dispatchers.IO) {
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
        if (mealResp.success != true)
            throw RuntimeException("Failed to create meal: ${mealResp.error}")

        meal.foods.forEach { food ->
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
          if (linkResp.success != true)
              throw RuntimeException("Failed to link food ${food.id}: ${linkResp.error}")
        }
        meal
      }

  override suspend fun getMeal(id: String): Meal? =
      withContext(Dispatchers.IO) {
        val mealRows: List<DbMealRow> =
            httpClient
                .post("$baseUrl/read") {
                  contentType(ContentType.Application.Json)
                  setBody(DbReadRequest("meals", filters = mapOf("id" to id)))
                }
                .body()

        val mealRow = mealRows.firstOrNull() ?: return@withContext null
        val baseMeal = mealRow.toModel()

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

        val foods =
            foodIds.map { fid ->
              org.example.model.Food(
                  id = fid,
                  name = "",
                  description = "",
                  calories = 0.0,
                  protein = 0.0,
                  carbs = 0.0,
                  saturatedFats = 0.0,
                  transFats = 0.0,
                  fiber = 0.0,
                  sugar = 0.0)
            }

        baseMeal.copy(foods = foods)
      }

  override suspend fun listMeals(
      userId: String,
      start: LocalDateTime,
      end: LocalDateTime
  ): List<Meal> =
      withContext(Dispatchers.IO) {
        val rows: List<DbMealRow> =
            httpClient
                .post("$baseUrl/read") {
                  contentType(ContentType.Application.Json)
                  setBody(DbReadRequest("meals", filters = mapOf("userid" to userId)))
                }
                .body()
        rows
            .map { row ->
              val base = row.toModel()
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
              val foods =
                  foodIds.map { fid ->
                    org.example.model.Food(
                        id = fid,
                        name = "",
                        description = "",
                        calories = 0.0,
                        protein = 0.0,
                        carbs = 0.0,
                        saturatedFats = 0.0,
                        transFats = 0.0,
                        fiber = 0.0,
                        sugar = 0.0)
                  }
              base.copy(foods = foods)
            }
            .filter { it.date >= start && it.date <= end }
      }

  override suspend fun deleteMeal(id: String): Boolean =
      withContext(Dispatchers.IO) {
        httpClient
            .delete("$baseUrl/delete") {
              contentType(ContentType.Application.Json)
              setBody(DbDeleteRequest("meals", "id = ?", listOf(id)))
            }
            .body<DbResponse>()
            .success == true
      }

  private fun DbMealRow.toModel() =
      Meal(
          id = id,
          userId = userid,
          name = name,
          mealType = MealType.valueOf(meal_type),
          foods = emptyList(),
          date = LocalDateTime.parse(date))
}
