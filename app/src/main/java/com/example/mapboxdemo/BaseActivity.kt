package com.example.mapboxdemo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_base.*

class BaseActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        btnCurrentLocation.setOnClickListener(this)
        btnRoute.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (view?.id == R.id.btnCurrentLocation) {
            var intent= Intent(this@BaseActivity,CurrenLocationActivity::class.java)
            startActivity(intent)
        } else if (view?.id == R.id.btnRoute) {
            var intent= Intent(this@BaseActivity,MovingIconWithTrailingLineActivity::class.java)
            startActivity(intent)
        }
    }
}