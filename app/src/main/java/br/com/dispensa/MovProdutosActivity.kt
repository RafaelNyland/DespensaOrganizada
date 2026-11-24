package br.com.dispensa

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import android.graphics.BitmapFactory
import android.widget.ImageView
import android.widget.DatePicker
import android.text.InputType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.Spinner
import java.io.ByteArrayOutputStream

class MovProdutosActivity : Activity() {
    private val verdeEscuro = 0xFF10564F.toInt()
    private val verde = 0xFF11665D.toInt()
    private val fundo = 0xFFF4F7F6.toInt()
    private val cinza = 0xFF71817D.toInt()
    private val linha = 0xFFE7ECEA.toInt()
    private lateinit var codigobarraInput: EditText
    private lateinit var bancoDados: ProdutoDatabase
    private lateinit var cartaoLista: LinearLayout
    private var categoriaSelecionada: String? = null
    private lateinit var textoQuantidade: TextView
    private lateinit var buscaInput: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bancoDados = ProdutoDatabase(this)
        val id = intent.getLongExtra("produto_id", -1L)
        val nome = intent.getStringExtra("produto_nome").orEmpty()
        val tipoMovimentacao = intent.getStringExtra("tipo_movimentacao").orEmpty()
        if (tipoMovimentacao.isNotEmpty()) {
            if (tipoMovimentacao == "ENTRADA") {
                montarTelaEntrada(id, nome)

            } else if (tipoMovimentacao == "RETIRADA") {
                if (bancoDados.totalEstoqueProduto(id) <= 0) {
                    Toast.makeText(this, "Não há estoque disponível para retirada.", Toast.LENGTH_SHORT).show()
                    montarTelaEmpty("Não há estoque disponível para retirada.")
                } else {
                    montarTelaRetirada(id, nome)
                }
            }
        } else {
            montarTelaEmpty("Aconteceu alguns erros ao localizar o produto. Por favor, tente novamente.")
        }
    }

    private fun montarTelaEmpty(erro: String) {
        val raiz = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(fundo)
        }

        val cabecalho = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(50), dp(16), dp(20))
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        cabecalho.addView(botao("<-", verdeEscuro) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }, LinearLayout.LayoutParams(dp(44), dp(44)))

        cabecalho.addView(TextView(this).apply {
            text = "ERROR "
            textSize = 22f
            setTextColor(0xFF1D3936.toInt())
            setTypeface(null, Typeface.BOLD)
        })
        cabecalho.addView(TextView(this).apply {
            text = erro
            textSize = 12f
            setTextColor(cinza)
            setPadding(0, dp(4), 0, 0)
        })
        raiz.addView(cabecalho)

        cartaoLista = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), 0, dp(8), 0)
            background = forma(0xFFFFFFFF.toInt(), 0xFFEEF1F0.toInt(), 12)
        }
        raiz.addView(cartaoLista)

        setContentView(raiz)

    }

    private fun montarTelaEntrada(id: Long, nome: String) {
        val raiz = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(fundo)
        }
        val cabecalho = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(50), dp(16), dp(20))
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        cabecalho.addView(TextView(this).apply {
            text = "Entrada de produtos"
            textSize = 22f
            setTextColor(0xFF1D3936.toInt())
            setTypeface(null, Typeface.BOLD)
        })
        cabecalho.addView(TextView(this).apply {
            text = "Informe os dados da entrada do produto."
            textSize = 12f
            setTextColor(cinza)
            setPadding(0, dp(4), 0, 0)
        })
        raiz.addView(cabecalho)

        val produtoEncontrado = bancoDados.allProducts().firstOrNull { it.id == id }

        if (produtoEncontrado == null) {
            Toast.makeText(this, "Produto não encontrado", Toast.LENGTH_SHORT).show()
            setContentView(raiz)
            return
        }
        val scroll = ScrollView(this)
        val conteudo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(18))
        }
        scroll.addView(conteudo)
        raiz.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val nomeProduto = produtoEncontrado.nome.ifBlank { nome }
        conteudo.addView(TextView(this).apply {
            text = "Produto selecionado"
            textSize = 14f
            setTextColor(0xFF243C39.toInt())
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(8))
        })
        val cartaoProduto = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = forma(0xFFFFFFFF.toInt(), linha, 12)
        }
        val foto = produtoEncontrado.foto
        val itemfoto = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(2), dp(10), dp(2), dp(10))
        }
        if (foto != null && foto.isNotEmpty()) {
            itemfoto.addView(
                ImageView(this).apply {
                    contentDescription = "Foto de $nomeProduto"
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageBitmap(BitmapFactory.decodeByteArray(foto, 0, foto.size))
                    background = forma(
                        0xFFF2F6F4.toInt(),
                        0xFFF2F6F4.toInt(),
                        7
                    )
                    clipToOutline = true
                },
                LinearLayout.LayoutParams(dp(50), dp(50))
            )
        } else {
            // Mostra um espaço reservado quando o produto não tem foto.
            itemfoto.addView(
                TextView(this).apply {
                    text = "⬡"
                    textSize = 17f
                    gravity = Gravity.CENTER
                    setTextColor(verde)
                    background = forma(
                        0xFFF2F6F4.toInt(),
                        0xFFF2F6F4.toInt(),
                        7
                    )
                },
                LinearLayout.LayoutParams(dp(50), dp(50))
            )
        }
        cartaoProduto.addView(TextView(this).apply {
            text = nomeProduto
            textSize = 15f
            setTextColor(0xFF243C39.toInt())
            setTypeface(null, Typeface.BOLD)
        })
        cartaoProduto.addView(TextView(this).apply {
            text =
                "${produtoEncontrado.categoria} • ${produtoEncontrado.conteudo_embalagem} ${produtoEncontrado.unidade} por embalagem"
            textSize = 11f
            setTextColor(cinza)
            setPadding(0, dp(4), 0, 0)
        })
        itemfoto.addView(cartaoProduto)
        conteudo.addView(itemfoto)

        val cartaoFormulario = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(14))
            background = forma(0xFFFFFFFF.toInt(), linha, 12)
        }

        cartaoFormulario.addView(rotulo("Quantidade de entrada"))
        var quantidade = 1
        val quantidadeTexto = TextView(this).apply {
            text = "$quantidade un."
            gravity = Gravity.CENTER
            textSize = 14f
            setTextColor(0xFF243C39.toInt())
        }
        var atualizarPrevisao: () -> Unit = {}
        val controleQuantidade = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(6), dp(8), dp(6))
            background = forma(0xFFFFFFFF.toInt(), linha, 8)
        }

        fun atualizarQuantidade(nova: Int) {
            quantidade = nova.coerceAtLeast(1)
            quantidadeTexto.text = "$quantidade un."
            atualizarPrevisao()
        }
        controleQuantidade.addView(TextView(this).apply {
            text = "−"; textSize = 20f; gravity = Gravity.CENTER; setTextColor(verde)
            setOnClickListener { atualizarQuantidade(quantidade - 1) }
        }, LinearLayout.LayoutParams(dp(32), dp(32)))
        controleQuantidade.addView(quantidadeTexto, LinearLayout.LayoutParams(dp(76), -2))
        controleQuantidade.addView(TextView(this).apply {
            text = "+"; textSize = 20f; gravity = Gravity.CENTER; setTextColor(verde)
            setOnClickListener { atualizarQuantidade(quantidade + 1) }
        }, LinearLayout.LayoutParams(dp(32), dp(32)))
        cartaoFormulario.addView(controleQuantidade)

        val preco = campo(
            "0,00",
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
        )
        val validade = campo("DD/MM/AAAA").apply { isFocusable = false }
        val local = campo("Despensa").apply { setText("Despensa") }
        val linhaCampos = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val colunaPreco = colunaCampo("Preço pago por unidade (R$)", preco)
        val colunaValidade = colunaCampo("Data de vencimento", validade)

        linhaCampos.addView(colunaPreco, LinearLayout.LayoutParams(0, -2, 1f).apply { marginEnd = dp(8) })
        linhaCampos.addView(colunaValidade, LinearLayout.LayoutParams(0, -2, 1f))

        cartaoFormulario.addView(linhaCampos)
        cartaoFormulario.addView(colunaCampo("Local onde será guardado", local))

        validade.setOnClickListener {
            val calendario = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, ano, mes, dia ->
                    validade.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", dia, mes + 1, ano))
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        conteudo.addView(cartaoFormulario, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })

        val estoqueAtual = bancoDados.totalEstoqueProduto(id)
        val previsao = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = forma(verdeEscuro, verdeEscuro, 12)
        }
        previsao.addView(TextView(this).apply {
            text = "Como ficará o estoque"
            textSize = 11f
            setTextColor(0xFFFFFFFF.toInt())
            setTypeface(null, Typeface.BOLD)
        })
        val valoresPrevisao = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val estoqueAntes = TextView(this).apply {
            text = "$estoqueAtual un."; textSize = 20f; setTextColor(0xFFFFFFFF.toInt()); setTypeface(
            null,
            Typeface.BOLD
        )
        }
        val estoqueDepois = TextView(this).apply {
            text = "${estoqueAtual + quantidade} un."; gravity = Gravity.END; textSize =
            20f; setTextColor(0xFFFFFFFF.toInt()); setTypeface(null, Typeface.BOLD)
        }
        valoresPrevisao.addView(estoqueAntes, LinearLayout.LayoutParams(0, -2, 1f))
        valoresPrevisao.addView(TextView(this).apply { text = "→"; textSize = 18f; setTextColor(0xFFB7D5CE.toInt()) })
        valoresPrevisao.addView(estoqueDepois, LinearLayout.LayoutParams(0, -2, 1f))

        atualizarPrevisao = { estoqueDepois.text = "${estoqueAtual + quantidade} un." }

        conteudo.addView(previsao, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })

        val registrar = Button(this).apply {
            text = "REGISTRAR ENTRADA"
            setTextColor(0xFFFFFFFF.toInt())
            setTypeface(null, Typeface.BOLD)
            background = forma(verde, verde, 8)
            setOnClickListener {
                val precoInformado = preco.text.toString().trim().replace(',', '.').toDoubleOrNull()
                val localInformado = local.text.toString().trim()
                val validadeExibida = validade.text.toString().trim()
                val validadeBanco = if (validadeExibida.isBlank()) "" else try {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { isLenient = false }
                        .parse(validadeExibida)?.let { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(it) }.orEmpty()
                } catch (_: Exception) {
                    ""
                }
                if (precoInformado == null || precoInformado < 0 || localInformado.isBlank()) {
                    Toast.makeText(
                        this@MovProdutosActivity,
                        "Informe um preço válido e o local de armazenamento.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (validadeExibida.isNotBlank() && validadeBanco.isBlank()) {
                    Toast.makeText(
                        this@MovProdutosActivity,
                        "Informe uma data de vencimento válida.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                val sucesso =
                    bancoDados.addMovementacaoEntrada(id, localInformado, quantidade, precoInformado, validadeBanco)
                if (sucesso) {
                    Toast.makeText(this@MovProdutosActivity, "Entrada registrada com sucesso.", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                } else Toast.makeText(
                    this@MovProdutosActivity,
                    "Não foi possível registrar a entrada.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        raiz.addView(registrar, LinearLayout.LayoutParams(-1, dp(48)).apply {
            setMargins(dp(16), dp(8), dp(16), dp(50))
        })
        setContentView(raiz)
    }

    private fun rotulo(texto: String) = TextView(this).apply {
        text = texto; textSize = 11f; setTextColor(cinza); setPadding(0, dp(12), 0, dp(5))
    }

    private fun colunaCampo(titulo: String, campo: EditText) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        addView(rotulo(titulo))
        addView(campo, LinearLayout.LayoutParams(-1, dp(42)))
    }

    private fun montarTelaRetirada(id: Long, nome: String) {
        val raiz = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(fundo)
        }
        val cabecalho = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(50), dp(16), dp(20))
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        cabecalho.addView(TextView(this).apply {
            text = "Retirada de produtos"
            textSize = 22f
            setTextColor(0xFF1D3936.toInt())
            setTypeface(null, Typeface.BOLD)
        })
        cabecalho.addView(TextView(this).apply {
            text = "Informe os dados da retirada do produto."
            textSize = 12f
            setTextColor(cinza)
            setPadding(0, dp(4), 0, 0)
        })
        raiz.addView(cabecalho)

        val produtoEncontrado = bancoDados.allProducts().firstOrNull { it.id == id }

        if (produtoEncontrado == null) {
            Toast.makeText(this, "Produto não encontrado", Toast.LENGTH_SHORT).show()
            setContentView(raiz)
            return
        }
        val scroll = ScrollView(this)
        val conteudo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(18))
        }
        scroll.addView(conteudo)
        raiz.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val nomeProduto = produtoEncontrado.nome.ifBlank { nome }
        conteudo.addView(TextView(this).apply {
            text = "Produto selecionado"
            textSize = 14f
            setTextColor(0xFF243C39.toInt())
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(8))
        })
        val cartaoProduto = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = forma(0xFFFFFFFF.toInt(), linha, 12)
        }
        val foto = produtoEncontrado.foto
        val itemfoto = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(2), dp(10), dp(2), dp(10))
        }
        if (foto != null && foto.isNotEmpty()) {
            itemfoto.addView(
                ImageView(this).apply {
                    contentDescription = "Foto de $nomeProduto"
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageBitmap(BitmapFactory.decodeByteArray(foto, 0, foto.size))
                    background = forma(
                        0xFFF2F6F4.toInt(),
                        0xFFF2F6F4.toInt(),
                        7
                    )
                    clipToOutline = true
                },
                LinearLayout.LayoutParams(dp(50), dp(50))
            )
        } else {
            // Mostra um espaço reservado quando o produto não tem foto.
            itemfoto.addView(
                TextView(this).apply {
                    text = "⬡"
                    textSize = 17f
                    gravity = Gravity.CENTER
                    setTextColor(verde)
                    background = forma(
                        0xFFF2F6F4.toInt(),
                        0xFFF2F6F4.toInt(),
                        7
                    )
                },
                LinearLayout.LayoutParams(dp(50), dp(50))
            )
        }
        cartaoProduto.addView(TextView(this).apply {
            text = nomeProduto
            textSize = 15f
            setTextColor(0xFF243C39.toInt())
            setTypeface(null, Typeface.BOLD)
        })
        cartaoProduto.addView(TextView(this).apply {
            text =
                "${produtoEncontrado.categoria} • ${produtoEncontrado.conteudo_embalagem} ${produtoEncontrado.unidade} por embalagem"
            textSize = 11f
            setTextColor(cinza)
            setPadding(0, dp(4), 0, 0)
        })
        itemfoto.addView(cartaoProduto)
        conteudo.addView(itemfoto)

        val cartaoFormulario = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(14))
            background = forma(0xFFFFFFFF.toInt(), linha, 12)
        }

        cartaoFormulario.addView(rotulo("Quantidade de retirada"))
        var quantidade = 1
        val quantidadeTexto = TextView(this).apply {
            text = "$quantidade un."
            gravity = Gravity.CENTER
            textSize = 14f
            setTextColor(0xFF243C39.toInt())
        }
        var atualizarPrevisao1: () -> Unit = {}
        val controleQuantidade = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(6), dp(8), dp(6))
            background = forma(0xFFFFFFFF.toInt(), linha, 8)
        }

        fun atualizarQuantidade(nova: Int) {
            quantidade = nova.coerceAtLeast(1)
            quantidadeTexto.text = "$quantidade un."
            atualizarPrevisao1()
        }
        controleQuantidade.addView(TextView(this).apply {
            text = "−"; textSize = 20f; gravity = Gravity.CENTER; setTextColor(verde)
            setOnClickListener { atualizarQuantidade(quantidade - 1) }
        }, LinearLayout.LayoutParams(dp(32), dp(32)))
        controleQuantidade.addView(quantidadeTexto, LinearLayout.LayoutParams(dp(76), -2))
        controleQuantidade.addView(TextView(this).apply {
            text = "+"; textSize = 20f; gravity = Gravity.CENTER; setTextColor(verde)
            setOnClickListener { atualizarQuantidade(quantidade + 1) }
        }, LinearLayout.LayoutParams(dp(32), dp(32)))
        cartaoFormulario.addView(controleQuantidade)


        // spinner para selecionar o local do produto
        val locaisproduto = bancoDados.listarLocaisPorProduto(produtoEncontrado.id)

        val locaisprodutocoluna = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), 0, 0, 0)
        }
        locaisprodutocoluna.addView(rotulo("Locais do produto"))

        val locaisspi = Spinner(this).apply {
            adapter = android.widget.ArrayAdapter(
                this@MovProdutosActivity,
                android.R.layout.simple_spinner_dropdown_item,
                locaisproduto
            )
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        locaisprodutocoluna.addView(locaisspi, campoParams())
        cartaoFormulario.addView(locaisprodutocoluna)

        val validadeColuna = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), 0, 0, 0)
        }
        validadeColuna.addView(rotulo("Datas de validade do produto neste local"))

        val vencimento = Spinner(this).apply {
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        validadeColuna.addView(vencimento, campoParams())
        cartaoFormulario.addView(validadeColuna)

        val previsao = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = forma(verdeEscuro, verdeEscuro, 12)
        }

        val tituloPrevisao = TextView(this).apply {
            textSize = 11f
            setTextColor(0xFFFFFFFF.toInt())
            setTypeface(null, Typeface.BOLD)
        }
        previsao.addView(tituloPrevisao)

        val valoresPrevisao = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val estoqueAntes = TextView(this).apply {
            textSize = 20f
            setTextColor(0xFFFFFFFF.toInt())
            setTypeface(null, Typeface.BOLD)
        }
        val setaPrevisao = TextView(this).apply {
            text = "→"
            textSize = 18f
            setTextColor(0xFFB7D5CE.toInt())
        }
        val estoqueDepois = TextView(this).apply {
            gravity = Gravity.END
            textSize = 20f
            setTextColor(0xFFFFFFFF.toInt())
            setTypeface(null, Typeface.BOLD)
        }

        val pesoPrevisao = LinearLayout.LayoutParams(0, -2, 1f)
        val pesoEstoqueDepois = LinearLayout.LayoutParams(0, -2, 1f)
        val margemSeta = LinearLayout.LayoutParams(-2, -2).apply {
            setMargins(dp(12), 0, dp(12), 0)
        }


        val linhaValores = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(valoresPrevisao)
        }
        previsao.addView(linhaValores)

        val atualizarPrevisao = {
            val localSelecionado = locaisspi.selectedItem?.toString().orEmpty()
            val validadeExibida = vencimento.selectedItem?.toString().orEmpty()

            val validadeBanco = runCatching {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .apply { isLenient = false }
                    .parse(validadeExibida)
                    ?.let { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(it) }
                    .orEmpty()
            }.getOrDefault("")

            val estoqueAtual = if (localSelecionado.isNotBlank() && validadeBanco.isNotBlank()) {
                bancoDados.listarQuantidadePorProdutoLocaleValidade(
                    produtoEncontrado.id,
                    localSelecionado,
                    validadeBanco
                )
            } else {
                0
            }

            tituloPrevisao.text = if (localSelecionado.isBlank()) {
                "Como ficará o estoque"
            } else {
                "Como ficará o estoque em $localSelecionado"
            }

            estoqueAntes.text = "$estoqueAtual un."
            estoqueDepois.text = "${(estoqueAtual - quantidade).coerceAtLeast(0)} un."
        }

        val atualizarQuantidade = { nova: Int ->
            quantidade = nova.coerceAtLeast(1)
            quantidadeTexto.text = "$quantidade un."
            atualizarPrevisao()
        }

        controleQuantidade.getChildAt(0).setOnClickListener {
            atualizarQuantidade(quantidade - 1)
        }
        controleQuantidade.getChildAt(2).setOnClickListener {
            atualizarQuantidade(quantidade + 1)
        }

        valoresPrevisao.addView(estoqueAntes, pesoPrevisao)
        valoresPrevisao.addView(setaPrevisao, margemSeta)
        valoresPrevisao.addView(estoqueDepois, pesoEstoqueDepois)

        fun atualizarVencimentos(local: String) {
            val validades = bancoDados.listarDataValidadePorProdutoLocal(
                produtoEncontrado.id,
                local
            )

            val datasFormatadas = validades.mapNotNull { data ->
                runCatching {
                    SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(data)?.let {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                    }
                }.getOrNull()
            }

            vencimento.adapter = android.widget.ArrayAdapter(
                this@MovProdutosActivity,
                android.R.layout.simple_spinner_dropdown_item,
                datasFormatadas
            )

            atualizarPrevisao()
        }

        vencimento.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    atualizarPrevisao()
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                    atualizarPrevisao()
                }
            }

        locaisspi.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val localSelecionado = locaisproduto.getOrNull(position).orEmpty()
                    atualizarVencimentos(localSelecionado)
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                    vencimento.adapter = null
                    atualizarPrevisao()
                }
            }

        // Exibe primeiro o formulário e depois o resumo da previsão.
        conteudo.addView(cartaoFormulario, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })

        conteudo.addView(
            previsao,
            LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) }
        )
        // val listarLocaisPorProduto = bancoDados.listarLocaisPorProduto(produtoEncontrado.id)
        // Toast.makeText(
        //     this@MovProdutosActivity,
        //     "$listarLocaisPorProduto",
        //     3000
        // ).show()
        val registrar = Button(this).apply {
            text = "REGISTRAR RETIRADA"
            setTextColor(0xFFFFFFFF.toInt())
            setTypeface(null, Typeface.BOLD)
            background = forma(verde, verde, 8)
            setOnClickListener {
                val vencimentoInput = vencimento.selectedItem?.toString().orEmpty()
                val localInput = locaisspi.selectedItem?.toString().orEmpty()
                val localInformado = localInput.trim()
                val validadeExibida = vencimentoInput.trim()
                if (localInformado.isBlank() || validadeExibida.isBlank()) {
                    Toast.makeText(
                        this@MovProdutosActivity,
                        "Selecione um local e uma data de validade.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                val validadeBanco = runCatching {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                        isLenient = false
                    }.parse(validadeExibida)?.let {
                        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(it)
                    }.orEmpty()
                }.getOrDefault("")
                if (validadeBanco.isBlank()) {
                    Toast.makeText(
                        this@MovProdutosActivity,
                        "A data de validade selecionada é inválida.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                val quantidadebanco = bancoDados.listarQuantidadePorProdutoLocaleValidade(
                    produtoEncontrado.id,
                    localInformado,
                    validadeBanco
                )
                if (quantidade <= 0) {
                    Toast.makeText(
                        this@MovProdutosActivity,
                        "Informe uma quantidade válida.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (quantidade > quantidadebanco) {
                    Toast.makeText(
                        this@MovProdutosActivity,
                        "Quantidade solicitada excede o estoque disponível.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (localInformado.isBlank()) {
                    Toast.makeText(
                        this@MovProdutosActivity,
                        "Informe o local de armazenamento valido.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (validadeExibida.isNotBlank() && validadeBanco.isBlank()) {
                    Toast.makeText(
                        this@MovProdutosActivity,
                        "Informe uma data de vencimento válida.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                val sucesso = bancoDados.addMovementacaoRetirada(
                    id,
                    localInformado,
                    quantidade,
                    validadeBanco
                )
                if (sucesso) {
                    Toast.makeText(
                        this@MovProdutosActivity,
                        "Retirada registrada com sucesso.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@MovProdutosActivity,
                        "Não foi possível registrar a retirada.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        raiz.addView(registrar, LinearLayout.LayoutParams(-1, dp(48)).apply {
            setMargins(dp(16), dp(8), dp(16), dp(50))
        })
        setContentView(raiz)
    }

    private fun forma(cor: Int, borda: Int, raioDp: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(cor)
            cornerRadius = dp(raioDp).toFloat()
            setStroke(dp(1), borda)
        }

    private fun dp(valor: Int): Int =
        (valor * resources.displayMetrics.density).toInt()


    private fun campo(
        dica: String,
        tipo: Int = android.text.InputType.TYPE_CLASS_TEXT
    ) = EditText(this).apply {
        hint = dica
        inputType = tipo
        textSize = 14f
        setPadding(dp(10), 0, dp(10), 0)
        background = fundoArredondado(0xFFFFFFFF.toInt(), dp(8))
    }

    private fun fundoArredondado(cor: Int, raio: Int) =
        android.graphics.drawable.GradientDrawable().apply {
            setColor(cor)
            cornerRadius = raio.toFloat()
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

    private fun campoParams() =
        LinearLayout.LayoutParams(-1, dp(44)).apply {
            bottomMargin = dp(8)
        }

}
