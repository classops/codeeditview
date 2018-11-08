package com.etoonet.demo.smsedittextdemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.hanter.android.codeeditview.CodeEditView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

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
                Toast.makeText(this@MainActivity, code, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initViews() {

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnDividerWidth -> {
                civTest.codeDividerWidth = 50
            }

            R.id.btnLength -> {
                civTest.codeLength = 6
            }
        }
    }
}
