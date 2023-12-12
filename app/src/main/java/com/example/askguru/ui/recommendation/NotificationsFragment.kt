package com.example.askguru.ui.recommendation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.askguru.databinding.FragmentNotificationsBinding
import com.example.askguru.network.ApiHelper
import com.example.askguru.network.RetrofitBuilder
import com.example.askguru.network.Status
import com.example.askguru.network.ViewModelFactory
import com.example.askguru.ui.login.LoginActivity
import com.example.askguru.ui.profile.ProfileUserDataResponse
import com.example.askguru.utils.Const
import com.example.askguru.utils.PreferenceHelper
import com.example.askguru.viewmodel.home.PlayListResponse
import com.example.askguru.viewmodel.home.PlayListResponseItem
import com.example.askguru.viewmodel.home.Recommendation

import com.example.askguru.viewmodel.notification.NotificationsViewModel
import com.google.gson.Gson

class NotificationsFragment : Fragment(),RecommendationListener {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecommendationAdapter

    val recommendationList: ArrayList<Recommendation> = ArrayList()
    val tempList: ArrayList<Recommendation> = ArrayList()

    private lateinit var viewModel: NotificationsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService)))[NotificationsViewModel::class.java]

//        val playlistResponseDataString =  PreferenceHelper.getStringPreference(requireContext(),"playListResponseData")
//
//        val playListResponseModel = Gson().fromJson(playlistResponseDataString, PlayListResponse::class.java)
//
//        val playList: ArrayList<PlayListResponseItem> = ArrayList()
//        playList.addAll(playListResponseModel) //playListModel.liked_playlists as ArrayList<LikedPlaylists>
//
//
//        playList.forEach {
//            if(it.playlist.recommendations.isNotEmpty()){
//                recommendationList.addAll(it.playlist.recommendations)
//            }
//        }
//        Log.e("kp", "recommendationList size ==>> ${recommendationList.size}}")

        getProfileDataApiCall()


//        adapter = RecommendationAdapter(recommendationList, this)
//        _binding?.rvRecommendation!!.layoutManager = LinearLayoutManager(requireActivity())
//        _binding?.rvRecommendation!!.addItemDecoration(
//            DividerItemDecoration(_binding?.rvRecommendation!!.context, (_binding?.rvRecommendation!!.layoutManager as LinearLayoutManager).orientation)
//        )
//        _binding?.rvRecommendation!!.adapter = adapter

        if(PreferenceHelper.getBooleanPreference(requireContext(),Const.PRE_IS_LOGIN,false)){
            binding.llLogin.visibility = View.GONE
        }else
            binding.llLogin.visibility = View.VISIBLE

        binding.tvLogin.setOnClickListener {
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
        }

    }

    private fun getProfileDataApiCall() {
        val token = PreferenceHelper.getStringPreference(requireContext(), Const.PRE_AUTHORIZATION_TOKEN)

        Log.e("kp","token ==> $token")

        viewModel.getProfileData("Bearer $token").observe(requireActivity()) {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.progress.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        binding.progress.visibility = View.GONE
                        setProfileUserData(it.data)


                    }

                    Status.ERROR -> {
                        binding.progress.visibility = View.GONE
                        Toast.makeText(
                            requireActivity(), "Some thing went wrong", Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }

        }
    }

    private fun setProfileUserData(model: ProfileUserDataResponse?) {

        model?.let {
            if(model.playlists.my_playlists.isNotEmpty()){
                model.playlists.my_playlists.forEach {
                    if(it.playlist.recommendations.isNotEmpty()){
                        recommendationList.addAll(it.playlist.recommendations)
                    }
                }
            }
        }

        if(recommendationList.isNotEmpty()){
            val b = recommendationList.distinctBy { it.songTitle } // ["a", "ab", "abc"]
            Log.e("kp","tempList b ==> ${b.size}")
            recommendationList.clear()
            recommendationList.addAll(b)

            binding.tvNoData.visibility = View.GONE

            adapter = RecommendationAdapter(recommendationList, this)
            _binding?.rvRecommendation!!.layoutManager = LinearLayoutManager(requireActivity())
            _binding?.rvRecommendation!!.addItemDecoration(
                DividerItemDecoration(_binding?.rvRecommendation!!.context, (_binding?.rvRecommendation!!.layoutManager as LinearLayoutManager).orientation)
            )
            _binding?.rvRecommendation!!.adapter = adapter

        }else
            binding.tvNoData.visibility = View.VISIBLE




    }


    override fun onClickAccepted(list: Recommendation, position: Int) {
        Log.e("kp","id -- ${list.recommendationId}")
        val token = "Bearer ${PreferenceHelper.getStringPreference(requireContext(), Const.PRE_AUTHORIZATION_TOKEN)}"


        acceptRequestApiCall(token,list.recommendationId,position)
    }

    private fun acceptRequestApiCall(token: String, recommendationId: String, position: Int) {
        viewModel.acceptedRequest(token,recommendationId).observe(requireActivity(), Observer {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.progress.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        binding.progress.visibility = View.GONE
                        recommendationList.removeAt(position)
                        adapter.notifyDataSetChanged()
                        Toast.makeText(requireContext(), "Request Accepted", Toast.LENGTH_LONG).show()
                    }
                    Status.ERROR -> {
                        binding.progress.visibility = View.GONE
                       Toast.makeText(requireContext(), "Something went wrong try again", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    override fun onClickRejected(list: Recommendation, position: Int) {
        Log.e("kp","id -- ${list.recommendationId}")
        val token = "Bearer ${PreferenceHelper.getStringPreference(requireContext(), Const.PRE_AUTHORIZATION_TOKEN)}"
        rejectRequestApiCall(token,list.recommendationId,position)
    }

    private fun rejectRequestApiCall(token: String, recommendationId: String, position: Int) {

        viewModel.rejectRequest(token,recommendationId).observe(requireActivity(), Observer {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.progress.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        binding.progress.visibility = View.GONE
                        recommendationList.removeAt(position)
                        adapter.notifyDataSetChanged()
                        Toast.makeText(requireContext(), "Request Rejected", Toast.LENGTH_LONG).show()
                    }
                    Status.ERROR -> {
                        binding.progress.visibility = View.GONE
                        Toast.makeText(requireContext(), "Something went wrong try again", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    override fun onItemClick(list: Recommendation, position: Int) {

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}