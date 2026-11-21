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
import android.widget.TextView

class VencimentosProdutosActivity : Activity() {
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
            text = "Alerta de vencimento"
            textSize = 21f
            setTextColor(0xFF1D3936.toInt())
            setTypeface(null, Typeface.BOLD)
        })
        cabecalho.addView(TextView(this).apply {
            text = "Consuma primeiro o que está mais perto de vencer"
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



