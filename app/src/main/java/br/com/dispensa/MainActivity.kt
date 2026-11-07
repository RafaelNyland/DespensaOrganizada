package br.com.dispensa

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(28), dp(24), dp(28), dp(24))
            setBackgroundColor(0xFFF6F7F2.toInt())
        }
        setContentView(page)

        page.addView(TextView(this).apply {
            text = "Minha Dispensa"
            textSize = 30f
            setTextColor(0xFF203B2C.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
        })
        page.addView(TextView(this).apply {
            text = "Organize os produtos da sua casa"
            textSize = 16f
            setTextColor(0xFF657269.toInt())
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(28))
        })
        page.addView(Button(this).apply {
            text = "Cadastrar novos produtos"
            textSize = 16f
            setOnClickListener {
                startActivity(Intent(this@MainActivity, ProductRegistrationActivity::class.java))
            }
        }, LinearLayout.LayoutParams(-1, dp(58)))
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
