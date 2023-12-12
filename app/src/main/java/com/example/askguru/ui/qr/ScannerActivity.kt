package com.example.askguru.ui.qr

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
//import com.apple.android.music.playback.controller.MediaPlayerControllerFactory
//import com.apple.android.music.playback.model.MediaContainerType
//import com.apple.android.music.playback.queue.CatalogPlaybackQueueItemProvider
//import com.apple.android.sdk.authentication.AuthenticationFactory
//import com.apple.android.sdk.authentication.TokenProvider
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.example.askguru.R
import com.example.askguru.databinding.ActivityScannerBinding
import com.example.askguru.network.ApiHelper
import com.example.askguru.network.RetrofitBuilder
import com.example.askguru.network.Status
import com.example.askguru.network.ViewModelFactory
import com.example.askguru.ui.base.BaseActivity
import com.example.askguru.viewmodel.home.HomeVm

class ScannerActivity : BaseActivity() {

    private lateinit var binding: ActivityScannerBinding

    private lateinit var codeScanner: CodeScanner

    private lateinit var viewModel: HomeVm

    val recommendationList: ArrayList<RecommendationData> = ArrayList()

    private lateinit var adapter: RecommendationDataAdapter

//    private var authenticationManager = AuthenticationFactory.createAuthenticationManager(this)
//    val REQUEST_CODE_APPLE_MUSIC_AUTH = 101

    var _playListId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_scanner)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()

        viewModel = ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService)))[HomeVm::class.java]
    }

    private fun initData() {

        codeScanner = CodeScanner(this, binding.scannerView)

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_LONG).show()

                openWebPage(it.text)
               // getPlayListApiCall(it.text)
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }

        binding.scannerView.setOnClickListener {
            codeScanner.startPreview()
        }


    }

    private fun openWebPage(playListId: String?) {


        Log.e("kp","playlist Id  ==> $playListId")

        _playListId = playListId!!

        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse("https://askaguru.com/playlist/${playListId}")
        startActivity(openURL)

        //signIn()

        //https://github.com/assembleinc/kids-tunes-android

    }


    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun getPlayListApiCall(playListId: String?) {

        if(playListId!!.isEmpty())
            return

        binding.scannerView.visibility = View.GONE


        // 8187803e-55b5-4eb4-9c1b-31459fa2cdba
        viewModel.getPlayListById(playListId).observe(this, Observer {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        showCustomDialog()
                    }
                    Status.SUCCESS -> {
                        cancelCustomDialog()
                        binding.rvList.visibility = View.VISIBLE
                        binding.btnAdd.visibility = View.VISIBLE

                        setListData(it.data)
                        Toast.makeText(this@ScannerActivity, "success", Toast.LENGTH_LONG).show()

                    }
                    Status.ERROR -> {
                        cancelCustomDialog()
                        binding.scannerView.visibility = View.VISIBLE

                        binding.rvList.visibility = View.GONE
                        binding.btnAdd.visibility = View.GONE
                        Toast.makeText(this@ScannerActivity, "Play list not found try again", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })

    }

    private fun setListData(model: PlayListByIdResponse?) {

        model?.let {

            if(model.playlist.recommendations.isNotEmpty()){
                recommendationList.addAll(model.playlist.recommendations)

                adapter = RecommendationDataAdapter(recommendationList)
                binding.rvList.layoutManager = LinearLayoutManager(this)
                binding.rvList.addItemDecoration(
                    DividerItemDecoration(this, (binding.rvList.layoutManager as LinearLayoutManager).orientation)
                )
                binding.rvList.adapter = adapter


            }
        }

    }



//    private fun signIn() {
//        val intent = authenticationManager.createIntentBuilder(getString(R.string.developer_token))
//            .setHideStartScreen(true)
//            .setStartScreenMessage("Connect with apple music!")
//            .build()
//        startActivityForResult(intent, REQUEST_CODE_APPLE_MUSIC_AUTH)
//    }
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (requestCode == REQUEST_CODE_APPLE_MUSIC_AUTH) {
//            val result = authenticationManager.handleTokenResult(data)
//            if (result.isError) {
//                val error = result.error
//                Log.e("kp", "error: $error")
//            }
//            else {
//
//                val tokenProvider = object : TokenProvider {
//                    override fun getDeveloperToken(): String = resources.getString(R.string.developer_token)//token.developerToken
//                    override fun getUserToken(): String = ""//token.userToken
//                }
//                val applePlayerController = MediaPlayerControllerFactory.createLocalController(applicationContext, tokenProvider)
//                val queueProviderBuilder = CatalogPlaybackQueueItemProvider.Builder()
//                queueProviderBuilder.containers(MediaContainerType.PLAYLIST, _playListId)
//
//                //saveToken(result.musicUserToken)
//                //startMainActivity()
//
//                // https://stackoverflow.com/questions/75655406/applemusickit-library-for-android
//                // https://stackoverflow.com/questions/72824625/apple-musickit-sdk-in-android
//            }
//        }
//        else {
//            super.onActivityResult(requestCode, resultCode, data)
//        }
//    }

}