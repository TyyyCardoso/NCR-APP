package ipt.lei.dam.ncrapp.models.didyouknow

data class DidYouKnowEditRequest(
    val id: Int,
    var title: String,
    var text: String,
    var references: String,
    val createdAt: String
)
