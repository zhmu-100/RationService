package org.example.service

import java.util.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.actions.IMealAction
import org.example.logging.LoggerProvider
import org.example.model.Meal

/**
 * Реализация интерфейса [IMealService] для бизнес логики работы с едой. Существование данного
 * класса под вопросом, но наверное хорошо разделять обращения к БД из [IMealAction] и бизнес
 * логику...хотя из бизнес логики здесь 2 строки...зачем я ��то пишу...я не понимаю
 */
class MealService(private val actions: IMealAction) : IMealService {
  private val logger = LoggerProvider.logger

  /**
   * Создает новый прием пищи. Генерит UUID и дату создания.
   *
   * @param meal Прием пищи для создания
   * @return Созданный прием пищи с UUID и датой создания
   */
  override suspend fun createMeal(meal: Meal): Meal {
    logger.logActivity(
        "Создание нового приема пищи",
        additionalData =
            mapOf(
                "userId" to meal.userId,
                "name" to meal.name,
                "mealType" to meal.mealType.name,
                "foodsCount" to meal.foods.size.toString()))

    try {
      val newId = UUID.randomUUID().toString()
      val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

      logger.logActivity(
          "Сгенерированы UUID и дата для нового приема пищи",
          additionalData =
              mapOf("userId" to meal.userId, "newId" to newId, "date" to now.toString()))

      val toSave = meal.copy(id = newId, date = now)

      val createdMeal = actions.createMeal(toSave)

      logger.logActivity(
          "Прием пищи успешно создан",
          additionalData =
              mapOf(
                  "id" to createdMeal.id,
                  "userId" to createdMeal.userId,
                  "name" to createdMeal.name))

      return createdMeal
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при создании приема пищи: userId=${meal.userId}, name=${meal.name}",
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
  override suspend fun getMeal(id: String): Meal? {
    logger.logActivity("Получение приема пищи по ID", additionalData = mapOf("id" to id))

    try {
      val meal = actions.getMeal(id)

      if (meal == null) {
        logger.logActivity("Прием пищи не найден", additionalData = mapOf("id" to id))
      } else {
        logger.logActivity(
            "Прием пищи успешно получен",
            additionalData =
                mapOf(
                    "id" to id,
                    "userId" to meal.userId,
                    "name" to meal.name,
                    "foodsCount" to meal.foods.size.toString()))
      }

      return meal
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при получении приема пищи: id=$id",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /**
   * Получает List<Meal> приемов пищи для пользователя с постраничной навигацией
   *
   * @param userId ID пользователя
   * @param start Начало диапазона дат
   * @param end Конец диапазона дат
   * @return Список приемов пищи
   */
  override suspend fun listMeals(
      userId: String,
      start: LocalDateTime,
      end: LocalDateTime
  ): List<Meal> {
    logger.logActivity(
        "Получение списка приемов пищи",
        additionalData =
            mapOf("userId" to userId, "start" to start.toString(), "end" to end.toString()))

    try {
      val meals = actions.listMeals(userId, start, end)

      logger.logActivity(
          "Список приемов пищи успешно получен",
          additionalData = mapOf("userId" to userId, "count" to meals.size.toString()))

      return meals
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при получении списка приемов пищи: userId=$userId, start=$start, end=$end",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /**
   * Обновляет существующий прием пищи. Обновляет дату обновления.
   *
   * @param meal Обновленный прием пищи. ID должен совпадать
   * @return Обновленный прием пищи
   */
  override suspend fun deleteMeal(id: String): Boolean {
    logger.logActivity("Удаление приема пищи", additionalData = mapOf("id" to id))

    try {
      val success = actions.deleteMeal(id)

      if (success) {
        logger.logActivity("Прием пищи успешно удален", additionalData = mapOf("id" to id))
      } else {
        logger.logActivity("Прием пищи не найден или не удален", additionalData = mapOf("id" to id))
      }

      return success
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при удалении приема пищи: id=$id",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }
}
