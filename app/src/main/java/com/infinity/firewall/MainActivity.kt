package com.infinity.firewall

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private val black = Color.rgb(8, 11, 16)
    private val gold = Color.rgb(229, 195, 106)
    private val ivory = Color.rgb(245, 239, 220)

    private fun makeText(
        value: String,
        size: Float,
        color: Int
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER
            typeface = Typeface.create(
                "sans-serif",
                Typeface.BOLD
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = black
        window.navigationBarColor = black
        window.decorView.systemUiVisibility = 0

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 32, 28, 32)
            setBackgroundColor(black)
        }

        val symbol = makeText("∞", 100f, gold)
        layout.addView(
            symbol,
            LinearLayout.LayoutParams(-1, 150)
        )

        val title = makeText("INFINITY", 32f, gold)
        layout.addView(
            title,
            LinearLayout.LayoutParams(-1, 60)
        )

        val subtitle = makeText(
            "YOUR INTERNET. YOUR CONTROL.",
            13f,
            ivory
        )
        layout.addView(
            subtitle,
            LinearLayout.LayoutParams(-1, 44)
        )

        val divider = android.view.View(this).apply {
            setBackgroundColor(gold)
        }
        val dividerParams = LinearLayout.LayoutParams(-1, 2)
        dividerParams.setMargins(12, 28, 12, 28)
        layout.addView(divider, dividerParams)

        val status = makeText(
            "FIREWALL NOT STARTED",
            16f,
            ivory
        )
        layout.addView(
            status,
            LinearLayout.LayoutParams(-1, 60)
        )

        val button = makeText(
            "BLOCK ALL INTERNET",
            17f,
            black
        ).apply {
            setBackgroundColor(gold)
            setPadding(12, 12, 12, 12)
        }

        val buttonParams = LinearLayout.LayoutParams(-1, 64)
        buttonParams.setMargins(0, 28, 0, 16)
        layout.addView(button, buttonParams)

        val note = makeText(
            "SECURED BY INFINITY",
            12f,
            gold
        )
        layout.addView(
            note,
            LinearLayout.LayoutParams(-1, 48)
        )

        setContentView(layout)
    }
}
