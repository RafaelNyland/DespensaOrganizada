package br.com.dispensa

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import java.io.ByteArrayOutputStream
import java.util.Locale

class ProductRegistrationActivity : Activity() {
    private lateinit var database: ProductDatabase
    private lateinit var nameInput: EditText
    private lateinit var barcodeInput: EditText
    private lateinit var quantityInput: EditText
    private lateinit var minimumInput: EditText
    private lateinit var unitSpinner: Spinner
    private lateinit var photoPreview: ImageView
    private lateinit var productsList: LinearLayout
    private var productPhoto: ByteArray? = null

    companion object {
        private const val PHOTO_REQUEST = 2101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = ProductDatabase(this)
        buildScreen()
        renderProducts()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val focused = currentFocus
            if (focused is EditText) {
                val bounds = Rect()
                focused.getGlobalVisibleRect(bounds)
                if (!bounds.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    focused.clearFocus()
                    (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                        .hideSoftInputFromWindow(focused.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun buildScreen() {
        val scroll = ScrollView(this).apply { setBackgroundColor(0xFFF6F7F2.toInt()) }
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(24))
        }
        scroll.addView(page)
        setContentView(scroll)

        page.addView(TextView(this).apply {
            text = "Cadastrar produto"
            textSize = 26f
            setTextColor(0xFF203B2C.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        page.addView(TextView(this).apply {
            text = "Preencha os dados do produto"
            textSize = 15f
            setTextColor(0xFF657269.toInt())
            setPadding(0, dp(4), 0, dp(18))
        })

        page.addView(label("Nome do produto"))
        nameInput = textInput("Ex.: Arroz")
        page.addView(nameInput, fieldParams())

        page.addView(label("Código de barras"))
        val barcodeRow = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        barcodeInput = textInput("Leia ou digite o código")
        barcodeRow.addView(barcodeInput, LinearLayout.LayoutParams(0, dp(52), 1f))
        barcodeRow.addView(Button(this).apply {
            text = "Ler código"
            setOnClickListener { scanBarcode() }
        }, LinearLayout.LayoutParams(-2, dp(52)).apply { leftMargin = dp(8) })
        page.addView(barcodeRow, fieldParams())

        page.addView(label("Foto do produto"))
        photoPreview = ImageView(this).apply {
            contentDescription = "Prévia da foto do produto"
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(0xFFE5EDDF.toInt())
            visibility = ImageView.GONE
        }
        page.addView(photoPreview, LinearLayout.LayoutParams(dp(132), dp(132)).apply { topMargin = dp(8) })
        page.addView(Button(this).apply {
            text = "Tirar foto"
            setOnClickListener { takePhoto() }
        }, fieldParams())

        page.addView(label("Quantidade"))
        val quantityRow = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        quantityInput = textInput("Ex.: 500", android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL)
        quantityInput.imeOptions = EditorInfo.IME_ACTION_NEXT
        quantityInput.setOnEditorActionListener { _, actionId, event ->
            val nextPressed = actionId == EditorInfo.IME_ACTION_NEXT ||
                (event?.let { it.keyCode == KeyEvent.KEYCODE_ENTER && it.action == KeyEvent.ACTION_UP } == true)
            if (nextPressed) {
                minimumInput.requestFocus()
                true
            } else false
        }
        quantityRow.addView(quantityInput, LinearLayout.LayoutParams(0, dp(52), 1f))
        unitSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@ProductRegistrationActivity, android.R.layout.simple_spinner_dropdown_item, listOf("g", "kg", "ml", "l"))
        }
        quantityRow.addView(unitSpinner, LinearLayout.LayoutParams(dp(100), dp(52)).apply { leftMargin = dp(8) })
        page.addView(quantityRow, fieldParams())

        page.addView(label("Estoque mínimo (mesma unidade da quantidade)"))
        minimumInput = textInput("Ex.: 200", android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL)
        minimumInput.imeOptions = EditorInfo.IME_ACTION_DONE
        minimumInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(minimumInput.windowToken, 0)
                minimumInput.clearFocus()
                true
            } else false
        }
        page.addView(minimumInput, fieldParams())

        page.addView(Button(this).apply {
            text = "Salvar produto"
            textSize = 16f
            setOnClickListener { saveProduct() }
        }, LinearLayout.LayoutParams(-1, dp(54)).apply { topMargin = dp(12) })

        page.addView(TextView(this).apply {
            text = "Produtos cadastrados"
            textSize = 20f
            setTextColor(0xFF203B2C.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, dp(24), 0, dp(10))
        })
        productsList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        page.addView(productsList, matchWidth())
    }

    private fun scanBarcode() {
        GmsBarcodeScanning.getClient(this).startScan()
            .addOnSuccessListener { barcode -> barcodeInput.setText(barcode.rawValue.orEmpty()) }
            .addOnCanceledListener { }
            .addOnFailureListener {
                Toast.makeText(this, "Não foi possível abrir o leitor. Tente novamente.", Toast.LENGTH_LONG).show()
            }
    }

    private fun takePhoto() {
        try {
            startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), PHOTO_REQUEST)
        } catch (_: Exception) {
            Toast.makeText(this, "Não foi encontrado um app de câmera.", Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Uses the system camera app to capture a product photo")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK) {
            val bitmap = data?.extras?.get("data") as? Bitmap ?: return
            val output = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
            productPhoto = output.toByteArray()
            photoPreview.setImageBitmap(bitmap)
            photoPreview.visibility = ImageView.VISIBLE
        }
    }

    private fun saveProduct() {
        val name = nameInput.text.toString().trim()
        val quantity = quantityInput.text.toString().replace(',', '.').toDoubleOrNull()
        val minimum = minimumInput.text.toString().replace(',', '.').toDoubleOrNull()
        if (name.isBlank()) {
            nameInput.error = "Informe o nome do produto"
            return
        }
        if (quantity == null || quantity < 0) {
            quantityInput.error = "Informe uma quantidade válida"
            return
        }
        if (minimum == null || minimum < 0) {
            minimumInput.error = "Informe um estoque mínimo válido"
            return
        }

        if (database.nameExists(name)) {
            nameInput.error = "Já existe um produto com esse nome"
            nameInput.requestFocus()
            return
        }
        val barcode = barcodeInput.text.toString().trim()
        if (database.barcodeExists(barcode)) {
            barcodeInput.error = "Esse código de barras já está cadastrado"
            barcodeInput.requestFocus()
            return
        }

        database.addProduct(
            Product(
                id = 0,
                name = name,
                barcode = barcode,
                quantity = quantity,
                unit = unitSpinner.selectedItem.toString(),
                minimum = minimum,
                photo = productPhoto
            )
        )
        nameInput.text.clear()
        barcodeInput.text.clear()
        quantityInput.text.clear()
        minimumInput.text.clear()
        productPhoto = null
        photoPreview.setImageDrawable(null)
        photoPreview.visibility = ImageView.GONE
        unitSpinner.setSelection(0)
        Toast.makeText(this, "Produto salvo", Toast.LENGTH_SHORT).show()
        renderProducts()
    }

    private fun renderProducts() {
        productsList.removeAllViews()
        val products = database.allProducts()
        if (products.isEmpty()) {
            productsList.addView(TextView(this).apply {
                text = "Ainda não há produtos cadastrados."
                textSize = 14f
                setTextColor(0xFF657269.toInt())
                setPadding(0, dp(4), 0, dp(12))
            })
        }
        products.forEach { product ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(10), dp(12), dp(10))
                setBackgroundColor(0xFFFFFFFF.toInt())
                elevation = dp(1).toFloat()
            }
            product.photo?.let { bytes ->
                card.addView(ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(dp(56), dp(56)).apply { rightMargin = dp(12) }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                })
            }
            val details = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            details.addView(TextView(this).apply {
                text = product.name
                textSize = 16f
                setTextColor(0xFF203B2C.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
            val codeText = if (product.barcode.isBlank()) "" else "  •  Código: ${product.barcode}"
            details.addView(TextView(this).apply {
                text = "Estoque: ${formatAmount(product.quantity)} ${product.unit}  •  Mínimo: ${formatAmount(product.minimum)} ${product.unit}$codeText"
                textSize = 12f
                setTextColor(0xFF657269.toInt())
                setPadding(0, dp(3), 0, 0)
            })
            card.addView(details, LinearLayout.LayoutParams(0, -2, 1f))
            card.addView(Button(this).apply {
                text = "Excluir"
                setOnClickListener {
                    AlertDialog.Builder(this@ProductRegistrationActivity)
                        .setTitle("Excluir produto")
                        .setMessage("Deseja excluir ${product.name}?")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Excluir") { _, _ ->
                            database.deleteProduct(product.id)
                            renderProducts()
                        }
                        .show()
                }
            }, LinearLayout.LayoutParams(-2, dp(48)).apply { leftMargin = dp(8) })
            productsList.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) })
        }
    }

    private fun label(value: String) = TextView(this).apply {
        text = value
        textSize = 14f
        setTextColor(0xFF34483A.toInt())
        setTypeface(null, android.graphics.Typeface.BOLD)
    }

    private fun textInput(hintText: String, inputType: Int = android.text.InputType.TYPE_CLASS_TEXT) = EditText(this).apply {
        hint = hintText
        this.inputType = inputType
        setSingleLine(true)
        setPadding(dp(12), 0, dp(12), 0)
    }

    private fun formatAmount(value: Double) = String.format(Locale("pt", "BR"), "%.2f", value).trimEnd('0').trimEnd(',')
    private fun fieldParams() = LinearLayout.LayoutParams(-1, dp(52)).apply { topMargin = dp(6); bottomMargin = dp(14) }
    private fun matchWidth() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
