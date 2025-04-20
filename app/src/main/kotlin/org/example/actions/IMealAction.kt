package org.example.actions

import kotlinx.datetime.LocalDateTime
import org.example.model.Meal

/**
 * Интерфейс для работы с приемами пищи
 *
 * Возможные операции:
 * - Создание приема пищи
 * - Получение приема пищи по id
 * - Получение списка приемов пищи для заданного пользователя
 * - Удаление приема пищи
 */
interface IMealAction {
  /**
   * Создает новый прием пищи
   *
   * @param meal Прием пищи для сохранения
   * @return Сохраненный прием пищи
   */
  suspend fun createMeal(meal: Meal): Meal

  /**
   * Получает прием пищи по его ID
   *
   * @param id ID приема пищи
   * @return Прием пищи или null, если он не найден
   */
  suspend fun getMeal(id: String): Meal?

  /**
   * Получает список всех приемов пищи для пользователя с учетом фильтрации по дате
   *
   * @param userId ID пользователя
   * @param start Начало диапазона дат
   * @param end Конец диапазона дат
   * @return List приемов пищи
   */
  suspend fun listMeals(userId: String, start: LocalDateTime, end: LocalDateTime): List<Meal>

  /**
   * Удаляет прием пищи
   *
   * @param id ID приема пищи
   * @return true, если удален, иначе false
   */
  suspend fun deleteMeal(id: String): Boolean
}
