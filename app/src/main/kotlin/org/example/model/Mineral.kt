package org.example.model

import kotlinx.serialization.Serializable

@Serializable
data class Mineral(
  val id: String,
  val name: String,
  val amount: Double,
  val unit: String
)
