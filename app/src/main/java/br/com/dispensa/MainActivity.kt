package br.com.dispensa

import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.app.Activity
import android.os.Bundle
import android.content.Intent

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            setPadding(0, dp(8), 0, dp(8))
        })

        // Botão para cadastrar produtos
        pagina.addView(botao("＋  CADASTRAR PRODUTOS", verdeEscuro) {
            val intent = Intent(this, CadastroProdutoActivity::class.java)
            startActivity(intent)
        }, LinearLayout.LayoutParams(-1, dp(44)))

        // Atalhos
        val atalhos = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val opcoes = listOf(
            "↑  ↓\nENTRADAS\nE RETIRADAS",
            "⬡\nMEUS\nPRODUTOS",
            "▱\nLISTA DE\nCOMPRAS",
            "•••\nOutros"
        )

        opcoes.forEach { titulo ->
            atalhos.addView(TextView(this).apply {
                text = titulo
                textSize = 11f
                gravity = Gravity.CENTER
                setTextColor(verdeEscuro)
                background = formato(verdeClaro, dp(12))
                setPadding(dp(4), dp(6), dp(4), dp(6))
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
            }, LinearLayout.LayoutParams(0, dp(72), 1f).apply {
                setMargins(dp(3), dp(10), dp(3), 0)
            })
        }
        pagina.addView(atalhos)

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

}
