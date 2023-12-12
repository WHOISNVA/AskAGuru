package com.example.askguru.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import com.example.askguru.R
import com.example.askguru.databinding.ActivityLoginBinding
import com.example.askguru.network.ApiHelper
import com.example.askguru.network.RetrofitBuilder
import com.example.askguru.network.Status
import com.example.askguru.network.ViewModelFactory
import com.example.askguru.ui.MainActivity
import com.example.askguru.ui.base.BaseActivity
import com.example.askguru.ui.forgot_password.ForgotPasswordActivity
import com.example.askguru.ui.signup.SignUpActivity
import com.example.askguru.utils.Const
import com.example.askguru.utils.Const.Companion.PRE_IS_LOGIN
import com.example.askguru.utils.InsetsWithKeyboardCallback
import com.example.askguru.utils.PreferenceHelper
import com.example.askguru.viewmodel.login.LoginViewModel
import com.google.gson.Gson


class LoginActivity : BaseActivity(), View.OnClickListener {



    private lateinit var mBinding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    var isShowPassword = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_login)
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        //viewModel = ViewModelProviders.of(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(LoginViewModel::class.java)

        viewModel = ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService)))[LoginViewModel::class.java]


        val insetsWithKeyboardCallback = InsetsWithKeyboardCallback(window)
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.llHeardar, insetsWithKeyboardCallback)
        ViewCompat.setWindowInsetsAnimationCallback(mBinding.llHeardar, insetsWithKeyboardCallback)

        setTransparentStatusBar()

        mBinding.tvForgotPassword.setOnClickListener(this)
        mBinding.btnLogin.setOnClickListener(this)
        mBinding.tvSignUp.setOnClickListener(this)
        mBinding.ivEye.setOnClickListener(this)

        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }

    override fun onClick(v: View?) {

        when(v?.id){
            R.id.ivEye ->{
                if(isShowPassword){
                    isShowPassword = false
                    mBinding.edPassword.transformationMethod = PasswordTransformationMethod()
                    mBinding.ivEye.setImageDrawable(getDrawable(R.drawable.show))
                }else{
                    isShowPassword =true
                    mBinding.edPassword.setTransformationMethod(null)
                    mBinding.ivEye.setImageDrawable(getDrawable(R.drawable.hide))
                }


            }
            R.id.tvForgotPassword ->{
                val intent = Intent(this, ForgotPasswordActivity::class.java)
                startActivity(intent)
            }
            R.id.btnLogin ->{
                checkValidation()
            }
            R.id.tvSignUp ->{
                val intent = Intent(this, SignUpActivity::class.java)
                startActivity(intent)
                //finish()
            }
        }
    }

    private fun checkValidation() {

        if (mBinding.edEmail.text.isNullOrEmpty()) {
            showSnackbar(mBinding.edEmail, "Please enter email.")
            return
        }

        if (!isValidEmail(mBinding.edEmail.text.toString().trim())) {
            showSnackbar(mBinding.edEmail, "Please enter valid email.")
            return
        }

        if (mBinding.edPassword.text.isNullOrEmpty()) {
            showSnackbar(mBinding.edEmail, "Please enter password.")
            return
        }

        if (mBinding.edPassword.text!!.length < 6) {
            showSnackbar(mBinding.edPassword, "Please enter valid password.")
            return
        }


        if (isNetworkAvailable(this)){
            loginApiCall();
        }


    }

    private fun loginApiCall() {

        var email =  mBinding.edEmail.text.toString().trim() //use1234@example.com
        var password = mBinding.edPassword.text.toString().trim() //"strings"
        var grant_type = ""
        var scope = ""
        var client_id = ""
        var client_secret = ""

        viewModel.getLogin(email,password,grant_type,scope,client_id,client_secret).observe(this) {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        showCustomDialog()
                    }
                    Status.SUCCESS -> {
                        cancelCustomDialog()
                       // Toast.makeText(this@LoginActivity, "success", Toast.LENGTH_LONG).show()

                        getUserdataApiCall(it.data?.access_token!!)
                        PreferenceHelper.setStringPreference(this, Const.PRE_AUTHORIZATION_TOKEN,it.data?.access_token!!)
//                        PreferenceHelper.setBooleanPreference(this,PRE_IS_LOGIN,true)
//                        val intent = Intent(this, MainActivity::class.java)
//                        startActivity(intent)
//                        finishAffinity()

                    }
                    Status.ERROR -> {
                        cancelCustomDialog()
                        Toast.makeText(this@LoginActivity, "Something went wrong try again", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }


    }

    private fun getUserdataApiCall(accessToken: String) {

        viewModel.getUserData("Bearer $accessToken").observe(this){
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        showCustomDialog()
                    }
                    Status.SUCCESS -> {
                        cancelCustomDialog()
                        Toast.makeText(this@LoginActivity, "success", Toast.LENGTH_LONG).show()

                        val userDataString = Gson().toJson(it.data)
                        PreferenceHelper.setStringPreference(this@LoginActivity, "user_data", userDataString)

                        PreferenceHelper.setStringPreference(this, Const.PRE_USER_ID,it.data?.user_id!!)
                        PreferenceHelper.setBooleanPreference(this,PRE_IS_LOGIN,true)
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finishAffinity()

                    }
                    Status.ERROR -> {
                        cancelCustomDialog()
                        Toast.makeText(this@LoginActivity, "Something went wrong try again", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    }

}