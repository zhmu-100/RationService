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
import kotlinx.datetime.LocalDateTime
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
        val body =
            DbCreateRequest(
                table = "meals",
                data =
                    mapOf(
                        "id" to meal.id,
                        "user_id" to meal.userId,
                        "name" to meal.name,
                        "meal_type" to meal.mealType.name,
                        "date" to meal.date.toString()))
        val response: HttpResponse =
            httpClient.post("$baseUrl/create") {
              contentType(ContentType.Application.Json)
              setBody(body)
            }
        val dbResp = response.body<DbResponse>()
        if (dbResp.success == true) meal else throw RuntimeException(dbResp.error)
      }

  override suspend fun getMeal(id: String): Meal? =
      withContext(Dispatchers.IO) {
        val body = DbReadRequest(table = "meals", filters = mapOf("id" to id))

        val response: HttpResponse =
            httpClient.post("$baseUrl/read") {
              contentType(ContentType.Application.Json)
              setBody(body)
            }
        val rows: List<DbMealRow> = response.body()
        rows.firstOrNull()?.let {
          Meal(
              id = it.id,
              userId = it.user_id,
              name = it.name,
              mealType = MealType.valueOf(it.meal_type),
              foods = emptyList(),
              date = LocalDateTime.parse(it.date))
        }
      }

  override suspend fun listMeals(
      userId: String,
      start: LocalDateTime,
      end: LocalDateTime
  ): List<Meal> =
      withContext(Dispatchers.IO) {
        val body =
            DbReadRequest(
                table = "meals",
                filters =
                    mapOf(
                        "user_id" to userId,
                        "date >= ?" to start.toString(),
                        "date <= ?" to end.toString()))
        val response: HttpResponse =
            httpClient.post("$baseUrl/read") {
              contentType(ContentType.Application.Json)
              setBody(body)
            }
        response.body<List<DbMealRow>>().map {
          Meal(
              id = it.id,
              userId = it.user_id,
              name = it.name,
              mealType = MealType.valueOf(it.meal_type),
              foods = emptyList(),
              date = LocalDateTime.parse(it.date))
        }
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
}
