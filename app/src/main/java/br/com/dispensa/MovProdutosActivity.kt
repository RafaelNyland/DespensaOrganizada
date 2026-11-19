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
                montarTelaRetirada(id, nome)

            }
        } else {
            montarTelaEmpty(id, nome)
        }
    }

    private fun montarTelaEmpty(id: Long, nome: String) {
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
            text = "Produto não encontrado"
            textSize = 22f
            setTextColor(0xFF1D3936.toInt())
            setTypeface(null, Typeface.BOLD)
        })
        cabecalho.addView(TextView(this).apply {
            text = "Aconteceu um erro ao localizar o produto. Por favor, tente novamente."
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
                    contentDescription = "Foto de ${nomeProduto}"
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
        previsao.addView(valoresPrevisao)

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
                    contentDescription = "Foto de ${nomeProduto}"
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
        setSingleLine(true)
        textSize = 14f
        setPadding(dp(10), 0, dp(10), 0)
        background = fundoArredondado(0xFFFFFFFF.toInt(), dp(8))
    }

    private fun fundoArredondado(cor: Int, raio: Int) =
        android.graphics.drawable.GradientDrawable().apply {
            setColor(cor)
            cornerRadius = raio.toFloat()
        }

}
