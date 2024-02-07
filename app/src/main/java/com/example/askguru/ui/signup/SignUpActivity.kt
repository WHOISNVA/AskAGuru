package com.example.askguru.ui.signup

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.askguru.R
import com.example.askguru.databinding.ActivitySignUpBinding
import com.example.askguru.network.ApiHelper
import com.example.askguru.network.RetrofitBuilder
import com.example.askguru.network.Status
import com.example.askguru.network.ViewModelFactory
import com.example.askguru.ui.MainActivity
import com.example.askguru.ui.base.BaseActivity
import com.example.askguru.utils.Const
import com.example.askguru.utils.InsetsWithKeyboardCallback
import com.example.askguru.utils.PreferenceHelper
import com.example.askguru.utils.SpotifyHelper
import com.example.askguru.viewmodel.signup.SignUpVm
import com.example.askguru.viewmodel.signup.SignupResponse
import com.google.gson.Gson
import timber.log.Timber

class SignUpActivity : BaseActivity(), View.OnClickListener {

    private lateinit var mBinding: ActivitySignUpBinding

    private lateinit var viewModel: SignUpVm
    var isShowPassword = false
    var isShowConfirmPassword = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_sign_up)
        mBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(mBinding.root)


        viewModel = ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService)))[SignUpVm::class.java]

        val insetsWithKeyboardCallback = InsetsWithKeyboardCallback(window)
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.llHeardar, insetsWithKeyboardCallback)
        ViewCompat.setWindowInsetsAnimationCallback(mBinding.llHeardar, insetsWithKeyboardCallback)


        setTransparentStatusBar()

        mBinding.tvLogin.setOnClickListener(this)
        mBinding.btnSignUp.setOnClickListener(this)

        mBinding.ivEyePassword.setOnClickListener(this)

        mBinding.ivEyeConfirmPassword.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        when(v?.id){

            R.id.tvLogin ->{ finish() }

            R.id.btnSignUp ->{ checkValidation() }

            R.id.ivEyePassword ->{
                if(isShowPassword){
                    isShowPassword = false
                    mBinding.edPassword.transformationMethod = PasswordTransformationMethod()
                    mBinding.ivEyePassword.setImageDrawable(getDrawable(R.drawable.show))
                }else{
                    isShowPassword =true
                    mBinding.edPassword.setTransformationMethod(null)
                    mBinding.ivEyePassword.setImageDrawable(getDrawable(R.drawable.hide))
                }
            }
            R.id.ivEyeConfirmPassword ->{
                if(isShowConfirmPassword){
                    isShowConfirmPassword = false
                    mBinding.edConfirmPassword.transformationMethod = PasswordTransformationMethod()
                    mBinding.ivEyeConfirmPassword.setImageDrawable(getDrawable(R.drawable.show))
                }else{
                    isShowConfirmPassword =true
                    mBinding.edConfirmPassword.setTransformationMethod(null)
                    mBinding.ivEyeConfirmPassword.setImageDrawable(getDrawable(R.drawable.hide))
                }
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

        if (mBinding.edPassword.text!!.length < 8) {
            showSnackbar(mBinding.edPassword, "Password should be at least 8 digits")
            return
        }

        if (mBinding.edPassword.text!! == mBinding.edConfirmPassword.text!! ) {
            showSnackbar(mBinding.edPassword, "Password and confirm password not match.")
            return
        }



        if (isNetworkAvailable(this)){

            var email = mBinding.edEmail.text.toString().trim()
            var password = mBinding.edPassword.text.toString().trim()

            viewModel.signUp(email,password).observe(this, Observer {

                it.let { resource ->
                    when (resource.status) {
                        Status.LOADING -> {
                            showCustomDialog()
                        }
                        Status.SUCCESS -> {
                            cancelCustomDialog()
                            Toast.makeText(this@SignUpActivity, "success", Toast.LENGTH_LONG).show()

                            PreferenceHelper.setStringPreference(this,Const.PRE_AUTHORIZATION_TOKEN,it.data?.access_token?.access_token!!)
                            PreferenceHelper.setBooleanPreference(this, Const.PRE_IS_LOGIN,true)

                            val gson = Gson()
                            val responseString = gson.toJson(it.data)
                            PreferenceHelper.setStringPreference(this, Const.PRE_USER_DATA,responseString)
                            PreferenceHelper.setStringPreference(this, Const.PRE_USER_ID,it.data.user_id)

                            //Timber.e("user data ==>> $responseString")

                            //val model = gson.fromJson("string", SignupResponse::class.java)

                            SpotifyHelper.getInstance(this)?.let {
                                it.logout()
                            }
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finishAffinity()

                        }
                        Status.ERROR -> {
                            cancelCustomDialog()
                            Toast.makeText(this@SignUpActivity, "Something went wrong try again", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            })


        }


    }

}

//{
//    "username": "test789",
//    "email": "test789@gmail.com",
//    "email_verified": false,
//    "profile_pic": "/static/images/2AI15PXP.png",
//    "biography": null,
//    "is_active": true,
//    "is_trending": false,
//    "total_listens": 0,
//    "accepted_recs_count": null,
//    "created_on": "2023-05-20T14:04:17.781195",
//    "updated_on": "2023-05-20T14:04:17.781195",
//    "user_id": "c31dd3ab-986f-4c06-a0d9-7716e96e75d8",
//    "access_token": {
//    "access_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0Nzg5QGdtYWlsLmNvbSIsInVzZXJuYW1lIjoidGVzdDc4OSIsImlzcyI6ImFza2FndXJ1LmNvbSIsImF1ZCI6ImFza2FndXJ1OmF1dGgiLCJpYXQiOjE2ODQ1OTE0NTcuNzg1MTU5LCJleHAiOjE2ODQ2NTE0NTcuNzg1MTYyfQ.ADrQt4K2qCJ7622QUafCEwae2N9qKXQv8H905DaJC7g",
//    "token_type": "bearer"
//},
//    "follower_data": null
//}