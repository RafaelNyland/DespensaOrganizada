package br.com.dispensa

import android.app.Activity
import android.app.AlertDialog
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
import android.content.Intent


class MeusProdutosActivity : Activity() {
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
        montarTela()
    }

    private fun montarTela() {
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
            text = "Meus produtos"
            textSize = 22f
            setTextColor(0xFF1D3936.toInt())
            setTypeface(null, Typeface.BOLD)
        })
        cabecalho.addView(TextView(this).apply {
            text = "Veja o que há em casa e encontre qualquer item."
            textSize = 12f
            setTextColor(cinza)
            setPadding(0, dp(4), 0, 0)
        })
        raiz.addView(cabecalho)

        val rolagem = ScrollView(this).apply { isFillViewport = true }
        val conteudo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(14), dp(12), dp(12))
        }
        rolagem.addView(conteudo)

        buscaInput = EditText(this).apply {
            hint = "Buscar por nome"
            textSize = 13f
            setPadding(dp(12), 0, dp(12), 0)
            background = forma(0xFFFFFFFF.toInt(), 0xFFE2E9E6.toInt(), 10)
        }
        conteudo.addView(buscaInput, LinearLayout.LayoutParams(-1, dp(42)).apply {
            bottomMargin = dp(12)
        })
        val codigoTitulo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(8), 0, dp(6))
        }
        codigoTitulo.addView(TextView(this).apply {
            text = "▥   Escanear código de barras"
            textSize = 14f
            setTextColor(verde)
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        codigoTitulo.addView(TextView(this).apply {
            text = "Ou preencha os dados abaixo"
            textSize = 12f
            setTextColor(cinza)
        })
        conteudo.addView(codigoTitulo)

        val codigoLinha = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        codigobarraInput = campo("Leia ou digite o código")
        codigoLinha.addView(codigobarraInput, LinearLayout.LayoutParams(0, dp(35), 1f))

        codigoLinha.addView(Button(this).apply {
            text = "LER CÓDIGO"
            textSize = 11f
            setTextColor(0xFFFFFFFF.toInt())
            background = fundoArredondado(verde, dp(10))
            setOnClickListener { lerCodigoBarras() }
        }, LinearLayout.LayoutParams(dp(100), dp(35)).apply {
            leftMargin = dp(8)
        })
        conteudo.addView(codigoLinha)

        val filtros = listOf(
            "Todos",
            "Alimentos", "Frios", "Frutas", "Legumes",
            "Congelados", "Limpeza", "Higiene", "Medicamentos"
        )
        val filtrosLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        var linhaAtual = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        filtrosLayout.addView(linhaAtual)
        val todosChips = mutableListOf<TextView>()
        filtros.forEachIndexed { indice, filtro ->
            if (indice == 5) {
                linhaAtual = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                }
                filtrosLayout.addView(linhaAtual)
            }

            val chip = TextView(this).apply {
                text = filtro
                textSize = 11f
                gravity = Gravity.CENTER
                setPadding(dp(18), 0, dp(18), 0)
                setTextColor(if (indice == 0) 0xFFFFFFFF.toInt() else cinza)
                background = forma(
                    if (indice == 0) verde else 0xFFFFFFFF.toInt(),
                    if (indice == 0) verde else 0xFFE2E9E6.toInt(),
                    20
                )
            }
            chip.setOnClickListener {
                categoriaSelecionada = filtro.takeUnless { it == "Todos" }
                atualizarLista()
                // Atualiza a aparência de todos os chips.
                todosChips.forEachIndexed { i, chipAtual ->
                    val selecionado =
                        (i == 0 && categoriaSelecionada == null) ||
                                filtros[i] == categoriaSelecionada

                    chipAtual.setTextColor(
                        if (selecionado) 0xFFFFFFFF.toInt() else cinza
                    )
                    chipAtual.background = forma(
                        if (selecionado) verde else 0xFFFFFFFF.toInt(),
                        if (selecionado) verde else 0xFFE2E9E6.toInt(),
                        20
                    )
                }

            }
            todosChips.add(chip)
            linhaAtual.addView(
                chip,
                LinearLayout.LayoutParams(-2, dp(26)).apply {
                    rightMargin = dp(5)
                    bottomMargin = dp(5)
                    topMargin = dp(5)
                }
            )
        }

        conteudo.addView(filtrosLayout)
        textoQuantidade = TextView(this).apply {
            textSize = 12f
            setTextColor(0xFF1D3936.toInt())
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(10), 0, dp(10))
        }
        conteudo.addView(textoQuantidade)

        cartaoLista = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), 0, dp(8), 0)
            background = forma(0xFFFFFFFF.toInt(), 0xFFEEF1F0.toInt(), 12)
        }
        puxarProdutos("", null)

        conteudo.addView(cartaoLista)
        raiz.addView(rolagem, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(raiz)

        buscaInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
                cartaoLista.removeAllViews()
                puxarProdutos(s?.toString().orEmpty(), categoriaSelecionada)
            }

            override fun afterTextChanged(s: android.text.Editable?) = Unit
        })
    }

    fun atualizarLista() {
        cartaoLista.removeAllViews()
        puxarProdutos(codigobarraInput.text.toString(), categoriaSelecionada)
    }

    private fun forma(cor: Int, borda: Int, raioDp: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(cor)
            cornerRadius = dp(raioDp).toFloat()
            setStroke(dp(1), borda)
        }

    private fun dp(valor: Int): Int =
        (valor * resources.displayMetrics.density).toInt()

    fun puxarProdutos(nomeproduto: String, categoria: String?) {
        val produtos = bancoDados.procurarProdutos(nomeproduto, categoria)
        textoQuantidade.text = "${produtos.size} produtos encontrados"
        produtos.forEachIndexed { index, produto ->
            val nome = produto.nome
            val detalhe =
                "${produto.conteudo_embalagem} ${produto.unidade} • ${produto.categoria}"
            val foto = produto.foto
            val itemfoto = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(2), dp(10), dp(2), dp(10))
            }
            if (foto != null && foto.isNotEmpty()) {
                itemfoto.addView(
                    ImageView(this).apply {
                        contentDescription = "Foto de ${produto.nome}"
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        setImageBitmap(BitmapFactory.decodeByteArray(foto, 0, foto.size))
                        background = forma(
                            0xFFF2F6F4.toInt(),
                            0xFFF2F6F4.toInt(),
                            7
                        )
                        clipToOutline = true
                    },
                    LinearLayout.LayoutParams(dp(34), dp(38))
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
                    LinearLayout.LayoutParams(dp(34), dp(38))
                )
            }
            val descricao = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(8), 0, dp(4), 0)
            }
            descricao.addView(TextView(this).apply {
                text = nome
                textSize = 11f
                setTextColor(0xFF243C39.toInt())
                setTypeface(null, Typeface.BOLD)
                maxLines = 1
            })
            descricao.addView(TextView(this).apply {
                text = detalhe
                textSize = 9f
                setTextColor(cinza)
                maxLines = 1
                setPadding(0, dp(3), 0, 0)
            })
            itemfoto.addView(descricao, LinearLayout.LayoutParams(0, -2, 1f))
            itemfoto.addView(TextView(this).apply {
                text = "EDITAR"
                textSize = 9f
                gravity = Gravity.CENTER
                setTextColor(0xFFFFFFFF.toInt())
                setTypeface(null, Typeface.BOLD)
                background = forma(verde, verde, 6)
                setOnClickListener {
                    val telaEditar = Intent(
                        this@MeusProdutosActivity,
                        EditarProdutosActivity::class.java
                    )
                    telaEditar.putExtra("produto_id", produto.id)
                    telaEditar.putExtra("produto_nome", nome)
                    this@MeusProdutosActivity.startActivity(telaEditar)
                }
            }, LinearLayout.LayoutParams(dp(54), dp(30)))
            cartaoLista.addView(itemfoto)
            if (index < produtos.lastIndex) {
                cartaoLista.addView(
                    View(this).apply { setBackgroundColor(linha) },
                    LinearLayout.LayoutParams(-1, dp(1))
                )
            }
        }
        // Toast.makeText(
        //     this,
        //     "Quantidade: ${quantidadeprodutos}",
        //     Toast.LENGTH_SHORT
        // ).show()
    }

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

    // função para ler o código de barras
    private fun lerCodigoBarras() {
        val opcoesLeitor = GmsBarcodeScannerOptions.Builder()
            .enableAutoZoom()
            .build()

        GmsBarcodeScanning.getClient(this, opcoesLeitor).startScan()
            .addOnSuccessListener { codigoLido ->
                val codigoBarras = codigoLido.rawValue.orEmpty().trim()
                codigobarraInput.setText(codigoBarras)
                if (codigoBarras.isNotEmpty() && !bancoDados.barcodeExists(codigoBarras)) {
                    AlertDialog.Builder(this)
                        .setTitle("Produto não cadastrado")
                        .setMessage("Este código de barras não esta cadastrado, primeiro registre.")
                        .setPositiveButton("OK", null)
                        .show()
                    codigobarraInput.requestFocus()
                    codigobarraInput.setText(null)

                }
                atualizarLista()
            }
            .addOnCanceledListener { }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Não foi possível abrir o leitor. Tente novamente.",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
