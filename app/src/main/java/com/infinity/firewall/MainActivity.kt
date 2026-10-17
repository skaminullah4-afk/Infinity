package com.infinity.firewall

import android.app.Activity
import android.widget.FrameLayout
import android.widget.ImageView
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView
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
        window.decorView.systemUiVisibility = 0


        val backgroundImage = ImageView(this).apply {
    setImageResource(R.drawable.firewall_background)
    scaleType = ImageView.ScaleType.CENTER_CROP
    contentDescription = null
}

val content = LinearLayout(this).apply {
    orientation = LinearLayout.VERTICAL
    gravity = Gravity.CENTER
    setPadding(24, 16, 24, 16)
    setBackgroundColor(Color.TRANSPARENT)
}

content.addView(
    makeText("∞", 76f, gold),
    LinearLayout.LayoutParams(-1, 100)
)

content.addView(
    makeText("INFINITY", 42f, gold),
    LinearLayout.LayoutParams(-1, 64)
)

content.addView(
    makeText("YOUR INTERNET. YOUR CONTROL.", 12f, ivory),
    LinearLayout.LayoutParams(-1, 36)
)

val divider = View(this).apply {
    setBackgroundColor(gold)
}
val dividerParams = LinearLayout.LayoutParams(-1, 2)
dividerParams.setMargins(12, 16, 12, 16)
content.addView(divider, dividerParams)

status = makeText("FIREWALL NOT STARTED", 14f, ivory)
content.addView(status, LinearLayout.LayoutParams(-1, 48))

button = makeText("BLOCK ALL INTERNET", 16f, black).apply {
    val shape = android.graphics.drawable.GradientDrawable()
    shape.setColor(gold)
    shape.cornerRadius = 14f
    background = shape
    setPadding(12, 8, 12, 8)
    isClickable = true
    isFocusable = true
    setOnClickListener {
        if (requested) stopFirewall() else requestFirewall()
    }
}

val buttonParams = LinearLayout.LayoutParams(-1, 58)
buttonParams.setMargins(0, 16, 0, 12)
content.addView(button, buttonParams)

content.addView(
    makeText("SECURED BY INFINITY", 11f, gold),
    LinearLayout.LayoutParams(-1, 36)
)

val scroll = ScrollView(this).apply {
    isFillViewport = true
    setBackgroundColor(Color.TRANSPARENT)
    addView(content, LinearLayout.LayoutParams(-1, -2))
}

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
