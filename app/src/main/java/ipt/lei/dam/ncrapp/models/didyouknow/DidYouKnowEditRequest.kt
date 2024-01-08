package ipt.lei.dam.ncrapp.models.didyouknow

/**
 *
 * Classe modelo para pedido à API de editar "sabiasque"
 *
 * id: identificador do sabias que
 * title: Titulo do sabias que
 * text: Texto do sabias que
 * references: Referências do sabias que
 * createdAt: datahora que o sabias que foi criado
 *
 */

data class DidYouKnowEditRequest(
    val id: Int,
    var title: String,
    var text: String,
    var references: String,
    val createdAt: String
)
