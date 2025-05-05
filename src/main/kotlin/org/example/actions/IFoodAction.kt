package org.example.actions

import org.example.model.Food

/**
 * Интерфейс для работы с продуктами
 *
 * Возможные операции:
 * - Создание продукта
 * - Получение продукта по id
 * - Получение списка продуктов с фильтрацией по имени
 * - Удаление продукта
 */
interface IFoodAction {
  /**
   * Создает новый продукт
   *
   * @param food Продукт для сохранения
   * @return Сохраненный продукт
   */
  suspend fun createFood(food: Food): Food

  /**
   * Получает продукт по его ID
   *
   * @param id ID продукта
   * @return Продукт или null, если он не найден
   */
  suspend fun getFood(id: String): Food?

  /**
   * Получает список всех продуктов с учетом фильтрации по имени
   *
   * @param nameFilter Фильтр по имени продукта
   * @return List продуктов
   */
  suspend fun listFoods(nameFilter: String?): List<Food>

  /**
   * Удаляет продукт
   *
   * @param id ID продукта
   * @return true, если удален, иначе false
   */
  suspend fun deleteFood(id: String): Boolean
}
