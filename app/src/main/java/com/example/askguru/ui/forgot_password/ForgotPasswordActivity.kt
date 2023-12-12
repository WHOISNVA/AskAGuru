package com.example.askguru.ui.forgot_password

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import com.example.askguru.R
import com.example.askguru.databinding.ActivityForgotPasswordBinding
import com.example.askguru.databinding.ActivityLoginBinding
import com.example.askguru.ui.base.BaseActivity
import com.example.askguru.utils.InsetsWithKeyboardCallback

class ForgotPasswordActivity : BaseActivity(), View.OnClickListener {

    private lateinit var mBinding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        val insetsWithKeyboardCallback = InsetsWithKeyboardCallback(window)
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.llHeardar, insetsWithKeyboardCallback)
        ViewCompat.setWindowInsetsAnimationCallback(mBinding.llHeardar, insetsWithKeyboardCallback)

        setTransparentStatusBar()

        mBinding.btnSend.setOnClickListener(this)
        mBinding.ivBack.setOnClickListener(this)

    }

    override fun onClick(v: View?) {

        when(v?.id){
            R.id.btnSend->{
                if (mBinding.edEmail.text.isNullOrEmpty()) {
                    showSnackbar(mBinding.edEmail, "Please enter email.")
                    return
                }

                if (!isValidEmail(mBinding.edEmail.text.toString().trim())) {
                    showSnackbar(mBinding.edEmail, "Please enter valid email.")
                    return
                }
            }

            R.id.ivBack ->{ onBackPressed() }
        }
    }
}