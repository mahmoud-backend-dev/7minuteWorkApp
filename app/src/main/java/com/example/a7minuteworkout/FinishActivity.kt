package com.example.a7minuteworkout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_finish.*

class FinishActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish)
        setSupportActionBar(finish_activity_toolBar)
        val action=supportActionBar
        if (action!=null){
            action.setDisplayHomeAsUpEnabled(true)
        }
        finish_activity_toolBar.setNavigationOnClickListener {
            onBackPressed()
        }
        btnFinish.setOnClickListener {
            finish()
        }
    }
}