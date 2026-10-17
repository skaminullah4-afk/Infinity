package com.infinity.firewall

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    private val black = Color.rgb(8, 11, 16)
    private val gold = Color.rgb(229, 195, 106)
    private val ivory = Color.rgb(255, 248, 230)

    private lateinit var status: TextView
    private lateinit var button: TextView
    private var requested = false

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density + 0.5f).toInt()

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
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            includeFontPadding = true
        }
    }

    private fun roundedBackground(
        color: Int,
        radiusDp: Int,
        strokeColor: Int = Color.TRANSPARENT,
        strokeDp: Int = 0
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radiusDp).toFloat()
            if (strokeDp > 0) {
                setStroke(dp(strokeDp), strokeColor)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(5, 5, 12)
        window.navigationBarColor = Color.rgb(5, 5, 12)

        // Full-screen anime background
        val backgroundImage = ImageView(this).apply {
            setImageResource(R.drawable.firewall_background)
            scaleType = ImageView.ScaleType.CENTER_CROP
            contentDescription = null
        }

        // Scrollable layout keeps the controls usable on smaller screens
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            setBackgroundColor(Color.TRANSPARENT)
        }

        val centerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(24), dp(20), dp(24))
            setBackgroundColor(Color.TRANSPARENT)
        }

        // Dark translucent panel makes all text readable
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(20), dp(18), dp(20))
            background = roundedBackground(
                Color.argb(218, 5, 7, 14),
                22,
                gold,
                1
            )
        }

        // Infinity symbol
        val infinity = makeText("∞", 76f, gold).apply {
            includeFontPadding = false
        }
        panel.addView(
            infinity,
            LinearLayout.LayoutParams(-1, dp(88))
        )

        // App heading
        val heading = makeText("INFINITY", 38f, gold).apply {
            letterSpacing = 0.12f
            maxLines = 1
            includeFontPadding = false
        }
        panel.addView(
            heading,
            LinearLayout.LayoutParams(-1, dp(58))
        )

        // Tagline
        val tagline = makeText(
            "YOUR INTERNET.\nYOUR CONTROL.",
            13f,
            ivory
        )
        tagline.letterSpacing = 0.04f
        val taglineParams = LinearLayout.LayoutParams(-1, dp(52))
        taglineParams.topMargin = dp(4)
        panel.addView(tagline, taglineParams)

        // Gold divider
        val divider = View(this).apply {
            setBackgroundColor(gold)
        }
        val dividerParams = LinearLayout.LayoutParams(-1, dp(2))
        dividerParams.setMargins(dp(4), dp(14), dp(4), dp(14))
        panel.addView(divider, dividerParams)

        // Firewall status
        status = makeText("FIREWALL NOT STARTED", 14f, ivory)
        status.maxLines = 2
        panel.addView(
            status,
            LinearLayout.LayoutParams(-1, dp(46))
        )

        // Large, rounded action button
        button = makeText("BLOCK ALL INTERNET", 16f, black).apply {
            background = roundedBackground(gold, 14)
            setPadding(dp(8), dp(10), dp(8), dp(10))
            isClickable = true
            isFocusable = true
            elevation = dp(3).toFloat()
            setOnClickListener {
                if (requested) {
                    stopFirewall()
                } else {
                    requestFirewall()
                }
            }
        }

        val buttonParams = LinearLayout.LayoutParams(-1, dp(58))
        buttonParams.setMargins(0, dp(14), 0, dp(12))
        panel.addView(button, buttonParams)

        // Footer
        val footer = makeText("SECURED BY INFINITY", 11f, gold)
        footer.letterSpacing = 0.08f
        panel.addView(
            footer,
            LinearLayout.LayoutParams(-1, dp(32))
        )

        centerLayout.addView(
            panel,
            LinearLayout.LayoutParams(-1, -2)
        )

        scroll.addView(
            centerLayout,
            android.view.ViewGroup.LayoutParams(-1, -1)
        )

        val root = FrameLayout(this)
        root.addView(
            backgroundImage,
            FrameLayout.LayoutParams(-1, -1)
        )
        root.addView(
            scroll,
            FrameLayout.LayoutParams(-1, -1)
        )

        setContentView(root)
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
        val intent = Intent(this, FirewallService::class.java).apply {
            action = FirewallService.ACTION_STOP
        }

        startService(intent)

        requested = false
        status.text = "STOP REQUESTED"
        button.text = "BLOCK ALL INTERNET"
    }

    companion object {
        private const val VPN_REQUEST = 100
    }
}
