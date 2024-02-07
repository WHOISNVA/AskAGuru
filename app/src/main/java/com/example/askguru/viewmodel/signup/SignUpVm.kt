package com.example.askguru.viewmodel.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.askguru.network.Resource
import com.example.askguru.repository.MainRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import retrofit2.HttpException
import timber.log.Timber

class SignUpVm (private val mainRepository: MainRepository): ViewModel() {

    fun signUp(email:String,password:String) = liveData(Dispatchers.IO) {

        val request =  SignUpRequest(NewUser(email,password))

        var gson = Gson()
        var jsonString = gson.toJson(request)
        Timber.e("jsonString ==>> $jsonString")

        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.signUp(request)))
        } catch (exception: Exception) {
            if (exception is HttpException) {
                val errorCode = exception.code()
                if (errorCode == 201){
                    emit(Resource.success(data = mainRepository.signUp(request)))
                }else{
                    emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
                }
            }else{
                emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
            }
        }
    }


//    fun guestLogin(device_id: String, appVersion: String, deviceName: String, deviceToken: String, deviceType: String) = liveData(Dispatchers.IO) {
//        emit(Resource.loading(data = null))
//        try {
//            val key = MainApplication.sharedPref.getStringDetail(PREFS_ENCRYPTION_KEY)
//            val requestModel = GuestLoginRequest(device_id, appVersion, deviceName, deviceToken, deviceType, lang)
//            val request = gson.toJson(requestModel)
//
//            if (BuildConfig.DEBUG)
//                Log.e("kp", "guestLogin request --> $request")
//
//            val rncryptor = RNCryptorNative()
//            val aesEncryption = String(rncryptor.encrypt(request, key))
//
//            emit(Resource.success(data = mainRepository.guestLogin(aesEncryption)))
//
//        } catch (exception: Exception) {
//            if (exception is HttpException) {
//                if (BuildConfig.DEBUG)
//                    Log.e("kp", "error code --> ${exception.code()}")
//                val errorCode = exception.code()
//                if (errorCode == UNAUTHORIZED_CODE)
//                    emit(Resource.unauthorized(data = null, message = getDecryptErrorResponse(exception.response()?.errorBody()?.string()!!), errorCode))
//                else
//                    emit(Resource.error(data = null, message = getDecryptErrorResponse(exception.response()?.errorBody()?.string()!!), errorCode))
//            } else
//                emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!", 0))
//        }
//    }
}