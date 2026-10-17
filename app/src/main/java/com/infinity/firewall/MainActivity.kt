package com.infinity.firewall

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private val black = Color.rgb(8, 11, 16)
    private val gold = Color.rgb(229, 195, 106)
    private val ivory = Color.rgb(245, 239, 220)

    private lateinit var status: TextView
    private lateinit var button: TextView
    private var requested = false

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

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 32, 28, 32)
            setBackgroundColor(black)
        }

        layout.addView(
            makeText("∞", 100f, gold),
            LinearLayout.LayoutParams(-1, 150)
        )

        layout.addView(
            makeText("INFINITY", 32f, gold),
            LinearLayout.LayoutParams(-1, 60)
        )

        layout.addView(
            makeText(
                "YOUR INTERNET. YOUR CONTROL.",
                13f,
                ivory
            ),
            LinearLayout.LayoutParams(-1, 44)
        )

        val divider = View(this).apply {
            setBackgroundColor(gold)
        }
        val dividerParams =
            LinearLayout.LayoutParams(-1, 2)
        dividerParams.setMargins(12, 28, 12, 28)
        layout.addView(divider, dividerParams)

        status = makeText(
            "FIREWALL NOT STARTED",
            16f,
            ivory
        )
        layout.addView(
            status,
            LinearLayout.LayoutParams(-1, 60)
        )

        button = makeText(
            "BLOCK ALL INTERNET",
            17f,
            black
        ).apply {
            setBackgroundColor(gold)
            setPadding(12, 12, 12, 12)
            setOnClickListener {
                if (requested) {
                    stopFirewall()
                } else {
                    requestFirewall()
                }
            }
        }

        val buttonParams =
            LinearLayout.LayoutParams(-1, 64)
        buttonParams.setMargins(0, 28, 0, 16)
        layout.addView(button, buttonParams)

        layout.addView(
            makeText("SECURED BY INFINITY", 12f, gold),
            LinearLayout.LayoutParams(-1, 48)
        )

        setContentView(layout)
    }

    private fun requestFirewall() {
        val permission = VpnService.prepare(this)

        if (permission != null) {
            startActivityForResult(permission, VPN_REQUEST)
        } else {
            startFirewall()
        }
    }

    @Deprecated("Uses Android's compatible activity result API")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == VPN_REQUEST) {
            if (resultCode == RESULT_OK) {
                startFirewall()
            } else {
                status.text = "VPN PERMISSION DENIED"
            }
        }
    }

    private fun startFirewall() {
        val intent = Intent(this, FirewallService::class.java)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }

            requested = true
            status.text = "FIREWALL START REQUESTED"
            button.text = "STOP FIREWALL"
        } catch (_: Exception) {
            requested = false
            status.text = "FIREWALL FAILED TO START"
            button.text = "BLOCK ALL INTERNET"
        }
    }

    private fun stopFirewall() {
        stopService(
            Intent(this, FirewallService::class.java).apply {
                action = FirewallService.ACTION_STOP
            }
        )

        requested = false
        status.text = "FIREWALL STOPPED"
        button.text = "BLOCK ALL INTERNET"
    }

    companion object {
        private const val VPN_REQUEST = 100
    }
}
