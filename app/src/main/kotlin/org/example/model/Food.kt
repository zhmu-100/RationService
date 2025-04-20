package org.example.model

import kotlinx.serialization.Serializable

@Serializable
data class Food(
  val id: String,
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
