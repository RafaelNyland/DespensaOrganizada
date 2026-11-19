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

class EditarProdutosActivity : Activity() {
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
        montarTela(id, nome)
    }

    private fun montarTela(id: Long, nome: String) {
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
            text = "Editar produto"
            textSize = 22f
            setTextColor(0xFF1D3936.toInt())
            setTypeface(null, Typeface.BOLD)
        })
        cabecalho.addView(TextView(this).apply {
            text = "Localize o produto pelo nome ou código de barras"
            textSize = 12f
            setTextColor(cinza)
            setPadding(0, dp(4), 0, 0)
        })
        raiz.addView(cabecalho)

        cabecalho.addView(TextView(this).apply {
            text = id.toString()
            textSize = 20f
            setTextColor(cinza)
            setPadding(0, dp(4), 0, 0)
        })
        cabecalho.addView(TextView(this).apply {
            text = nome
            textSize = 20f
            setTextColor(cinza)
            setPadding(0, dp(4), 0, 0)
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
