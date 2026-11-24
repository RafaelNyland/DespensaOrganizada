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


class CadastroProdutoActivity : Activity() {
    private lateinit var bancoDados: ProdutoDatabase
    private lateinit var codigobarraInput: EditText
    private lateinit var fotoPreview: ImageView
    private var fotoProduto: ByteArray? = null

    companion object {
        private const val SOLICITACAO_FOTO = 2101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val codigoBarrasexterno = intent.getStringExtra("codigo_barras").orEmpty()
        bancoDados = ProdutoDatabase(this)
        viewCabecalho(codigoBarrasexterno)
    }

    private fun viewCabecalho(codigoBarrasexterno: String) {

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
            text = "Cadastrar Produto"
            textSize = 23f
            gravity = Gravity.CENTER
            setTextColor(verdeEscuro)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, dp(48), 1f))

        pagina.addView(cabecalho)

        pagina.addView(TextView(this).apply {
            text = "Cadastre um novo item antes de guardar as compras"
            textSize = 15f
            setTextColor(texto)
            setPadding(0, dp(8), 0, dp(8))
        })

        //RESTO DA PAGINA

        pagina.addView(rotulo("Nome do produto *"))
        val nomeInput = campo("Ex.: Aveia em flocos")
        pagina.addView(nomeInput, campoParams())

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
        pagina.addView(codigoTitulo)

        val codigoLinha = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        codigobarraInput = campo("Leia ou digite o código")
        if (codigoBarrasexterno.isNotEmpty()) {
            codigobarraInput.setText(codigoBarrasexterno)
        }
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
        pagina.addView(codigoLinha)
        pagina.addView(pagina2)
        pagina.addView(rotulo("Foto do produto"))
        val fotoLinha = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        fotoLinha.addView(Button(this).apply {
            text = "TIRAR FOTO"
            textSize = 12f
            setTextColor(0xFFFFFFFF.toInt())
            background = fundoArredondado(verde, dp(10))
            setOnClickListener { tirarFoto() }
        }, LinearLayout.LayoutParams(dp(112), dp(44)).apply {
            rightMargin = dp(12)
        })
        fotoPreview = ImageView(this).apply {
            contentDescription = "Prévia da foto do produto"
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(0xFFE5EDDF.toInt())
            visibility = ImageView.GONE
        }
        fotoLinha.addView(fotoPreview, LinearLayout.LayoutParams(dp(88), dp(88)))
        pagina.addView(fotoLinha, LinearLayout.LayoutParams(-1, dp(96)).apply {
            bottomMargin = dp(12)
        })

        pagina.addView(rotulo("Categoria *"))
        val categoria = Spinner(this).apply {
            adapter = android.widget.ArrayAdapter(
                this@CadastroProdutoActivity,
                android.R.layout.simple_spinner_dropdown_item,
                listOf(
                    "Alimentos", "Frios", "Frutas", "Legumes",
                    "Congelados", "Limpeza", "Higiene", "Medicamentos"
                )
            )
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        pagina.addView(categoria, campoParams())

        val conteudoLinha = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val conteudoColuna = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        conteudoColuna.addView(rotulo("Conteúdo por embalagem *"))
        val conteudoEmbalagemInput = campo(
            "Ex.: 500",
            android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        )
        conteudoColuna.addView(conteudoEmbalagemInput, campoParams())

        val unidadeColuna = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), 0, 0, 0)
        }
        unidadeColuna.addView(rotulo("Unidade *"))
        val unidade = Spinner(this).apply {
            adapter = android.widget.ArrayAdapter(
                this@CadastroProdutoActivity,
                android.R.layout.simple_spinner_dropdown_item,
                listOf("Unidade (un.)", "g", "kg", "ml", "l", "Pacote")
            )
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        unidadeColuna.addView(unidade, campoParams())

        conteudoLinha.addView(conteudoColuna, LinearLayout.LayoutParams(0, -2, 1f))
        conteudoLinha.addView(unidadeColuna, LinearLayout.LayoutParams(0, -2, 1f))
        pagina.addView(conteudoLinha)

        pagina.addView(rotulo("Estoque mínimo"))
        val estoqueMinimo = campo(
            "Ex.: 1",
            android.text.InputType.TYPE_CLASS_NUMBER
        )
        pagina.addView(estoqueMinimo, campoParams())

        pagina.addView(Button(this).apply {
            text = "＋  CADASTRAR PRODUTO"
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            background = fundoArredondado(verde, dp(10))
            setOnClickListener {
                salvarProduto(nomeInput, categoria, conteudoEmbalagemInput, unidade, estoqueMinimo)
            }
        }, LinearLayout.LayoutParams(-1, dp(50)).apply {
            topMargin = dp(8)
        })

        pagina.addView(TextView(this).apply {
            text = "* Campos obrigatórios"
            textSize = 11f
            gravity = android.view.Gravity.CENTER
            setTextColor(cinza)
            setPadding(0, dp(10), 0, 0)
        })

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

    private fun formato(cor: Int, raio: Int): android.graphics.drawable.GradientDrawable =
        android.graphics.drawable.GradientDrawable().apply {
            setColor(cor)
            cornerRadius = raio.toFloat()
        }

    private fun lerCodigoBarras() {
        val opcoesLeitor = GmsBarcodeScannerOptions.Builder()
            .enableAutoZoom()
            .build()

        GmsBarcodeScanning.getClient(this, opcoesLeitor).startScan()
            .addOnSuccessListener { codigoLido ->
                val codigoBarras = codigoLido.rawValue.orEmpty().trim()
                codigobarraInput.setText(codigoBarras)
                if (codigoBarras.isNotEmpty() && bancoDados.barcodeExists(codigoBarras)) {
                    AlertDialog.Builder(this)
                        .setTitle("Produto já cadastrado")
                        .setMessage("Este código de barras já pertence a um produto salvo.")
                        .setPositiveButton("OK", null)
                        .show()
                    codigobarraInput.requestFocus()
                    codigobarraInput.setText(null)

                }
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

    // a parte de tirar foto do produto
    private fun tirarFoto() {
        try {
            startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), SOLICITACAO_FOTO)
        } catch (_: Exception) {
            Toast.makeText(this, "Não foi encontrado um app de câmera.", Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Usado para receber a foto da câmera")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SOLICITACAO_FOTO && resultCode == RESULT_OK) {
            val bitmap = data?.extras?.get("data") as? Bitmap
            if (bitmap != null) {
                fotoPreview.setImageBitmap(bitmap)
                fotoPreview.visibility = View.VISIBLE
                val fluxo = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fluxo)
                fotoProduto = fluxo.toByteArray()
            } else {
                Toast.makeText(this, "Não foi possível carregar a prévia da foto.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fieldParams() = LinearLayout.LayoutParams(-1, dp(52)).apply { topMargin = dp(6); bottomMargin = dp(14) }


    //salvar produto no banco de dados
    private fun salvarProduto(
        campoNome: EditText,
        categoria: Spinner,
        campoConteudoEmbalagem: EditText,
        unidade: Spinner,
        campoEstoqueMinimo: EditText
    ) {
        val nome = campoNome.text.toString().trim()
        val codigoBarras = codigobarraInput.text.toString().trim()
        val conteudoEmbalagem = campoConteudoEmbalagem.text.toString().replace(',', '.').toDoubleOrNull()
        val estoqueMinimo = campoEstoqueMinimo.text.toString().toIntOrNull()
        if (nome.isBlank()) {
            campoNome.error = "Informe o nome do produto"
            return
        }
        if (bancoDados.nomeExiste(nome)) {
            AlertDialog.Builder(this)
                .setTitle("Produto já cadastrado")
                .setMessage("Já existe um produto com esse nome.")
                .setPositiveButton("OK", null)
                .show()
            campoNome.requestFocus()
            return
        }
        if (codigoBarras.isNotEmpty() && bancoDados.barcodeExists(codigoBarras)) {
            AlertDialog.Builder(this)
                .setTitle("Código de barras já cadastrado")
                .setMessage("Já existe um produto com esse código de barras.")
                .setPositiveButton("OK", null)
                .show()
            codigobarraInput.requestFocus()
            return
        }
        if (conteudoEmbalagem == null || conteudoEmbalagem <= 0) {
            campoConteudoEmbalagem.error = "Informe um conteúdo maior que zero"
            return
        }
        if (estoqueMinimo == null || estoqueMinimo < 0) {
            campoEstoqueMinimo.error = "Informe um mínimo válido"
            return
        }
        if (fotoProduto == null) {
            Toast.makeText(this, "Tire uma foto do produto", Toast.LENGTH_SHORT).show()
            return
        }
        bancoDados.cadastrarProduto(
            Produto(
                id = 0,
                nome = nome,
                categoria = categoria.selectedItem.toString(),
                codigo_barras = codigoBarras,
                conteudo_embalagem = conteudoEmbalagem,
                unidade = unidade.selectedItem.toString(),
                estoque_minimo = estoqueMinimo,
                foto = fotoProduto
            )
        )
        campoNome.text.clear()
        codigobarraInput.text.clear()
        campoConteudoEmbalagem.text.clear()
        campoEstoqueMinimo.text.clear()
        fotoProduto = null
        fotoPreview.setImageDrawable(null)
        fotoPreview.visibility = ImageView.GONE
        categoria.setSelection(0)
        unidade.setSelection(0)
        AlertDialog.Builder(this)
            .setTitle("Produto salvo")
            .setMessage("O produto foi cadastrado com sucesso.")
            .setPositiveButton("OK", null)
            .show()
    }
}

