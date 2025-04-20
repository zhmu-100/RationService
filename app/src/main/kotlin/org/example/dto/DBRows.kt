package org.example.dto

import kotlinx.serialization.Serializable


@Serializable
data class DbMealRow(
  val id: String,
  val user_id: String,
  val name: String,
  val meal_type: String,
  val date: String
)
