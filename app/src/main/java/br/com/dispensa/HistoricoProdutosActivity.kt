package br.com.dispensa

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.ScrollView
import android.widget.TextView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HistoricoProdutosActivity : Activity() {
    private val verdeEscuro = 0xFF193B38.toInt()
    private val verde = 0xFF11665D.toInt()
    private val fundo = 0xFFF4F7F6.toInt()
    private val cinza = 0xFF71817D.toInt()
    private val linha = 0xFFE7ECEA.toInt()
    private val vermelho = 0xFFC44739.toInt()
    private lateinit var bancoDados: ProdutoDatabase
    private lateinit var listaHistorico: LinearLayout
    private lateinit var textoRodapeHistorico: TextView
    private lateinit var textoPeriodoHistorico: TextView
    private var anoPeriodoHistorico = 0
    private var mesPeriodoHistorico = 0
    private var buscaHistorico = ""

    override fun onCreate(estadoSalvo: Bundle?) {
        super.onCreate(estadoSalvo)
        bancoDados = ProdutoDatabase(this)
        val calendarioAtual = Calendar.getInstance()
        anoPeriodoHistorico = calendarioAtual.get(Calendar.YEAR)
        mesPeriodoHistorico = calendarioAtual.get(Calendar.MONTH)
        montarTelaHistorico()
    }

    private fun montarTelaHistorico() {
        val raiz = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(fundo)
        }

        val cabecalhoHistorico = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(36), dp(16), dp(12))
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        cabecalhoHistorico.addView(TextView(this).apply {
            text = "Histórico"
            textSize = 22f
            setTextColor(verdeEscuro)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(10), 0, 0)
        })
        raiz.addView(cabecalhoHistorico)

        val rolagemHistorico = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
        }
        val conteudoHistorico = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(14), dp(12), dp(16))
        }
        rolagemHistorico.addView(conteudoHistorico)
        raiz.addView(rolagemHistorico, LinearLayout.LayoutParams(-1, 0, 1f))

        conteudoHistorico.addView(TextView(this).apply {
            text = "Período"
            textSize = 11f
            setTextColor(cinza)
            setPadding(0, 0, 0, dp(5))
        })

        textoPeriodoHistorico = TextView(this).apply {
            text = formatarPeriodoHistorico(anoPeriodoHistorico, mesPeriodoHistorico)
            textSize = 13f
            setTextColor(verdeEscuro)
            gravity = Gravity.CENTER_VERTICAL
            setSingleLine(true)
        }
        val campoPeriodoHistorico = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), 0, dp(9), 0)
            background = forma(0xFFFFFFFF.toInt(), linha, 10)
            addView(textoPeriodoHistorico, LinearLayout.LayoutParams(0, -1, 1f))
            addView(TextView(this@HistoricoProdutosActivity).apply {
                text = "▦"
                textSize = 18f
                gravity = Gravity.CENTER
                setTextColor(verde)
                contentDescription = "Escolher período"
            }, LinearLayout.LayoutParams(dp(28), -1))
            setOnClickListener { abrirSeletorPeriodoHistorico() }
        }
        conteudoHistorico.addView(campoPeriodoHistorico, LinearLayout.LayoutParams(-1, dp(46)).apply {
            bottomMargin = dp(10)
        })

        val campoBuscaHistorico = EditText(this).apply {
            hint = "⌕  Buscar por nome"
            textSize = 12f
            setTextColor(verdeEscuro)
            setHintTextColor(cinza)
            setPadding(dp(10), 0, dp(10), 0)
            background = forma(0xFFFFFFFF.toInt(), linha, 10)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(texto: CharSequence?, inicio: Int, quantidade: Int, depois: Int) = Unit
                override fun onTextChanged(texto: CharSequence?, inicio: Int, antes: Int, quantidade: Int) {
                    buscaHistorico = texto?.toString().orEmpty().trim()
                    atualizarListaHistorico()
                }

                override fun afterTextChanged(texto: Editable?) = Unit
            })
        }
        conteudoHistorico.addView(campoBuscaHistorico, LinearLayout.LayoutParams(-1, dp(42)).apply {
            bottomMargin = dp(12)
        })

        listaHistorico = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        conteudoHistorico.addView(listaHistorico)
        textoRodapeHistorico = TextView(this).apply {
            textSize = 10f
            setTextColor(cinza)
            setPadding(0, dp(4), 0, 0)
        }
        conteudoHistorico.addView(textoRodapeHistorico)

        setContentView(raiz)
        atualizarListaHistorico()
    }

    private fun abrirSeletorPeriodoHistorico() {
        val calendarioAtual = Calendar.getInstance()
        val seletorMesHistorico = NumberPicker(this).apply {
            minValue = 1
            maxValue = 12
            value = mesPeriodoHistorico + 1
            displayedValues = arrayOf(
                "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
            )
        }
        val seletorAnoHistorico = NumberPicker(this).apply {
            minValue = calendarioAtual.get(Calendar.YEAR) - 5
            maxValue = calendarioAtual.get(Calendar.YEAR) + 10
            value = anoPeriodoHistorico
        }
        val seletoresPeriodoHistorico = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(12), dp(8), dp(12), dp(4))
            addView(seletorMesHistorico, LinearLayout.LayoutParams(0, -2, 1f))
            addView(seletorAnoHistorico, LinearLayout.LayoutParams(0, -2, 1f))
        }
        AlertDialog.Builder(this)
            .setTitle("Período do histórico")
            .setView(seletoresPeriodoHistorico)
            .setPositiveButton("Aplicar") { _, _ ->
                anoPeriodoHistorico = seletorAnoHistorico.value
                mesPeriodoHistorico = seletorMesHistorico.value - 1
                textoPeriodoHistorico.text = formatarPeriodoHistorico(anoPeriodoHistorico, mesPeriodoHistorico)
                atualizarListaHistorico()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun atualizarListaHistorico() {
        val itensHistorico = bancoDados.listarMovimentacoesHistorico(
            anoPeriodoHistorico,
            mesPeriodoHistorico,
            buscaHistorico
        )
        listaHistorico.removeAllViews()

        if (itensHistorico.isEmpty()) {
            listaHistorico.addView(TextView(this).apply {
                text = if (buscaHistorico.isBlank()) {
                    "Nenhuma movimentação neste período."
                } else {
                    "Nenhuma movimentação encontrada para essa busca."
                }
                textSize = 13f
                setTextColor(cinza)
                gravity = Gravity.CENTER
                setPadding(dp(12), dp(30), dp(12), dp(30))
            })
            textoRodapeHistorico.text = "Exibindo 0 registros no período."
            return
        }

        val movimentosPorDia = itensHistorico.groupBy { itemHistorico: ItemHistorico ->
            obterInicioDoDiaHistorico(itemHistorico.dataHora)
        }
        movimentosPorDia.forEach { (diaEmMillis, movimentosDoDia) ->
            listaHistorico.addView(TextView(this).apply {
                text = tituloDiaHistorico(diaEmMillis)
                textSize = 13f
                setTextColor(verdeEscuro)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, dp(2), 0, dp(6))
            })
            listaHistorico.addView(criarGrupoDiaHistorico(movimentosDoDia), LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = dp(10)
            })
        }
        textoRodapeHistorico.text = "Exibindo ${itensHistorico.size} registros no período."
    }

    private fun criarGrupoDiaHistorico(itens: List<ItemHistorico>): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(8), dp(2), dp(8), dp(2))
        background = forma(0xFFFFFFFF.toInt(), linha, 12)
        itens.forEachIndexed { indice, item ->
            addView(criarLinhaHistorico(item), LinearLayout.LayoutParams(-1, -2))
            if (indice < itens.lastIndex) {
                addView(
                    View(this@HistoricoProdutosActivity).apply { setBackgroundColor(linha) },
                    LinearLayout.LayoutParams(-1, dp(1)).apply {
                        leftMargin = dp(28)
                        rightMargin = dp(2)
                    })
            }
        }
    }

    private fun criarLinhaHistorico(item: ItemHistorico): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(2), dp(10), dp(2), dp(10))

        val entradaHistorico = item.tipoMovimentacao.equals("ENTRADA", ignoreCase = true)
        val corMovimento = if (entradaHistorico) verde else vermelho
        val fundoMovimento = if (entradaHistorico) 0xFFE8F4F0.toInt() else 0xFFFFEFEC.toInt()
        addView(TextView(this@HistoricoProdutosActivity).apply {
            text = if (entradaHistorico) "↙" else "↗"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(corMovimento)
            background = forma(fundoMovimento, fundoMovimento, 9)
        }, LinearLayout.LayoutParams(dp(32), dp(34)))

        val textosHistorico = LinearLayout(this@HistoricoProdutosActivity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), 0, dp(3), 0)
        }
        textosHistorico.addView(TextView(this@HistoricoProdutosActivity).apply {
            text = item.nomeProduto
            textSize = 10f
            setTextColor(verdeEscuro)
            setTypeface(null, Typeface.BOLD)
        })
        val horaHistorico = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(item.dataHora))
        val detalheMovimentacao = if (entradaHistorico) "Entrada" else "Retirada"
        textosHistorico.addView(TextView(this@HistoricoProdutosActivity).apply {
            text = "$detalheMovimentacao • $horaHistorico"
            textSize = 9f
            setTextColor(cinza)
            setPadding(0, dp(2), 0, 0)
        })
        val detalheValorHistorico = if (entradaHistorico) {
            "Compra ${formatarMoedaHistorico(item.precoUnitario * item.quantidade)} • Saldo: ${item.saldoAtual} un."
        } else {
            "Consumo • Saldo: ${item.saldoAtual} un."
        }
        textosHistorico.addView(TextView(this@HistoricoProdutosActivity).apply {
            text = detalheValorHistorico
            textSize = 8f
            setTextColor(cinza)
            setPadding(0, dp(2), 0, 0)
        })
        addView(textosHistorico, LinearLayout.LayoutParams(0, -2, 1f))

        addView(TextView(this@HistoricoProdutosActivity).apply {
            val sinal = if (entradaHistorico) "+" else "−"
            text = "$sinal${item.quantidade} un."
            textSize = 9f
            setTextColor(corMovimento)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
        })
    }

    private fun obterInicioDoDiaHistorico(dataEmMillis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = dataEmMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun tituloDiaHistorico(diaEmMillis: Long): String {
        val hoje = obterInicioDoDiaHistorico(System.currentTimeMillis())
        val ontem = hoje - 24L * 60L * 60L * 1000L
        return when (diaEmMillis) {
            hoje -> "Hoje, ${SimpleDateFormat("dd 'de' MMMM", Locale("pt", "BR")).format(Date(diaEmMillis))}"
            ontem -> "Ontem, ${SimpleDateFormat("dd 'de' MMMM", Locale("pt", "BR")).format(Date(diaEmMillis))}"
            else -> SimpleDateFormat("dd 'de' MMMM", Locale("pt", "BR")).format(Date(diaEmMillis))
        }
    }

    private fun formatarPeriodoHistorico(ano: Int, mes: Int): String {
        val inicioMes = Calendar.getInstance().apply {
            set(Calendar.YEAR, ano)
            set(Calendar.MONTH, mes)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val fimMes = inicioMes.clone() as Calendar
        fimMes.set(Calendar.DAY_OF_MONTH, fimMes.getActualMaximum(Calendar.DAY_OF_MONTH))
        val nomeMesAno = SimpleDateFormat("'de' MMMM 'de' yyyy", Locale("pt", "BR")).format(inicioMes.time)
        return "01 a ${String.format(Locale.getDefault(), "%02d", fimMes.get(Calendar.DAY_OF_MONTH))} $nomeMesAno"
    }

    private fun formatarMoedaHistorico(valor: Double): String =
        NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(valor)

    private fun forma(cor: Int, borda: Int, raioDp: Int) = GradientDrawable().apply {
        setColor(cor)
        cornerRadius = dp(raioDp).toFloat()
        setStroke(dp(1), borda)
    }

    private fun dp(valor: Int): Int = (valor * resources.displayMetrics.density).toInt()
}
