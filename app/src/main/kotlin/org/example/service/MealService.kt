package org.example.service

import java.util.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.actions.IMealAction
import org.example.model.Meal

/**
 * Реализация интерфейса [IMealService] для бизнес логики работы с едой. Существование данного
 * класса под вопросом, но наверное хорошо разделять обращения к БД из [IMealAction] и бизнес
 * логику...хотя из бизнес логики здесь 2 строки...зачем я это пишу...я не понимаю
 */
class MealService(private val actions: IMealAction) : IMealService {

  /**
   * Создает новый прием пищи. Генерит UUID и дату создания.
   *
   * @param meal Прием пищи для создания
   * @return Созданный прием пищи с UUID и датой создания
   */
  override suspend fun createMeal(meal: Meal): Meal {
    val toSave =
        meal.copy(
            id = UUID.randomUUID().toString(),
            date = Clock.System.now().toLocalDateTime(TimeZone.UTC))
    return actions.createMeal(toSave)
  }

  /**
   * Получает прием пищи по его ID
   *
   * @param id ID приема пищи
   * @return Прием пищи или null, если он не найден
   */
  override suspend fun getMeal(id: String): Meal? = actions.getMeal(id)

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
  ): List<Meal> = actions.listMeals(userId, start, end)

  /**
   * Обновляет существующий прием пищи. Обновляет дату обновления.
   *
   * @param meal Обновленный прием пищи. ID должен совпадать
   * @return Обновленный прием пищи
   */
  override suspend fun deleteMeal(id: String): Boolean = actions.deleteMeal(id)
}
