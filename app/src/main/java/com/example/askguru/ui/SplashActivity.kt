package com.example.askguru.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.askguru.R
import com.example.askguru.ui.base.BaseActivity
import com.example.askguru.ui.login.LoginActivity
import com.example.askguru.utils.Const
import com.example.askguru.utils.PreferenceHelper

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        setFullScreenActivity()

        initData()
    }


    private fun initData() {
        Handler(mainLooper).postDelayed({

            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()

//            if(PreferenceHelper.getBooleanPreference(this, Const.IS_LOGIN,false)){
//                val intent = Intent(this, HomeActivity::class.java)
//                startActivity(intent)
//                finish()
//            }else {
//                val intent = Intent(this, LoginActivity::class.java)
//                startActivity(intent)
//                finish()
//            }

        },2000)

    }

}