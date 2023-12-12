package com.example.askguru.ui.suggest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.example.askguru.databinding.ActivitySuggestBinding

class SuggestActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivitySuggestBinding

    var isSuggest = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_suggest)
        mBinding = ActivitySuggestBinding.inflate(layoutInflater)
        setContentView(mBinding.root)


        isSuggest = intent.getBooleanExtra("is_suggest",false)

        if(isSuggest) {
            mBinding.tvHeader.text = "Suggest a song"
            mBinding.btnSuggest.text = "Suggest Track (2)"
        }
        else {
            mBinding.tvHeader.text = "Add a song to playlist"
            mBinding.btnSuggest.text = "Add to playlist (2)"
        }


        mBinding.ivCancel.setOnClickListener {
            finish()
        }
    }
}