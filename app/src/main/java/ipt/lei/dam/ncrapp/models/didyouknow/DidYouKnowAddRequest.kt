package ipt.lei.dam.ncrapp.models.didyouknow

/**
 *
 * Classe modelo para pedido à API de adicionar "sabiasque"
 *
 * title: Titulo do sabias que
 * text: Texto do sabias que
 * references: Referências do sabias que
 *
 */
data class DidYouKnowAddRequest(
    var title: String,
    var text: String,
    var references: String
)
