package br.com.dispensa

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class OutrosActivity : Activity() {
    private val fundo = 0xFFF5F7F6.toInt()
    private val verdeEscuro = 0xFF10564F.toInt()
    private val verdeClaro = 0xFFE7F2EF.toInt()
    private val texto = 0xFF29413E.toInt()
    private val secundario = 0xFF71817D.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        montarTela()
    }

    private fun montarTela() {
        val raiz = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(fundo)
        }

        val cabecalho = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(50), dp(20), dp(20))
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        cabecalho.addView(TextView(this).apply {
            text = "Outros recursos"
            textSize = 23f
            setTextColor(verdeEscuro)
            setTypeface(null, Typeface.BOLD)
        })
        cabecalho.addView(TextView(this).apply {
            text = "Acompanhe sua despensa e seus gastos."
            textSize = 13f
            setTextColor(secundario)
            setPadding(0, dp(5), 0, 0)
        })
        raiz.addView(cabecalho)

        val rolagem = ScrollView(this).apply { isFillViewport = true }
        val conteudo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(18), dp(16), dp(20))
        }
        rolagem.addView(conteudo)
        raiz.addView(rolagem, LinearLayout.LayoutParams(-1, 0, 1f))
        val listacompras = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = forma(0xFFFFFFFF.toInt(), 0xFFE7ECEA.toInt(), 14)
            addView(iconeOpcao("▱"), LinearLayout.LayoutParams(dp(48), dp(48)))
            addView(
                textosOpcao("Lista de compras", "Veja o que falta no seu estoque."),
                LinearLayout.LayoutParams(0, -2, 1f)
            )
            addView(setaOpcao())
            setOnClickListener {
                startActivity(Intent(this@OutrosActivity, ListaCompraProdutosActivity::class.java))
            }
        }
        conteudo.addView(listacompras, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })
        val historico = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = forma(0xFFFFFFFF.toInt(), 0xFFE7ECEA.toInt(), 14)
            addView(iconeOpcao("◷"), LinearLayout.LayoutParams(dp(48), dp(48)))
            addView(
                textosOpcao("Histórico", "Consulte entradas e retiradas dos produtos."),
                LinearLayout.LayoutParams(0, -2, 1f)
            )
            addView(setaOpcao())
            setOnClickListener {
                startActivity(Intent(this@OutrosActivity, HistoricoProdutosActivity::class.java))
            }
        }
        conteudo.addView(historico, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })

        val alertaVencimento = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = forma(0xFFFFFFFF.toInt(), 0xFFE7ECEA.toInt(), 14)
            addView(iconeOpcao("◷"), LinearLayout.LayoutParams(dp(48), dp(48)))
            addView(
                textosOpcao("Alerta de vencimento", "Veja os produtos próximos da validade."),
                LinearLayout.LayoutParams(0, -2, 1f)
            )
            addView(setaOpcao())
            setOnClickListener {
                startActivity(Intent(this@OutrosActivity, VencimentosProdutosActivity::class.java))
            }
        }
        conteudo.addView(alertaVencimento, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })

        val ondeGuardou = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = forma(0xFFFFFFFF.toInt(), 0xFFE7ECEA.toInt(), 14)
            addView(iconeOpcao("⌖"), LinearLayout.LayoutParams(dp(48), dp(48)))
            addView(
                textosOpcao("Onde guardou", "Encontre produtos pelos locais de armazenamento."),
                LinearLayout.LayoutParams(0, -2, 1f)
            )
            addView(setaOpcao())
            setOnClickListener {
                startActivity(Intent(this@OutrosActivity, OndeGuardouProdutosActivity::class.java))
            }
        }
        conteudo.addView(ondeGuardou, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })

        val relatorioGastos = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = forma(0xFFFFFFFF.toInt(), 0xFFE7ECEA.toInt(), 14)
            addView(iconeOpcao("R$"), LinearLayout.LayoutParams(dp(48), dp(48)))
            addView(
                textosOpcao("Relatório de gastos", "Acompanhe quanto foi gasto com a despensa."),
                LinearLayout.LayoutParams(0, -2, 1f)
            )
            addView(setaOpcao())
            setOnClickListener {
                startActivity(Intent(this@OutrosActivity, GastosProdutosActivity::class.java))
            }
        }
        conteudo.addView(relatorioGastos, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })

        setContentView(raiz)
    }

    private fun iconeOpcao(icone: String) = TextView(this).apply {
        text = icone
        textSize = if (icone == "R$") 16f else 23f
        gravity = Gravity.CENTER
        setTextColor(verdeEscuro)
        background = forma(verdeClaro, verdeClaro, 11)
    }

    private fun textosOpcao(titulo: String, descricao: String) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(12), 0, dp(6), 0)
        addView(TextView(this@OutrosActivity).apply {
            text = titulo
            textSize = 15f
            setTextColor(texto)
            setTypeface(null, Typeface.BOLD)
        })
        addView(TextView(this@OutrosActivity).apply {
            text = descricao
            textSize = 12f
            setTextColor(secundario)
            setPadding(0, dp(4), 0, 0)
        })
    }

    private fun setaOpcao() = TextView(this).apply {
        text = "›"
        textSize = 24f
        setTextColor(secundario)
    }

    private fun forma(cor: Int, borda: Int, raioDp: Int) = GradientDrawable().apply {
        setColor(cor)
        cornerRadius = dp(raioDp).toFloat()
        setStroke(dp(1), borda)
    }

    private fun dp(valor: Int): Int =
        (valor * resources.displayMetrics.density).toInt()
}
