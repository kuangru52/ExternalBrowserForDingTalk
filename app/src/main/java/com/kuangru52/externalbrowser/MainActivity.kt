package com.kuangru52.externalbrowser

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusTextView = findViewById<TextView>(R.id.tvStatus)
        val btnLaunchDingTalk = findViewById<Button>(R.id.btnLaunchDingTalk)

        statusTextView.text = "模块运行说明：\n本模块基于传统 Xposed API 开发。\n只要你在 LSPosed 管理器中已启用本模块，并且作用域勾选了“钉钉”，模块就会在钉钉后台正常生效拦截网页。"
        statusTextView.setTextColor(Color.parseColor("#1976D2")) // Blue info color

        btnLaunchDinGtalkListeners(btnLaunchDingTalk)
    }

    private fun btnLaunchDinGtalkListeners(btnLaunchDingTalk: Button) {
        btnLaunchDingTalk.setOnClickListener {
            val intent = packageManager.getLaunchIntentForPackage("com.alibaba.android.rimet")
                ?: Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    setPackage("com.alibaba.android.rimet")
                }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "未找到钉钉应用，请确认钉钉已安装", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
