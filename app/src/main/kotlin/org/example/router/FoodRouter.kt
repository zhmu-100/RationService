package org.example.router

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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
  routing {
    route("/diet/foods") {
      /** Создание новой еды */
      post {
        val food = call.receive<Food>()
        val created = foodService.createFood(food)
        call.respond(created)
      }

      /** Получение еды по ID */
      get("{id}") {
        val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val food = foodService.getFood(id) ?: return@get call.respond(HttpStatusCode.NotFound)
        call.respond(food)
      }

      /**
       * Получение списка всех еды с фильтрацией по имени
       *
       * Параметры запроса:
       * - name_filter - Фильтр по имени еды (необязательный)
       */
      get {
        val filter = call.request.queryParameters["name_filter"]
        call.respond(foodService.listFoods(filter))
      }

      /** Удаление еды по ID */
      delete("{id}") {
        val id =
            call.parameters["id"]
                ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("status" to "error", "message" to "Missing id parameter"))
        val ok = foodService.deleteFood(id)
        if (ok) {
          call.respond(HttpStatusCode.OK, mapOf("status" to "success", "message" to "Food deleted"))
        } else {
          call.respond(
              HttpStatusCode.NotFound, mapOf("status" to "error", "message" to "Food not found"))
        }
      }
    }
  }
}
