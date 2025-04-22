package org.example.model

import kotlinx.serialization.Serializable

/**
 * Data class, который описывает витамин
 *
 * @property id Идентификатор витамина
 * @property name Название витамина
 * @property amount Количество витамина
 * @property unit Единица измерения витамина
 */
@Serializable
data class Vitamin(val id: String, val name: String, val amount: Double, val unit: String)
