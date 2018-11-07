package com.etoonet.demo.smsedittextdemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.hanter.android.codeeditview.CodeEditView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        civTest.showInputMethod()
        civTest.onCodeCompleteListener = object: CodeEditView.OnCodeCompleteListener {
            override fun onCodeComplete(code: String) {
                Log.e("Test", code)
            }
        }
    }

    private fun initViews() {


    }
}
