package org.example.model

import kotlinx.serialization.Serializable

/**
 * Data class, который описывает минерал
 *
 * @property id Идентификатор минерала
 * @property name Название минерала
 * @property amount Количество минерала
 * @property unit Единица измерения минерала
 */
@Serializable
data class Mineral(val id: String, val name: String, val amount: Double, val unit: String)
