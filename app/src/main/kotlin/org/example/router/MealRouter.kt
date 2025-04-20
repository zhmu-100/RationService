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
import org.example.model.Food
import org.example.model.Meal
import org.example.service.IMealService

private fun stubFood(id: String) =
    Food(
        id = id,
        name = "",
        description = "",
        calories = 0.0,
        protein = 0.0,
        carbs = 0.0,
        saturatedFats = 0.0,
        transFats = 0.0,
        fiber = 0.0,
        sugar = 0.0)

fun Application.registerMealRoutes(mealService: IMealService) {
  routing {
    route("/diet/meals") {
      post {
        val req = call.receive<CreateMealRequest>()
        val foods = req.foods.map { stubFood(it.id) }
        val meal =
            Meal(
                id = "",
                userId = req.userId,
                name = req.name,
                mealType = req.mealType,
                foods = foods,
                date = req.date ?: Clock.System.now().toLocalDateTime(TimeZone.UTC))
        val created = mealService.createMeal(meal)
        call.respond(HttpStatusCode.Created, created)
      }

      get("/{id}") {
        val id =
            call.parameters["id"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("status" to "error", "message" to "Missing id"))
        val meal =
            mealService.getMeal(id)
                ?: return@get call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("status" to "error", "message" to "Meal not found"))
        call.respond(meal)
      }

      get {
        val userId = call.request.queryParameters["user_id"]
        if (userId.isNullOrBlank())
            return@get call.respond(
                HttpStatusCode.BadRequest,
                mapOf("status" to "error", "message" to "user_id required"))

        val start =
            call.request.queryParameters["start"]?.let(LocalDateTime::parse)
                ?: LocalDateTime.parse("1970-01-01T00:00")
        val end =
            call.request.queryParameters["end"]?.let(LocalDateTime::parse)
                ?: LocalDateTime.parse("3000-01-01T00:00")

        val meals = mealService.listMeals(userId, start, end)
        call.respond(meals)
      }

      delete("/{id}") {
        val id =
            call.parameters["id"]
                ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("status" to "error", "message" to "Missing id"))
        if (mealService.deleteMeal(id)) {
          call.respond(HttpStatusCode.OK, mapOf("status" to "success", "message" to "Meal deleted"))
        } else {
          call.respond(
              HttpStatusCode.NotFound, mapOf("status" to "error", "message" to "Meal not found"))
        }
      }
    }
  }
}
