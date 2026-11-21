package br.com.dispensa

import android.app.Activity
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import android.widget.TextView

class ListaCompraProdutosActivity : Activity() {
    private val verdeEscuro = 0xFF10564F.toInt()
    private val verde = 0xFF11665D.toInt()
    private val fundo = 0xFFF4F7F6.toInt()
    private val cinza = 0xFF71817D.toInt()
    private val linha = 0xFFE7ECEA.toInt()
    private lateinit var bancoDados: ProdutoDatabase

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
        val produtosFaltando = bancoDados.listarProdutosEmFalta()
        val cabecalho = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(50), dp(16), dp(14))
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        cabecalho.addView(TextView(this).apply {
            text = "Lista de compras"
            textSize = 21f
            setTextColor(0xFF1D3936.toInt())
            setTypeface(null, Typeface.BOLD)
        })
        cabecalho.addView(TextView(this).apply {
            text = "Tudo o que precisa repor, separado por categoria."
            textSize = 11f
            setTextColor(cinza)
            setPadding(0, dp(4), 0, 0)
        })
        raiz.addView(cabecalho)

        val rolagem = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
        }
        val conteudo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(16))
        }
        rolagem.addView(conteudo)
        raiz.addView(rolagem, LinearLayout.LayoutParams(-1, 0, 1f))

        val resumo = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = forma(verdeEscuro, verdeEscuro, 12)
        }
        val quantidadeProdutosFaltando = produtosFaltando.size
        resumo.addView(TextView(this).apply {
            text = "$quantidadeProdutosFaltando produtos"
            textSize = 17f
            setTextColor(0xFFFFFFFF.toInt())
            setTypeface(null, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, -2, 1f))
        conteudo.addView(resumo, LinearLayout.LayoutParams(-1, dp(50)).apply {
            bottomMargin = dp(10)
        }

        )

        fun adicionarCategoria(
            nome: String,
            quantidade: String,
            itens: List<ProdutoEmFalta>
        ) {
            val cabecalhoCategoria = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(3), 0, dp(5))
            }
            cabecalhoCategoria.addView(TextView(this).apply {
                text = nome
                textSize = 10f
                setTextColor(0xFF29413E.toInt())
                setTypeface(null, Typeface.BOLD)
            }, LinearLayout.LayoutParams(0, -2, 1f))
            cabecalhoCategoria.addView(TextView(this).apply {
                text = quantidade
                textSize = 9f
                setTextColor(cinza)
            })
            conteudo.addView(cabecalhoCategoria)

            val grupo = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(8), dp(2), dp(8), dp(2))
                background = forma(0xFFFFFFFF.toInt(), linha, 10)
            }
            itens.forEachIndexed { index, item ->
                val linhaItem = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(dp(2), dp(7), dp(2), dp(7))
                }
                val foto = item.foto
                if (foto != null && foto.isNotEmpty()) {
                    linhaItem.addView(
                        ImageView(this).apply {
                            contentDescription = "Foto de ${item.nome}"
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            setImageBitmap(BitmapFactory.decodeByteArray(foto, 0, foto.size))
                            background = forma(0xFFF2F6F4.toInt(), 0xFFF2F6F4.toInt(), 7)
                            clipToOutline = true
                        },
                        LinearLayout.LayoutParams(dp(34), dp(38))
                    )
                } else {
                    linhaItem.addView(
                        TextView(this).apply {
                            text = "⬡"
                            textSize = 17f
                            gravity = Gravity.CENTER
                            setTextColor(verde)
                            background = forma(0xFFF2F6F4.toInt(), 0xFFF2F6F4.toInt(), 7)
                        },
                        LinearLayout.LayoutParams(dp(34), dp(38))
                    )
                }

                val textos = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(8), 0, dp(4), 0)
                }
                textos.addView(TextView(this).apply {
                    text = item.nome
                    textSize = 9f
                    setTextColor(0xFF29413E.toInt())
                    setTypeface(null, Typeface.BOLD)
                })
                textos.addView(TextView(this).apply {
                    text = "Atual: ${item.estoqueAtual} • Mínimo: ${item.estoqueMinimo}"
                    textSize = 8f
                    setTextColor(cinza)
                    setPadding(0, dp(2), 0, 0)
                })
                linhaItem.addView(textos, LinearLayout.LayoutParams(0, -2, 1f))
                linhaItem.addView(TextView(this).apply {
                    text = "${item.quantidadeFaltando} UND"
                    textSize = 9f
                    setTextColor(verde)
                    setTypeface(null, Typeface.BOLD)
                    gravity = Gravity.END or Gravity.CENTER_VERTICAL
                })
                grupo.addView(linhaItem)
                if (index < itens.lastIndex) {
                    grupo.addView(
                        View(this).apply { setBackgroundColor(linha) },
                        LinearLayout.LayoutParams(-1, dp(1)).apply {
                            leftMargin = dp(28)
                            rightMargin = dp(2)
                        })
                }
            }
            conteudo.addView(grupo, LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = dp(8)
            })
        }

        if (produtosFaltando.isEmpty()) {
            conteudo.addView(TextView(this).apply {
                text = "Nenhum produto precisa de reposição."
                textSize = 12f
                setTextColor(cinza)
                gravity = Gravity.CENTER
                setPadding(dp(12), dp(28), dp(12), dp(28))
            })
        } else {
            produtosFaltando
                .groupBy { it.categoria.ifBlank { "Outros" } }
                .forEach { (categoria, produtosDaCategoria) ->
                    val quantidade = produtosDaCategoria.size
                    adicionarCategoria(
                        categoria,
                        "$quantidade ${if (quantidade == 1) "item" else "itens"}",
                        produtosDaCategoria
                    )
                }
        }
        setContentView(raiz)
    }

    private fun forma(cor: Int, borda: Int, raioDp: Int) =
        GradientDrawable().apply {
            setColor(cor)
            cornerRadius = dp(raioDp).toFloat()
            setStroke(dp(1), borda)
        }

    private fun dp(valor: Int): Int =
        (valor * resources.displayMetrics.density).toInt()
}



