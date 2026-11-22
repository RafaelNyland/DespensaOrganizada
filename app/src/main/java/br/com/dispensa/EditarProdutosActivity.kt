package br.com.dispensa

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import java.io.ByteArrayOutputStream

class EditarProdutosActivity : Activity() {
    private val verdeEscuro = 0xFF10564F.toInt()
    private val verde = 0xFF11665D.toInt()
    private val fundo = 0xFFF4F7F6.toInt()
    private val cinza = 0xFF71817D.toInt()
    private val linha = 0xFFE7ECEA.toInt()
    private lateinit var bancoDados: ProdutoDatabase
    private lateinit var codigobarraInput: EditText
    private lateinit var imagemProduto: ImageView
    private var produtoEditado: Produto? = null
    private var novaFotoProduto: ByteArray? = null

    companion object {
        private const val SOLICITACAO_FOTO = 2102
    }

    override fun onCreate(estadoSalvo: Bundle?) {
        super.onCreate(estadoSalvo)
        bancoDados = ProdutoDatabase(this)
        val idProduto = intent.getLongExtra("produto_id", -1L)
        produtoEditado = bancoDados.buscarProdutoPorId(idProduto)

        if (produtoEditado == null) {
            Toast.makeText(this, "Produto não encontrado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        montarTelaEdicao(produtoEditado!!)
    }

    private fun montarTelaEdicao(produto: Produto) {
        val raiz = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(fundo)
        }
        val cabecalho = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(36), dp(16), dp(14))
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        cabecalho.addView(botao("<-", verdeEscuro) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }, LinearLayout.LayoutParams(dp(44), dp(44)))

        cabecalho.addView(TextView(this).apply {
            text = "Editar produtos"
            textSize = 22f
            setTextColor(verdeEscuro)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(10), 0, 0)
        })
        cabecalho.addView(TextView(this).apply {
            text = "Edite os dados dos produtos salvos"
            textSize = 12f
            setTextColor(cinza)
            setPadding(0, dp(3), 0, 0)
        })
        raiz.addView(cabecalho)

        val rolagem = ScrollView(this).apply { isFillViewport = true }
        val conteudo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(14), dp(12), dp(18))
        }
        rolagem.addView(conteudo)
        raiz.addView(rolagem, LinearLayout.LayoutParams(-1, 0, 1f))

        val resumoProduto = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(10), dp(10), dp(10))
            background = forma(0xFFFFFFFF.toInt(), linha, 12)
        }
        imagemProduto = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            contentDescription = "Foto de ${produto.nome}"
            background = forma(0xFFE7F2EF.toInt(), 0xFFE7F2EF.toInt(), 8)
            clipToOutline = true
            val foto = produto.foto
            if (foto != null && foto.isNotEmpty()) {
                setImageBitmap(BitmapFactory.decodeByteArray(foto, 0, foto.size))
            } else {
                setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
        resumoProduto.addView(imagemProduto, LinearLayout.LayoutParams(dp(44), dp(44)))
        resumoProduto.addView(TextView(this).apply {
            text = produto.nome
            textSize = 13f
            setTextColor(verdeEscuro)
            setTypeface(null, Typeface.BOLD)
            setPadding(dp(10), 0, 0, 0)
        })
        conteudo.addView(resumoProduto, LinearLayout.LayoutParams(-1, dp(66)).apply {
            bottomMargin = dp(14)
        })

        conteudo.addView(rotulo("Informações do produto"), LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = dp(8)
        })
        conteudo.addView(rotuloCampo("Nome do produto"))
        val nomeInput = campo("Nome do produto").apply { setText(produto.nome) }
        conteudo.addView(nomeInput, campoParams())

        conteudo.addView(rotuloCampo("Categoria"))
        val categorias = (listOf(
            "Alimentos", "Frios", "Frutas", "Legumes", "Congelados", "Limpeza", "Higiene", "Medicamentos"
        ) + produto.categoria).distinct()
        val categoria = criarSeletor(categorias)
        selecionarOpcao(categoria, categorias, produto.categoria)
        conteudo.addView(categoria, campoParams())

        val painelCodigo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(8), dp(10), dp(8))
            background = forma(0xFFE7F2EF.toInt(), 0xFFE7F2EF.toInt(), 10)
        }
        painelCodigo.addView(TextView(this).apply {
            text = "▥   Escanear código de barras"
            textSize = 12f
            setTextColor(verde)
            setTypeface(null, Typeface.BOLD)
        })
        painelCodigo.addView(TextView(this).apply {
            text = "Ou preencha os dados abaixo."
            textSize = 10f
            setTextColor(cinza)
        })
        val linhaCodigo = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(6), 0, 0)
        }
        codigobarraInput = campo("Código atual").apply { setText(produto.codigo_barras) }
        linhaCodigo.addView(codigobarraInput, LinearLayout.LayoutParams(0, dp(42), 1f))
        linhaCodigo.addView(
            botao("LER CÓDIGO", verdeEscuro) { lerCodigoBarras() },
            LinearLayout.LayoutParams(dp(104), dp(42)).apply { leftMargin = dp(6) })
        painelCodigo.addView(linhaCodigo)
        conteudo.addView(painelCodigo, LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = dp(10)
        })

        conteudo.addView(rotuloCampo("Foto do produto"))
        conteudo.addView(
            botao("TIRAR FOTO", verdeEscuro) { tirarFoto() },
            LinearLayout.LayoutParams(dp(112), dp(42)).apply { bottomMargin = dp(10) })

        val linhaConteudo = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val colunaConteudo = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        colunaConteudo.addView(rotuloCampo("Conteúdo por embalagem"))
        val conteudoEmbalagemInput = campo(
            "Conteúdo",
            android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        ).apply {
            setText(produto.conteudo_embalagem.toString().removeSuffix(".0"))
        }
        colunaConteudo.addView(conteudoEmbalagemInput, campoParams())

        val colunaUnidade = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), 0, 0, 0)
        }
        colunaUnidade.addView(rotuloCampo("Unidade"))
        val unidades = (listOf("Unidade (un.)", "g", "kg", "ml", "l") + produto.unidade).distinct()
        val unidade = criarSeletor(unidades)
        selecionarOpcao(unidade, unidades, produto.unidade)
        colunaUnidade.addView(unidade, campoParams())
        linhaConteudo.addView(colunaConteudo, LinearLayout.LayoutParams(0, -2, 1f))
        linhaConteudo.addView(colunaUnidade, LinearLayout.LayoutParams(0, -2, 1f))
        conteudo.addView(linhaConteudo)

        conteudo.addView(rotuloCampo("Estoque mínimo"))
        val estoqueMinimo = campo("Quantidade mínima", android.text.InputType.TYPE_CLASS_NUMBER).apply {
            setText(produto.estoque_minimo.toString())
        }
        conteudo.addView(estoqueMinimo, campoParams())

        val botaoSalvar = botao("✓  Salvar alterações", verdeEscuro) {
            salvarAlteracoes(produto, nomeInput, categoria, conteudoEmbalagemInput, unidade, estoqueMinimo)
        }.apply { textSize = 13f }
        conteudo.addView(botaoSalvar, LinearLayout.LayoutParams(-1, dp(48)).apply {
            topMargin = dp(2)
        })

        setContentView(raiz)
    }

    private fun salvarAlteracoes(
        produto: Produto,
        nomeInput: EditText,
        categoria: Spinner,
        conteudoEmbalagemInput: EditText,
        unidade: Spinner,
        estoqueMinimo: EditText
    ) {
        val nome = nomeInput.text.toString().trim()
        val codigoBarras = codigobarraInput.text.toString().trim()
        val conteudoEmbalagem = conteudoEmbalagemInput.text.toString().replace(',', '.').toDoubleOrNull()
        val quantidadeMinima = estoqueMinimo.text.toString().toIntOrNull()

        if (nome.isBlank()) {
            nomeInput.error = "Informe o nome do produto"
            return
        }
        val nomeDuplicado = bancoDados.allProducts().any {
            it.id != produto.id && it.nome.trim().equals(nome, ignoreCase = true)
        }
        if (nomeDuplicado) {
            AlertDialog.Builder(this)
                .setTitle("Produto já cadastrado")
                .setMessage("Já existe outro produto com esse nome.")
                .setPositiveButton("OK", null)
                .show()
            return
        }
        if (codigoBarras.isNotEmpty() && bancoDados.allProducts().any {
                it.id != produto.id && it.codigo_barras.trim() == codigoBarras
            }) {
            AlertDialog.Builder(this)
                .setTitle("Código de barras já cadastrado")
                .setMessage("Esse código pertence a outro produto salvo.")
                .setPositiveButton("OK", null)
                .show()
            codigobarraInput.requestFocus()
            return
        }
        if (conteudoEmbalagem == null || conteudoEmbalagem <= 0) {
            conteudoEmbalagemInput.error = "Informe um conteúdo maior que zero"
            return
        }
        if (quantidadeMinima == null || quantidadeMinima < 0) {
            estoqueMinimo.error = "Informe um mínimo válido"
            return
        }

        val produtoAtualizado = produto.copy(
            nome = nome,
            categoria = categoria.selectedItem.toString(),
            codigo_barras = codigoBarras,
            conteudo_embalagem = conteudoEmbalagem,
            unidade = unidade.selectedItem.toString(),
            estoque_minimo = quantidadeMinima,
            foto = novaFotoProduto ?: produto.foto
        )
        if (bancoDados.atualizarProduto(produtoAtualizado)) {
            AlertDialog.Builder(this)
                .setTitle("Alterações salvas")
                .setMessage("Os dados do produto foram atualizados.")
                .setPositiveButton("OK") { _, _ -> finish() }
                .show()
        } else {
            Toast.makeText(this, "Não foi possível salvar as alterações.", Toast.LENGTH_LONG).show()
        }
    }

    private fun lerCodigoBarras() {
        val opcoesLeitor = GmsBarcodeScannerOptions.Builder().enableAutoZoom().build()
        GmsBarcodeScanning.getClient(this, opcoesLeitor).startScan()
            .addOnSuccessListener { codigoLido ->
                codigobarraInput.setText(codigoLido.rawValue.orEmpty())
            }
            .addOnFailureListener {
                Toast.makeText(this, "Não foi possível abrir o leitor.", Toast.LENGTH_LONG).show()
            }
    }

    private fun tirarFoto() {
        try {
            startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), SOLICITACAO_FOTO)
        } catch (_: Exception) {
            Toast.makeText(this, "Não foi encontrado um app de câmera.", Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Usado para receber a foto da câmera")
    override fun onActivityResult(codigoSolicitacao: Int, resultado: Int, dados: Intent?) {
        super.onActivityResult(codigoSolicitacao, resultado, dados)
        if (codigoSolicitacao == SOLICITACAO_FOTO && resultado == RESULT_OK) {
            val foto = dados?.extras?.get("data") as? Bitmap
            if (foto != null) {
                imagemProduto.setImageBitmap(foto)
                val fluxoFoto = ByteArrayOutputStream()
                foto.compress(Bitmap.CompressFormat.JPEG, 85, fluxoFoto)
                novaFotoProduto = fluxoFoto.toByteArray()
            } else {
                Toast.makeText(this, "Não foi possível carregar a foto.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun criarSeletor(opcoes: List<String>) = Spinner(this).apply {
        adapter = ArrayAdapter(this@EditarProdutosActivity, android.R.layout.simple_spinner_dropdown_item, opcoes)
        background = forma(0xFFFFFFFF.toInt(), linha, 8)
    }

    private fun selecionarOpcao(seletor: Spinner, opcoes: List<String>, valor: String) {
        val indice = opcoes.indexOf(valor).let { if (it >= 0) it else 0 }
        seletor.setSelection(indice)
    }

    private fun rotulo(texto: String) = TextView(this).apply {
        text = texto
        textSize = 14f
        setTextColor(verdeEscuro)
        setTypeface(null, Typeface.BOLD)
    }

    private fun rotuloCampo(texto: String) = TextView(this).apply {
        text = texto
        textSize = 10f
        setTextColor(cinza)
        setPadding(0, dp(2), 0, dp(4))
    }

    private fun campo(dica: String, tipo: Int = android.text.InputType.TYPE_CLASS_TEXT) = EditText(this).apply {
        hint = dica
        inputType = tipo
        textSize = 12f
        setPadding(dp(10), 0, dp(10), 0)
        background = forma(0xFFFFFFFF.toInt(), linha, 8)
    }

    private fun botao(
        texto: String,
        cor: Int,
        aoClicar: () -> Unit
    ): Button = Button(this).apply {
        text = texto
        setTextColor(0xFFFFFFFF.toInt())
        setBackgroundColor(cor)
        background = android.graphics.drawable.GradientDrawable().apply {
            setColor(cor)
            cornerRadius = dp(12).toFloat()
        }
        setOnClickListener { aoClicar() }
    }

    private fun campoParams() = LinearLayout.LayoutParams(-1, dp(42)).apply {
        bottomMargin = dp(8)
    }

    private fun forma(cor: Int, borda: Int, raioDp: Int) = GradientDrawable().apply {
        setColor(cor)
        cornerRadius = dp(raioDp).toFloat()
        setStroke(dp(1), borda)
    }

    private fun dp(valor: Int): Int = (valor * resources.displayMetrics.density).toInt()
}
