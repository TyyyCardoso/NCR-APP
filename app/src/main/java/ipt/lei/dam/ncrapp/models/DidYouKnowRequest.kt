package ipt.lei.dam.ncrapp.models

import java.time.LocalDateTime

data class DidYouKnowRequest(
    val id: Int,
    var title: String,
    var text: String,
    var references: String,
    var createdAt: String,
    var updatedAt: String
)
