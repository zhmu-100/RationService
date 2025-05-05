package org.example.service

import java.util.UUID
import org.example.actions.IFoodAction
import org.example.logging.LoggerProvider
import org.example.model.Food

/**
 * Реализация интерфейса [IFoodService] для бизнес логики работы с едой. Существование данного
 * класса под вопросом, но наверное хорошо разделять обращения к БД из [IFoodAction] и бизнес
 * логику...хотя из бизнес логики здесь 2 строки...зачем я это пишу...я не понимаю
 */
class FoodService(private val actions: IFoodAction) : IFoodService {
  private val logger = LoggerProvider.logger

  /**
   * Создает новый продукт. Генерит UUID и дату создания.
   *
   * @param food Продукт для создания без UUID
   * @return Созданный продукт с UUID
   */
  override suspend fun createFood(food: Food): Food {
    logger.logActivity(
        "Создание нового продукта",
        additionalData =
            mapOf(
                "name" to food.name,
                "vitaminsCount" to food.vitamins.size.toString(),
                "mineralsCount" to food.minerals.size.toString()))

    try {
      val newId = UUID.randomUUID().toString()

      logger.logActivity(
          "Сгенерирован UUID для нового продукта",
          additionalData = mapOf("name" to food.name, "newId" to newId))

      val newFood = food.copy(id = newId)
      val createdFood = actions.createFood(newFood)

      logger.logActivity(
          "Продукт успешно создан",
          additionalData = mapOf("id" to createdFood.id, "name" to createdFood.name))

      return createdFood
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при создании продукта: name=${food.name}",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /**
   * Получает продукт по его ID
   *
   * @param id ID продукта
   * @return Продукт или null, если он не найден
   */
  override suspend fun getFood(id: String): Food? {
    logger.logActivity("Получение продукта по ID", additionalData = mapOf("id" to id))

    try {
      val food = actions.getFood(id)

      if (food == null) {
        logger.logActivity("Продукт не найден", additionalData = mapOf("id" to id))
      } else {
        logger.logActivity(
            "Продукт успешно получен", additionalData = mapOf("id" to id, "name" to food.name))
      }

      return food
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при получении продукта: id=$id",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /**
   * Получает список всех продуктов с учетом фильтрации по имени
   *
   * @param nameFilter Фильтр по имени продукта
   * @return List продуктов
   */
  override suspend fun listFoods(nameFilter: String?): List<Food> {
    logger.logActivity(
        "Получение списка продуктов",
        additionalData = mapOf("nameFilter" to (nameFilter ?: "null")))

    try {
      val foods = actions.listFoods(nameFilter)

      logger.logActivity(
          "Список продуктов успешно получен",
          additionalData = mapOf("count" to foods.size.toString()))

      return foods
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при получении списка продуктов: nameFilter=${nameFilter ?: "null"}",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /**
   * Удаляет продукт
   *
   * @param id ID продукта
   * @return true, если удален, иначе false
   */
  override suspend fun deleteFood(id: String): Boolean {
    logger.logActivity("Удаление продукта", additionalData = mapOf("id" to id))

    try {
      val success = actions.deleteFood(id)

      if (success) {
        logger.logActivity("Продукт успешно удален", additionalData = mapOf("id" to id))
      } else {
        logger.logActivity("Продукт не найден или не удален", additionalData = mapOf("id" to id))
      }

      return success
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при удалении продукта: id=$id",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }
}
