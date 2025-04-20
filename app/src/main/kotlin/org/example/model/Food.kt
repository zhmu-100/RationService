package org.example.model

import kotlinx.serialization.Serializable

@Serializable
data class Vitamin(
  val id: String,
  val name: String,
  val amount: Double,
  val unit : String
)

@Serializable
data class Mineral(
  val id: String,
  val name: String,
  val amount: Double,
  val unit: String
)

@Serializable
data class Food(
  val id: String,
  val name: String,
  val description: String,
  val calories: Double,
  val protein: Double,
  val carbs: Double,
  val saturated_fats: Double,
  val trans_fats: Double,
  val fiber: Double,
  val sugar: Double,
  val Vitamins: List<Vitamin> = emptyList(),
  val Minerals: List<Mineral> = emptyList(),
)