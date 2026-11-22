package br.com.dispensa

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import java.io.ByteArrayOutputStream
import android.view.View


class TesteCadastroProdutoActivity : Activity() {
    private lateinit var bancoDados: ProdutoDatabase
    private lateinit var codigobarraInput: EditText
    private lateinit var fotoPreview: ImageView
    private var fotoProduto: ByteArray? = null

    companion object {
        private const val SOLICITACAO_FOTO = 2101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bancoDados = ProdutoDatabase(this)
        viewCabecalho()
    }

    private fun viewCabecalho() {

        //CABEÇALHO
        val fundo = 0xFFF5F7F6.toInt()
        val verdeEscuro = 0xFF10564F.toInt()
        val verdeClaro = 0xFFE7F2EF.toInt()
        val texto = 0xFF62716E.toInt()
        val verde = 0xFF11665D.toInt()
        val cinza = 0xFF78827D.toInt()

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
        cabecalho.addView(botao("<-", verdeEscuro) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }, LinearLayout.LayoutParams(dp(44), dp(44)))


        cabecalho.addView(TextView(this).apply {
            text = "Leia o codigo do Produto"
            textSize = 23f
            gravity = Gravity.CENTER
            setTextColor(verdeEscuro)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, dp(48), 1f))

        pagina.addView(cabecalho)

        pagina.addView(TextView(this).apply {
            text = "Leia o codigo de barras do item"
            textSize = 15f
            setTextColor(texto)
            setPadding(0, dp(8), 0, dp(8))
        })

        //RESTO DA PAGINA

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
        pagina.addView(codigoTitulo)

        val codigoLinha = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        codigobarraInput = campo("Leia ou digite o código")
        codigoLinha.addView(codigobarraInput, LinearLayout.LayoutParams(0, dp(48), 1f))

        codigoLinha.addView(Button(this).apply {
            text = "LER CÓDIGO"
            textSize = 11f
            setTextColor(0xFFFFFFFF.toInt())
            background = fundoArredondado(verde, dp(10))
            setOnClickListener { lerCodigoBarras() }
        }, LinearLayout.LayoutParams(dp(112), dp(48)).apply {
            leftMargin = dp(8)
        })
        val pagina2 = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(fundo)
            setPadding(dp(16), dp(0), dp(16), dp(0))
        }
        pagina2.addView(TextView(this).apply {
            text = "* ATENÇÃO a leitura não é 100% correta, após a leitura verifique se o codigo está correto"
            textSize = 11f
            setTextColor(cinza)
            setPadding(0, dp(10), 0, 0)
        })
        pagina.addView(codigoLinha)
        pagina.addView(pagina2)

        setContentView(pagina)
    }

    private fun rotulo(texto: String) = TextView(this).apply {
        text = texto
        textSize = 12f
        setTextColor(0xFF536258.toInt())
        setPadding(0, dp(8), 0, dp(4))
    }

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

    private fun campoParams() =
        LinearLayout.LayoutParams(-1, dp(44)).apply {
            bottomMargin = dp(8)
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

    private fun dp(valor: Int): Int =
        (valor * resources.displayMetrics.density).toInt()

    private fun lerCodigoBarras() {
        val opcoesLeitor = GmsBarcodeScannerOptions.Builder()
            .enableAutoZoom()
            .build()

        val tarefaLeitura = GmsBarcodeScanning.getClient(this, opcoesLeitor).startScan()
        tarefaLeitura.addOnSuccessListener { codigoLido ->
            val codigoBarras = codigoLido.rawValue.orEmpty().trim()
            val produtoEncontrado = bancoDados.buscarProdutoPorCodigoBarras(codigoBarras)

            if (produtoEncontrado == null) {
                AlertDialog.Builder(this)
                    .setTitle("O produto não está cadastrado")
                    .setMessage("Este código de barras não foi cadastrado.\nDeseja cadastrar?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Cadastrar") { _, _ ->
                        val telaCadastro = Intent(this, CadastroProdutoActivity::class.java).apply {
                            putExtra("codigo_barras", codigoBarras)
                        }
                        startActivity(telaCadastro)
                        codigobarraInput.text.clear()
                    }
                    .show()
                return@addOnSuccessListener
            }

            codigobarraInput.setText(codigoBarras)
            AlertDialog.Builder(this)
                .setTitle("Produto já cadastrado")
                .setMessage("Este código de barras já pertence a um produto salvo.\nDeseja registrar uma entrada?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Registrar") { _, _ ->
                    val telaEntrada = Intent(this, MovProdutosActivity::class.java).apply {
                        putExtra("produto_id", produtoEncontrado.id)
                        putExtra("produto_nome", produtoEncontrado.nome)
                        putExtra("tipo_movimentacao", "ENTRADA")
                    }
                    startActivity(telaEntrada)
                    codigobarraInput.text.clear()
                }
                .show()
        }
        tarefaLeitura.addOnCanceledListener { }
        tarefaLeitura.addOnFailureListener {
            Toast.makeText(
                this,
                "Não foi possível abrir o leitor. Tente novamente.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun fieldParams() =
        LinearLayout.LayoutParams(-1, dp(52)).apply { topMargin = dp(6); bottomMargin = dp(14) }

}

