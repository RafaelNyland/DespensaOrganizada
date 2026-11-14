package br.com.dispensa

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Produto(
    val id: Long,
    val nome: String,
    val categoria: String,
    val codigo_barras: String,
    val conteudo_embalagem: Double,
    val unidade: String,
    val estoque_minimo: Int,
    val foto: ByteArray?
)

data class Movimentacao(
    val id: Long,
    val tipo_movimentacao: String,
    val local_guardado: String,
    val quantidade: Int,
    val data_hora: Long,
    val preco_unitario: Double,
    val data_validade: String
)

data class Estoque_Atual(
    val produto_id: Int,
    val local_guardado: String,
    val quantidade: Int,
    val data_validade: String
)
//Criação das tabelas do banco de dados
class ProdutoDatabase(context: Context) : SQLiteOpenHelper(context, "dispensa.db", null, 14) {
    override fun onCreate(db: SQLiteDatabase) {
        criarTabelaProduto(db)
        CriarTabelaMovimentacao(db)
        CriarTabelaEstoqueAtual(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS estoque_atual")
        db.execSQL("DROP TABLE IF EXISTS movimentacao")
        db.execSQL("DROP TABLE IF EXISTS produto")
        criarTabelaProduto(db)
        CriarTabelaMovimentacao(db)
        CriarTabelaEstoqueAtual(db)
    }

    private fun criarTabelaProduto(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS produto (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL,
                    categoria TEXT NOT NULL DEFAULT 'Alimentos',
                    codigo_barras TEXT NOT NULL DEFAULT '',
                    conteudo_embalagem REAL NOT NULL,
                    unidade TEXT NOT NULL,
                    estoque_minimo INTEGER NOT NULL DEFAULT 0,
                    foto BLOB
            )""".trimIndent()
        )
    }

    private fun CriarTabelaMovimentacao(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS movimentacao (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    produto_id INTEGER NOT NULL,
                    quantidade INTEGER NOT NULL,
                    data_hora INTEGER NOT NULL,
                    preco_unitario REAL NOT NULL DEFAULT 0,
                    data_validade TEXT NOT NULL DEFAULT '',
                    local_guardado TEXT NOT NULL,
                    tipo_movimentacao TEXT NOT NULL,
                    FOREIGN KEY (produto_id) REFERENCES produto(id)
            )""".trimIndent()
        )
    }
    private fun CriarTabelaEstoqueAtual(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS estoque_atual (
                    produto_id INTEGER NOT NULL,
                    local_guardado TEXT NOT NULL,
                    data_validade TEXT NOT NULL DEFAULT '',
                    quantidade INTEGER NOT NULL DEFAULT 0 CHECK (quantidade >= 0),
                    PRIMARY KEY (produto_id, local_guardado, data_validade),
                    FOREIGN KEY (produto_id) REFERENCES produto(id)
            )""".trimIndent()
        )
    }

// Funcoes de consulta e manipulacao de dados do banco de dados

        fun allProducts(): List<Produto> = queryProducts(null, null)

    fun searchProducts(search: String, category: String? = null): List<Produto> {
        val term = search.trim()
        val selection: String?
        val selectionArgs: Array<String>?
        if (term.isEmpty()) {
            selection = if (category == null) null else "categoria = ?"
            selectionArgs = category?.let { arrayOf(it) }
        } else if (category == null) {
            selection = "nome LIKE ? OR codigo_barras = ?"
            selectionArgs = arrayOf("%$term%", term)
        } else {
            selection = "(nome LIKE ? OR codigo_barras = ?) AND categoria = ?"
            selectionArgs = arrayOf("%$term%", term, category)
        }
        return queryProducts(selection, selectionArgs)
    }

//     fun productsWithMovements(): List<ProductMovementSummary> {
//         val grouped = linkedMapOf<Long, Pair<Product, MutableList<MovementRecord>>>()
//         readableDatabase.rawQuery(
//             """SELECT p.id AS product_id, p.name, p.category, p.barcode, p.package_content, p.unit,
//                       p.minimum_stock, p.photo, m.id AS movement_id, m.movement_type, m.quantity,
//                       m.created_at, m.unit_price, m.expiry_date
//                FROM products p
//                INNER JOIN movements m ON m.product_id = p.id
//                ORDER BY p.name COLLATE NOCASE ASC, m.created_at DESC""".trimIndent(),
//             null
//         ).use { cursor ->
//             while (cursor.moveToNext()) {
//                 val productId = cursor.getLong(cursor.getColumnIndexOrThrow("product_id"))
//                 val productAndMovements = grouped.getOrPut(productId) {
//                     val photoIndex = cursor.getColumnIndexOrThrow("photo")
//                     val product = Product(
//                         id = productId,
//                         name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
//                         category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
//                         barcode = cursor.getString(cursor.getColumnIndexOrThrow("barcode")),
//                         packageContent = cursor.getDouble(cursor.getColumnIndexOrThrow("package_content")),
//                         unit = cursor.getString(cursor.getColumnIndexOrThrow("unit")),
//                         minimumStock = cursor.getInt(cursor.getColumnIndexOrThrow("minimum_stock")),
//                         photo = if (cursor.isNull(photoIndex)) null else cursor.getBlob(photoIndex)
//                     )
//                     product to mutableListOf()
//                 }
//                 productAndMovements.second += MovementRecord(
//                     id = cursor.getLong(cursor.getColumnIndexOrThrow("movement_id")),
//                     type = cursor.getString(cursor.getColumnIndexOrThrow("movement_type")),
//                     quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
//                     createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at")),
//                     unitPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("unit_price")),
//                     expiryDate = cursor.getString(cursor.getColumnIndexOrThrow("expiry_date"))
//                 )
//             }
//         }
//         return grouped.values.map { ProductMovementSummary(it.first, it.second) }
//     }

    private fun queryProducts(selection: String?, selectionArgs: Array<String>?): List<Produto> {
        val result = mutableListOf<Produto>()
        readableDatabase.query("produto", null, selection, selectionArgs, null, null, "nome COLLATE NOCASE ASC").use { cursor ->
            while (cursor.moveToNext()) {
                result += Produto(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                    categoria = cursor.getString(cursor.getColumnIndexOrThrow("categoria")),
                    codigo_barras = cursor.getString(cursor.getColumnIndexOrThrow("codigo_barras")),
                    conteudo_embalagem = cursor.getDouble(cursor.getColumnIndexOrThrow("conteudo_embalagem")),
                    unidade = cursor.getString(cursor.getColumnIndexOrThrow("unidade")),
                    estoque_minimo = cursor.getInt(cursor.getColumnIndexOrThrow("estoque_minimo")),
                    foto = cursor.getBlob(cursor.getColumnIndexOrThrow("foto"))
                )
            }
        }
        return result
    }

//     fun currentStock(productId: Long): Int {
//         readableDatabase.rawQuery(
//             "SELECT COALESCE(SUM(CASE WHEN movement_type = 'IN' THEN quantity ELSE -quantity END), 0) FROM movements WHERE product_id = ?",
//             arrayOf(productId.toString())
//         ).use { cursor -> return if (cursor.moveToFirst()) cursor.getInt(0) else 0 }
//     }

// movimentacao de retirada
        fun addMovementacaoRetirada(
            produtoId: Long,
            local_guardado: String,
            quantidade: Int
        ): Boolean {
            if (quantidade <= 0) return false

            val db = writableDatabase
            db.beginTransaction()
            try {
                //lista de lotes disponíveis para o produto no local especificado, ordenados por validade
                val lotes = mutableListOf<Pair<String, Int>>()
                //consulta os lotes do produto para o local
                db.query(
                    "estoque_atual",
                    arrayOf("data_validade", "quantidade"),
                    "produto_id = ? AND local_guardado = ? AND quantidade > 0",
                    arrayOf(produtoId.toString(), local_guardado),
                    null,
                    null,
                    //deixa as datas da mais proxima para a mais distante
                    "CASE WHEN data_validade = '' THEN 1 ELSE 0 END, data_validade ASC"
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        lotes += cursor.getString(0) to cursor.getInt(1)
                    }
                }
                // soma as quantidades encontradas, se o total for menor que a quantidade pedida, retorna false
                val totalDisponivel = lotes.sumOf { it.second }
                if (quantidade > totalDisponivel) return false

                var faltaRetirar = quantidade
                // percorre cada lote e retira so a quantidade disponivel
                for ((validade, disponivel) in lotes) {
                    if (faltaRetirar == 0) break

                    val retiradaDesteLote = minOf(faltaRetirar, disponivel)
                    val restante = disponivel - retiradaDesteLote

                    val valoresEstoque = ContentValues().apply {
                        put("quantidade", restante)
                    }
                    //verifica se a quantidade restante é 0, se for, deleta o registro do estoque atual, caso contrario atualiza a quantidade
                        if (restante == 0) {
                            db.delete(
                                "estoque_atual",
                                "produto_id = ? AND local_guardado = ? AND data_validade = ?",
                                arrayOf(produtoId.toString(), local_guardado, validade)
                            )
                        } else {
                            val valoresEstoque = ContentValues().apply {
                                put("quantidade", restante)
                            }
                            db.update(
                                "estoque_atual",
                                valoresEstoque,
                                "produto_id = ? AND local_guardado = ? AND data_validade = ?",
                                arrayOf(produtoId.toString(), local_guardado, validade)
                            )
                        }
                    //salva a movimentacao de retirada no banco de dados
                    val valoresMovimentacao = ContentValues().apply {
                        put("produto_id", produtoId)
                        put("tipo_movimentacao", "RETIRADA")
                        put("local_guardado", local_guardado)
                        put("quantidade", retiradaDesteLote)
                        put("data_hora", System.currentTimeMillis())
                        put("preco_unitario", 0.0)
                        put("data_validade", validade)
                    }
                    db.insertOrThrow("movimentacao", null, valoresMovimentacao)

                    faltaRetirar -= retiradaDesteLote
                }

                db.setTransactionSuccessful()
                return true
            } finally {
                db.endTransaction()
            }
        }
    
    fun addMovementacaoEntrada(
        produtoId: Long,
        local_guardado: String,
        quantidade: Int,
        preco_unitario: Double,
        data_validade: String
    ): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val values = ContentValues().apply {
                put("produto_id", produtoId)
                put("tipo_movimentacao", "ENTRADA")
                put("quantidade", quantidade)
                put("data_hora", System.currentTimeMillis())
                put("preco_unitario", preco_unitario)
                put("data_validade", data_validade)
                put("local_guardado", local_guardado)
            }
            db.insertOrThrow("movimentacao", null, values)
            atualizarEstoqueAtual(
                db = db,
                produtoId = produtoId,
                localGuardado = local_guardado,
                quantidade = quantidade,
                tipoMovimentacao = "ENTRADA",
                dataValidade = data_validade
            )
            db.setTransactionSuccessful()
            return true
        } finally {
            db.endTransaction()
        }
    }
    private fun atualizarEstoqueAtual(
        db: SQLiteDatabase,
        produtoId: Long,
        localGuardado: String,
        quantidade: Int,
        tipoMovimentacao: String,
        dataValidade: String
    ) {
        val atual = db.query(
            "estoque_atual",
            arrayOf("quantidade"),
            "produto_id = ? AND local_guardado = ? AND data_validade = ?",
            arrayOf(produtoId.toString(), localGuardado, dataValidade),
            null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }

        val novaQuantidade = when (tipoMovimentacao) {
            "ENTRADA" -> atual + quantidade
            "RETIRADA" -> atual - quantidade
            else -> throw IllegalArgumentException("Tipo de movimentação inválido")
        }

        if (novaQuantidade < 0) {
            throw IllegalStateException("Estoque insuficiente para essa validade e local")
        }

        val valores = ContentValues().apply {
            put("produto_id", produtoId)
            put("local_guardado", localGuardado)
            put("data_validade", dataValidade)
            put("quantidade", novaQuantidade)
        }

        if (atual == 0) {
            db.insertOrThrow("estoque_atual", null, valores)
        } else {
            db.update(
                "estoque_atual",
                valores,
                "produto_id = ? AND local_guardado = ? AND data_validade = ?",
                arrayOf(produtoId.toString(), localGuardado, dataValidade)
            )
        }
    }

//cadastra um novo produto no banco de dados

    fun cadastrarProduto(product: Produto) {
        val values = ContentValues().apply {
            put("nome", product.nome.trim())
            put("categoria", product.categoria)
            put("codigo_barras", product.codigo_barras.trim())
            put("conteudo_embalagem", product.conteudo_embalagem)
            put("unidade", product.unidade)
            put("estoque_minimo", product.estoque_minimo)
            product.foto?.let { put("foto", it) } ?: putNull("foto")
        }
        writableDatabase.insertOrThrow("produto", null, values)
    }
//Verifica se o nome do produto já existe no banco de dados
    fun nomeExiste(name: String): Boolean {
        readableDatabase.query(
            "produto", arrayOf("id"), "LOWER(TRIM(nome)) = LOWER(TRIM(?))", arrayOf(name.trim()), null, null, null, "1"
        ).use { return it.moveToFirst() }
    }
//Verifica se o codigo do produto já existe no banco de dados

    fun barcodeExists(barcode: String): Boolean {
        val normalizedBarcode = barcode.trim()
        if (normalizedBarcode.isEmpty()) return false
        readableDatabase.query(
            "produto", arrayOf("id"), "LOWER(TRIM(codigo_barras)) = LOWER(TRIM(?))", arrayOf(normalizedBarcode), null, null, null, "1"
        ).use { return it.moveToFirst() }
    }

//     fun deleteProduct(id: Long) {
//         val db = writableDatabase
//         db.beginTransaction()
//         try {
//             db.delete("movements", "product_id = ?", arrayOf(id.toString()))
//             db.delete("products", "id = ?", arrayOf(id.toString()))
//             db.setTransactionSuccessful()
//         } finally {
//             db.endTransaction()
//         }
//     }
}
