package org.example.model

import kotlinx.serialization.Serializable

/**
 * Data class, который описывает еду
 *
 * @property id Идентификатор пищи
 * @property name Название пищи
 * @property description Описание пищи
 * @property calories Калории
 * @property protein Белки
 * @property carbs Углеводы
 * @property saturated_fats Насыщенные жиры
 * @property trans_fats Транс ждиры
 * @property fiber Клетчатка
 * @property sugar Сахар
 * @property vitamins Витамины
 * @property minerals Минералы
 */
@Serializable
data class Food(
    val id: String = "",
    val name: String,
    val description: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val saturatedFats: Double,
    val transFats: Double,
    val fiber: Double,
    val sugar: Double,
    val vitamins: List<Vitamin> = emptyList(),
    val minerals: List<Mineral> = emptyList()
)
