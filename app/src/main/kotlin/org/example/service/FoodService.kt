package org.example.service

import java.util.UUID
import org.example.actions.IFoodAction
import org.example.model.Food

/**
 * Реализация интерфейса [IFoodService] для бизнес логики работы с едой. Существование данного
 * класса под вопросом, но наверное хорошо разделять обращения к БД из [IFoodAction] и бизнес
 * логику...хотя из бизнес логики здесь 2 строки...зачем я это пишу...я не понимаю
 */
class FoodService(private val actions: IFoodAction) : IFoodService {

  /**
   * Создает новый продукт. Генерит UUID и дату создания.
   *
   * @param food Продукт для создания без UUID
   * @return Созданный продукт с UUID
   */
  override suspend fun createFood(food: Food): Food {
    val newFood = food.copy(id = UUID.randomUUID().toString())
    return actions.createFood(newFood)
  }

  /**
   * Получает продукт по его ID
   *
   * @param id ID продукта
   * @return Продукт или null, если он не найден
   */
  override suspend fun getFood(id: String): Food? = actions.getFood(id)

  /**
   * Получает список всех продуктов с учетом фильтрации по имени
   *
   * @param nameFilter Фильтр по имени продукта
   * @return List продуктов
   */
  override suspend fun listFoods(nameFilter: String?): List<Food> = actions.listFoods(nameFilter)

  /**
   * Удаляет продукт
   *
   * @param id ID продукта
   * @return true, если удален, иначе false
   */
  override suspend fun deleteFood(id: String): Boolean = actions.deleteFood(id)
}
