package br.com.dispensa

import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.Toast

private lateinit var bancoDados: ProdutoDatabase

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bancoDados = ProdutoDatabase(this)
    }

    override fun onResume() {
        super.onResume()
        montarPainel()
    }

    private fun montarPainel() {
        val fundo = 0xFFF5F7F6.toInt()
        val verdeEscuro = 0xFF10564F.toInt()
        val verdeClaro = 0xFFE7F2EF.toInt()
        val textoSecundario = 0xFF62716E.toInt()

        val pagina = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(fundo)
            setPadding(dp(16), dp(50), dp(16), dp(20))
        }
        val conteudo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(fundo)
            setPadding(dp(16), dp(50), dp(16), dp(20))
        }

        // Título e botão de notificações
        val cabecalho = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        cabecalho.addView(TextView(this).apply {
            text = "Despensa Organizada"
            textSize = 21f
            setTextColor(verdeEscuro)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, dp(48), 1f))

        pagina.addView(cabecalho)

        pagina.addView(TextView(this).apply {
            text = "O que vamos fazer hoje?"
            textSize = 13f
            setTextColor(textoSecundario)
            setPadding(0, dp(8), 0, dp(15))
        })

        // Botão para cadastrar produtos
        pagina.addView(botao("▥ LER PRODUTO", verdeEscuro) {
            val intent = Intent(this, TesteCadastroProdutoActivity::class.java)
            startActivity(intent)
        }, LinearLayout.LayoutParams(-1, dp(44)))
        // Atalhos
        val atalhos = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val opcoes = listOf(
            "＋\nCADASTRAR\nPRODUTOS",

            "↑  ↓\nENTRADAS\nE RETIRADAS",
            "⬡\nMEUS\nPRODUTOS",
            "•••\nOUTROS"
        )

        opcoes.forEach { titulo ->
            atalhos.addView(TextView(this).apply {
                text = titulo
                textSize = 11f
                gravity = Gravity.CENTER
                setTextColor(verdeEscuro)
                background = formato(verdeClaro, dp(12))
                setPadding(dp(4), dp(6), dp(4), dp(6))
                if (titulo.contains("CADASTRAR")) {
                    setOnClickListener {
                        startActivity(Intent(this@MainActivity, CadastroProdutoActivity::class.java))
                    }
                }
                if (titulo.contains("ENTRADAS")) {
                    setOnClickListener {
                        startActivity(Intent(this@MainActivity, RegRetProdutosActivity::class.java))
                    }
                }
                if (titulo.contains("MEUS")) {
                    setOnClickListener {
                        startActivity(Intent(this@MainActivity, MeusProdutosActivity::class.java))
                    }
                }
                if (titulo.contains("OUTROS")) {
                    setOnClickListener {
                        startActivity(Intent(this@MainActivity, OutrosActivity::class.java))
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(72), 1f).apply {
                setMargins(dp(3), dp(10), dp(3), 10)
            })
        }
        pagina.addView(atalhos)
        val rolagemConteudo = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            addView(conteudo)
        }
        pagina.addView(rolagemConteudo, LinearLayout.LayoutParams(-1, 0, 1f))
        // Conteúdo do painel inicial abaixo dos atalhos.
        conteudo.removeAllViews()
        conteudo.gravity = Gravity.TOP
        conteudo.setPadding(0, dp(16), 0, 0)

        val produtos = bancoDados.procurarProdutos("")
        val resumo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = forma(verdeEscuro, verdeEscuro, 12)
        }
        resumo.addView(TextView(this).apply {
            text = "Seu estoque em números"
            textSize = 12f
            setTextColor(0xFFFFFFFF.toInt())
        })
        resumo.addView(TextView(this).apply {
            text = produtos.size.toString()
            textSize = 23f
            setTextColor(0xFFFFFFFF.toInt())
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(3), 0, 0)
        })
        resumo.addView(TextView(this).apply {
            text = "Produtos cadastrados"
            textSize = 9f
            setTextColor(0xFFB9D6D1.toInt())
        })
        conteudo.addView(resumo, LinearLayout.LayoutParams(dp(190), dp(82)).apply {
            bottomMargin = dp(14)
        })

        val produtosEmFalta = bancoDados.listarProdutosEmFalta()
        conteudo.addView(TextView(this).apply {
            text = "Pontos de atenção"
            textSize = 14f
            setTextColor(verdeEscuro)
            setTypeface(null, Typeface.BOLD)
            setPadding(dp(10), dp(8), dp(10), dp(8))

        })

        val cartaoFaltas = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = forma(0xFFFFFFFF.toInt(), 0xFFE6ECE9.toInt(), 12)
            clipToOutline = true
        }
        val cabecalhoFaltas = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(8), dp(10), dp(8))
            setBackgroundColor(0xFFFFF2E4.toInt())
        }
        cabecalhoFaltas.addView(TextView(this).apply {
            text = "Abaixo do estoque mínimo"
            textSize = 10f
            setTextColor(0xFFB65A23.toInt())
        }, LinearLayout.LayoutParams(0, -2, 1f))
        cabecalhoFaltas.addView(TextView(this).apply {
            text = produtosEmFalta.size.toString()
            textSize = 9f
            setTextColor(0xFFB65A23.toInt())
            gravity = Gravity.CENTER
            background = formato(0xFFFFFFFF.toInt(), dp(20))
            setPadding(dp(6), dp(2), dp(6), dp(2))
        })
        cartaoFaltas.addView(cabecalhoFaltas)

        if (produtosEmFalta.isEmpty()) {
            cartaoFaltas.addView(TextView(this).apply {
                text = "Nenhum produto abaixo do mínimo."
                textSize = 10f
                setTextColor(textoSecundario)
                setPadding(dp(10), dp(12), dp(10), dp(12))
            })
        } else {
            produtosEmFalta.take(5).forEach { produto ->
                val linha = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(dp(10), dp(8), dp(10), dp(8))
                }
                linha.addView(TextView(this).apply {
                    text = "⬡"
                    textSize = 13f
                    gravity = Gravity.CENTER
                    setTextColor(verdeEscuro)
                    background = formato(verdeClaro, dp(8))
                }, LinearLayout.LayoutParams(dp(30), dp(30)))
                val detalhes = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(8), 0, 0, 0)
                }
                detalhes.addView(TextView(this).apply {
                    text = produto.nome
                    textSize = 10f
                    setTextColor(0xFF263B38.toInt())
                    setTypeface(null, Typeface.BOLD)
                })
                detalhes.addView(TextView(this).apply {
                    text = "${produto.conteudo_embalagem} ${produto.unidade}  •  mínimo ${produto.estoqueMinimo}"
                    textSize = 9f
                    setTextColor(textoSecundario)
                })
                linha.addView(detalhes, LinearLayout.LayoutParams(0, -2, 1f))
                linha.addView(TextView(this).apply {
                    text = "Faltam ${produto.quantidadeFaltando}"
                    textSize = 9f
                    setTextColor(0xFFC44838.toInt())
                })
                cartaoFaltas.addView(linha)
                if (produto != produtosEmFalta.take(2).last()) {
                    cartaoFaltas.addView(View(this).apply {
                        setBackgroundColor(0xFFE9EEEC.toInt())
                    }, LinearLayout.LayoutParams(-1, dp(1)).apply {
                        leftMargin = dp(10)
                        rightMargin = dp(10)
                    })
                }
            }
        }
        conteudo.addView(cartaoFaltas, LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = dp(16)
        })

        val historico = bancoDados.listarUltimasMovimentacoes(5)
        val tituloMovimentacoes = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        tituloMovimentacoes.addView(TextView(this).apply {
            text = "Últimas movimentações"
            textSize = 14f
            setTextColor(verdeEscuro)
            setTypeface(null, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, -2, 1f))
        conteudo.addView(tituloMovimentacoes, LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = dp(8)
        })

        val cartaoHistorico = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = forma(0xFFFFFFFF.toInt(), 0xFFE6ECE9.toInt(), 12)
            clipToOutline = true
        }
        if (historico.isEmpty()) {
            cartaoHistorico.addView(TextView(this).apply {
                text = "Suas movimentações recentes aparecerão aqui."
                textSize = 10f
                setTextColor(textoSecundario)
                setPadding(dp(10), dp(12), dp(10), dp(12))
            })
        } else {
            historico.forEachIndexed { indice, item ->
                val entrada = item.tipoMovimentacao.equals("ENTRADA", ignoreCase = true)
                val linha = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(dp(10), dp(8), dp(10), dp(8))
                }
                var imgcard = if (entrada) "↙" else "↗"
                linha.addView(TextView(this).apply {
                    text = imgcard
                    textSize = 13f
                    gravity = Gravity.CENTER
                    setTextColor(verdeEscuro)
                    background = formato(verdeClaro, dp(8))
                }, LinearLayout.LayoutParams(dp(28), dp(28)))
                val detalhes = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(8), 0, 0, 0)
                }
                detalhes.addView(TextView(this).apply {
                    text = item.nomeProduto
                    textSize = 10f
                    setTextColor(0xFF263B38.toInt())
                    setTypeface(null, Typeface.BOLD)
                })
                detalhes.addView(TextView(this).apply {
                    val dataHora = java.text.SimpleDateFormat("dd/MM, HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(item.dataHora))
                    text = "${if (entrada) "Entrada" else "Retirada"} - $dataHora"
                    textSize = 9f
                    setTextColor(textoSecundario)
                })
                linha.addView(detalhes, LinearLayout.LayoutParams(0, -2, 1f))
                linha.addView(TextView(this).apply {
                    text = "${if (entrada) "+" else "−"}${item.quantidade} un."
                    textSize = 9f
                    setTextColor(if (entrada) verdeEscuro else textoSecundario)
                })
                cartaoHistorico.addView(linha)
                if (indice < historico.lastIndex) {
                    cartaoHistorico.addView(View(this).apply {
                        setBackgroundColor(0xFFE9EEEC.toInt())
                    }, LinearLayout.LayoutParams(-1, dp(1)).apply {
                        leftMargin = dp(10)
                        rightMargin = dp(10)
                    })
                }
            }
        }
        conteudo.addView(cartaoHistorico, LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = dp(14)
        })
        setContentView(pagina)
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

    private fun formato(cor: Int, raio: Int): android.graphics.drawable.GradientDrawable =
        android.graphics.drawable.GradientDrawable().apply {
            setColor(cor)
            cornerRadius = raio.toFloat()
        }

    private fun dp(valor: Int): Int =
        (valor * resources.displayMetrics.density).toInt()

    private fun forma(cor: Int, borda: Int, raioDp: Int) =
        GradientDrawable().apply {
            setColor(cor)
            cornerRadius = dp(raioDp).toFloat()
            setStroke(dp(1), borda)
        }

}
