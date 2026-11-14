package br.com.dispensa

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.Locale
import android.content.Intent


class MeusProdutosActivity : Activity() {
    private lateinit var bancoDados: ProdutoDatabase
    private lateinit var listaProdutos: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bancoDados = ProdutoDatabase(this)
        montarTela()
        mostrarProdutos()
    }

    private fun montarTela() {
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

        val rolagem = ScrollView(this)
        listaProdutos = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        rolagem.addView(listaProdutos)
        pagina.addView(rolagem, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(pagina)
    }

    private fun mostrarProdutos() {
        listaProdutos.removeAllViews()
        val produtos = bancoDados.allProducts()
        if (produtos.isEmpty()) {
            listaProdutos.addView(TextView(this).apply {
                text = "Nenhum produto cadastrado."
                textSize = 16f
                setTextColor(0xFF62716E.toInt())
                setPadding(0, dp(12), 0, dp(12))
            })
            return
        }

        produtos.forEach { produto ->
            val cartao = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), dp(12), dp(12), dp(12))
                setBackgroundColor(0xFFFFFFFF.toInt())
                elevation = dp(2).toFloat()
            }

            val fotoProduto = produto.foto
            if (fotoProduto != null && fotoProduto.isNotEmpty()) {
                cartao.addView(ImageView(this).apply {
                    contentDescription = "Foto de ${produto.nome}"
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageBitmap(BitmapFactory.decodeByteArray(fotoProduto, 0, fotoProduto.size))
                }, LinearLayout.LayoutParams(-1, dp(180)).apply {
                    bottomMargin = dp(10)
                })
            } else {
                cartao.addView(TextView(this).apply {
                    text = "Sem foto"
                    gravity = Gravity.CENTER
                    setTextColor(0xFF62716E.toInt())
                    setBackgroundColor(0xFFE7F2EF.toInt())
                }, LinearLayout.LayoutParams(-1, dp(100)).apply {
                    bottomMargin = dp(10)
                })
            }

            cartao.addView(TextView(this).apply {
                text = produto.nome
                textSize = 19f
                setTextColor(0xFF10564F.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
            cartao.addView(TextView(this).apply {
                text = "Categoria: ${produto.categoria}\n" +
                    "Código de barras: ${produto.codigo_barras.ifBlank { "Não informado" }}\n" +
                    "Conteúdo por embalagem: ${formatarQuantidade(produto.conteudo_embalagem)} ${produto.unidade}\n" +
                    "Estoque mínimo: ${produto.estoque_minimo} embalagem(ns)"
                textSize = 14f
                setTextColor(0xFF536258.toInt())
                setPadding(0, dp(6), 0, 0)
            })

            listaProdutos.addView(cartao, LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = dp(12)
            })
        }
    }

    private fun formatarQuantidade(quantidade: Double): String =
        String.format(Locale("pt", "BR"), "%.2f", quantidade).trimEnd('0').trimEnd(',')

        
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

}
