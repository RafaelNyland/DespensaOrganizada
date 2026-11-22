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

data class ProdutoEmFalta(
    val id: Long,
    val nome: String,
    val categoria: String,
    val unidade: String,
    val foto: ByteArray?,
    val estoqueAtual: Int,
    val estoqueMinimo: Int,
    val quantidadeFaltando: Int
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

data class ItemHistorico(
    val nomeProduto: String,
    val tipoMovimentacao: String,
    val quantidade: Int,
    val dataHora: Long,
    val precoUnitario: Double,
    val localGuardado: String,
    val saldoAtual: Int
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

    fun allProducts(): List<Produto> = consultaProducts(null, null)

    // total de produtos no estoque em todos os locais
    fun totalEstoqueProduto(produtoId: Long): Int = readableDatabase.rawQuery(
        "SELECT COALESCE(SUM(quantidade), 0) FROM estoque_atual WHERE produto_id = ?",
        arrayOf(produtoId.toString())
    ).use { cursor ->
        if (cursor.moveToFirst()) cursor.getInt(0) else 0
    }

    fun totalEstoque(): List<ContentValues> {
        val itens = mutableListOf<ContentValues>()

        readableDatabase.rawQuery(
            "SELECT * FROM estoque_atual",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val item = ContentValues()

                for (i in 0 until cursor.columnCount) {
                    val nomeColuna = cursor.getColumnName(i)

                    when (cursor.getType(i)) {
                        android.database.Cursor.FIELD_TYPE_INTEGER ->
                            item.put(nomeColuna, cursor.getLong(i))

                        android.database.Cursor.FIELD_TYPE_FLOAT ->
                            item.put(nomeColuna, cursor.getDouble(i))

                        android.database.Cursor.FIELD_TYPE_STRING ->
                            item.put(nomeColuna, cursor.getString(i))

                        android.database.Cursor.FIELD_TYPE_BLOB ->
                            item.put(nomeColuna, cursor.getBlob(i))

                        android.database.Cursor.FIELD_TYPE_NULL ->
                            item.putNull(nomeColuna)
                    }
                }

                itens.add(item)
            }
        }

        return itens
    }

    fun listarProdutosEmFalta(): List<ProdutoEmFalta> {
        val produtos = mutableListOf<ProdutoEmFalta>()
        readableDatabase.rawQuery(
            """SELECT p.id, p.nome, p.categoria, p.unidade, p.foto, p.estoque_minimo,
                      COALESCE(SUM(e.quantidade), 0) AS estoque_atual
               FROM produto p
               LEFT JOIN estoque_atual e ON e.produto_id = p.id
               GROUP BY p.id, p.nome, p.categoria, p.unidade, p.estoque_minimo
               HAVING COALESCE(SUM(e.quantidade), 0) < p.estoque_minimo
               ORDER BY p.categoria COLLATE NOCASE, p.nome COLLATE NOCASE""".trimIndent(),
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val atual = cursor.getInt(cursor.getColumnIndexOrThrow("estoque_atual"))
                val minimo = cursor.getInt(cursor.getColumnIndexOrThrow("estoque_minimo"))
                produtos += ProdutoEmFalta(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                    categoria = cursor.getString(cursor.getColumnIndexOrThrow("categoria")),
                    unidade = cursor.getString(cursor.getColumnIndexOrThrow("unidade")),
                    foto = cursor.getBlob(cursor.getColumnIndexOrThrow("foto")),
                    estoqueAtual = atual,
                    estoqueMinimo = minimo,
                    quantidadeFaltando = minimo - atual
                )
            }
        }
        return produtos
    }

    fun listarMovimentacoesHistorico(ano: Int, mes: Int, busca: String = ""): List<ItemHistorico> {
        val inicioPeriodo = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, ano)
            set(java.util.Calendar.MONTH, mes)
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val fimPeriodo = inicioPeriodo.clone() as java.util.Calendar
        fimPeriodo.add(java.util.Calendar.MONTH, 1)
        val condicaoBusca = if (busca.isBlank()) "" else " AND p.nome LIKE ? COLLATE NOCASE"
        val parametrosConsulta = mutableListOf(
            inicioPeriodo.timeInMillis.toString(),
            fimPeriodo.timeInMillis.toString()
        )
        if (busca.isNotBlank()) parametrosConsulta.add("%${busca.trim()}%")

        val registrosHistorico = mutableListOf<ItemHistorico>()
        readableDatabase.rawQuery(
            """SELECT p.nome, m.tipo_movimentacao, m.quantidade, m.data_hora,
                      m.preco_unitario, m.local_guardado,
                      COALESCE((SELECT SUM(e.quantidade) FROM estoque_atual e WHERE e.produto_id = p.id), 0) AS saldo
               FROM movimentacao m
               INNER JOIN produto p ON p.id = m.produto_id
               WHERE m.data_hora >= ? AND m.data_hora < ?$condicaoBusca
               ORDER BY m.data_hora DESC, m.id DESC""".trimIndent(),
            parametrosConsulta.toTypedArray()
        ).use { cursorHistorico ->
            while (cursorHistorico.moveToNext()) {
                registrosHistorico += ItemHistorico(
                    nomeProduto = cursorHistorico.getString(0),
                    tipoMovimentacao = cursorHistorico.getString(1),
                    quantidade = cursorHistorico.getInt(2),
                    dataHora = cursorHistorico.getLong(3),
                    precoUnitario = cursorHistorico.getDouble(4),
                    localGuardado = cursorHistorico.getString(5),
                    saldoAtual = cursorHistorico.getInt(6)
                )
            }
        }
        return registrosHistorico
    }

    // totol de produtos no estoque no local
    fun totalEstoqueProdutoLocal(produtoId: Long, local: String): Int = readableDatabase.rawQuery(
        "SELECT COALESCE(SUM(quantidade), 0) FROM estoque_atual WHERE produto_id = ? AND local_guardado = ?",
        arrayOf(produtoId.toString(), local)
    ).use { cursor ->
        if (cursor.moveToFirst()) cursor.getInt(0) else 0
    }

    // funcao para a validade mais proxima do produto no local
    fun validadeMaisProxima(produtoId: Long, local: String): String? = readableDatabase.rawQuery(
        "SELECT data_validade FROM estoque_atual WHERE produto_id = ? AND local_guardado = ? AND quantidade > 0 AND data_validade <> '' ORDER BY data_validade ASC LIMIT 1",
        arrayOf(produtoId.toString(), local)
    ).use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }

    fun procurarProdutos(search: String, category: String? = null): List<Produto> {
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
        return consultaProducts(selection, selectionArgs)
    }

    private fun consultaProducts(selection: String?, selectionArgs: Array<String>?): List<Produto> {
        val result = mutableListOf<Produto>()
        readableDatabase.query("produto", null, selection, selectionArgs, null, null, "nome COLLATE NOCASE ASC")
            .use { cursor ->
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

    fun listarLocaisPorProduto(produtoId: Long): List<String> {
        val result = mutableListOf<String>()
        readableDatabase.query(
            "estoque_atual",
            arrayOf("local_guardado"),
            "produto_id = ? AND quantidade > 0",
            arrayOf(produtoId.toString()),
            "local_guardado",
            null,
            "local_guardado COLLATE NOCASE ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += cursor.getString(0)
            }
        }
        return result
    }

    fun listarQuantidadePorProdutoLocaleValidade(produtoId: Long, local: String, validade: String): Int {
        val result = readableDatabase.rawQuery(
            "SELECT quantidade FROM estoque_atual WHERE produto_id = ? AND local_guardado = ? AND data_validade = ?",
            arrayOf(produtoId.toString(), local, validade)
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
        return result
    }

    fun listarDataValidadePorProdutoLocal(produtoId: Long, local: String): List<String> {
        val result = mutableListOf<String>()
        readableDatabase.query(
            "estoque_atual",
            arrayOf("data_validade"),
            "produto_id = ? AND local_guardado = ? AND quantidade > 0",
            arrayOf(produtoId.toString(), local),
            null,
            null,
            "CASE WHEN data_validade = '' THEN 1 ELSE 0 END, data_validade ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += cursor.getString(0)
            }
        }
        return result
    }

    // movimentacao de retirada
    fun addMovementacaoRetirada(
        produtoId: Long,
        local_guardado: String,
        quantidade: Int,
        data_validade: String
    ): Boolean {
        if (quantidade <= 0) return false

        val db = writableDatabase
        db.beginTransaction()

        try {
            // Busca a quantidade disponível no lote selecionado.
            val disponivel = db.query(
                "estoque_atual",
                arrayOf("quantidade"),
                "produto_id = ? AND local_guardado = ? AND data_validade = ?",
                arrayOf(produtoId.toString(), local_guardado, data_validade),
                null,
                null,
                null
            ).use { cursor ->
                if (cursor.moveToFirst()) cursor.getInt(0) else 0
            }

            // Não retira se o lote não existir ou não tiver quantidade suficiente.
            if (quantidade > disponivel) return false

            val restante = disponivel - quantidade

            if (restante == 0) {
                db.delete(
                    "estoque_atual",
                    "produto_id = ? AND local_guardado = ? AND data_validade = ?",
                    arrayOf(produtoId.toString(), local_guardado, data_validade)
                )
            } else {
                val valoresEstoque = ContentValues().apply {
                    put("quantidade", restante)
                }

                db.update(
                    "estoque_atual",
                    valoresEstoque,
                    "produto_id = ? AND local_guardado = ? AND data_validade = ?",
                    arrayOf(produtoId.toString(), local_guardado, data_validade)
                )
            }

            val valoresMovimentacao = ContentValues().apply {
                put("produto_id", produtoId)
                put("tipo_movimentacao", "RETIRADA")
                put("local_guardado", local_guardado)
                put("quantidade", quantidade)
                put("data_hora", System.currentTimeMillis())
                put("preco_unitario", 0.0)
                put("data_validade", data_validade)
            }

            db.insertOrThrow("movimentacao", null, valoresMovimentacao)

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

    fun buscarProdutoPorId(id: Long): Produto? =
        consultaProducts("id = ?", arrayOf(id.toString())).firstOrNull()

    fun buscarProdutoPorCodigoBarras(codigoBarras: String): Produto? =
        consultaProducts(
            "codigo_barras = ?",
            arrayOf(codigoBarras.trim())
        ).firstOrNull()

    fun atualizarProduto(produto: Produto): Boolean {
        val valores = ContentValues().apply {
            put("nome", produto.nome.trim())
            put("categoria", produto.categoria)
            put("codigo_barras", produto.codigo_barras.trim())
            put("conteudo_embalagem", produto.conteudo_embalagem)
            put("unidade", produto.unidade)
            put("estoque_minimo", produto.estoque_minimo)
            produto.foto?.let { put("foto", it) } ?: putNull("foto")
        }
        return writableDatabase.update(
            "produto",
            valores,
            "id = ?",
            arrayOf(produto.id.toString())
        ) > 0
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
            "produto",
            arrayOf("id"),
            "LOWER(TRIM(codigo_barras)) = LOWER(TRIM(?))",
            arrayOf(normalizedBarcode),
            null,
            null,
            null,
            "1"
        ).use { return it.moveToFirst() }
    }

    // verifica se o codigo de barras e valido usando o codigo verificador

}



