package com.example.askguru.ui.profile

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
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.askguru.R
import com.example.askguru.databinding.FragmentProfileBinding
import com.example.askguru.network.ApiHelper
import com.example.askguru.network.RetrofitBuilder
import com.example.askguru.network.Status
import com.example.askguru.network.ViewModelFactory
import com.example.askguru.ui.login.LoginActivity
import com.example.askguru.utils.Const
import com.example.askguru.utils.Const.Companion.PRE_AUTHORIZATION_TOKEN
import com.example.askguru.utils.PreferenceHelper
import com.example.askguru.viewmodel.login.UserResponse
import com.example.askguru.viewmodel.profile.ProfileDataResponse
import com.example.askguru.viewmodel.profile.ProfileVm
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import timber.log.Timber


class ProfileFragment : Fragment {

    //Add empty constructor to avoid the exception
    constructor() : super()

    private val profileArray = arrayOf("All Playlist", "Likes", "Contributions")

    private lateinit var binding: FragmentProfileBinding

    private lateinit var viewModel: ProfileVm

    private var myUserId : String? = null
    private var isFollowing : Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService)))[ProfileVm::class.java]

        myUserId = PreferenceHelper.getStringPreference(requireActivity(), Const.PRE_USER_ID)

        if (PreferenceHelper.getBooleanPreference(requireActivity(), "is_fromRanking", false)) {
            val userId = PreferenceHelper.getStringPreference(requireActivity(), "rank_user_id")



            // used in ProfilePlatListDetailsFragment screen
            PreferenceHelper.setBooleanPreference(requireContext(),"is_show_delete_button",false)

            PreferenceHelper.setBooleanPreference(requireActivity(), "is_fromRanking", false)
            //PreferenceHelper.setStringPreference(requireActivity(), "rank_user_id", "")
            PreferenceHelper.setBooleanPreference(requireContext(),"is_fromRanking_click",true)

            binding.llFollow.visibility = View.VISIBLE

            getRankProfileApiCall(userId)

        } else if (PreferenceHelper.getBooleanPreference(requireContext(), Const.PRE_IS_LOGIN, false)) {
            binding.llLogin.visibility = View.GONE
            binding.llFollow.visibility = View.GONE
            getProfileDataApiCall()

            PreferenceHelper.setBooleanPreference(requireContext(),"is_show_delete_button",true)

        } else {
            binding.llLogin.visibility = View.VISIBLE
            binding.tvLogin.setOnClickListener {
                val intent = Intent(requireActivity(), LoginActivity::class.java)
                startActivity(intent)
            }

        }


        val navController = activity?.findNavController(R.id.nav_host_fragment_activity_main)

        binding.ivSettings.setOnClickListener {
            navController?.navigate(R.id.navigation_settings)
        }

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnFollow.setOnClickListener {
            followApiCall()
        }



    }

    private fun followApiCall() {
        val userId = PreferenceHelper.getStringPreference(requireActivity(), "rank_user_id")
        val token = "Bearer "+PreferenceHelper.getStringPreference(requireActivity(), PRE_AUTHORIZATION_TOKEN)

        if (userId.isNullOrEmpty()) {
            Toast.makeText(requireActivity(), "User Id Not found", Toast.LENGTH_LONG).show()
            return
        }

        viewModel.followUnFollow(token,userId,isFollowing).observe(requireActivity(), Observer {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        isFollowing = !isFollowing
                        binding.progressBar.visibility = View.GONE
                        //binding.llFollow.visibility = View.GONE
                        updateFollowButton()
                    }

                    Status.ERROR -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireActivity(), "Some thing went wrong", Toast.LENGTH_LONG).show()
                    }

                }
            }
        })

    }

    private fun getProfileDataApiCall() {
        val token = PreferenceHelper.getStringPreference(requireContext(), Const.PRE_AUTHORIZATION_TOKEN)

        Log.e("kp","token ==> $token")

        viewModel.getProfileData("Bearer $token").observe(requireActivity()) {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {}
                    Status.SUCCESS -> {
                        val res = Gson().toJson(it)

                        setProfileUserData(it.data)
                    }

                    Status.ERROR -> {
                        Toast.makeText(
                            requireActivity(), "Some thing went wrong", Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }

        }
    }



    private fun getRankProfileApiCall(userId: String) {


        if (userId.isNullOrEmpty()) {
            Toast.makeText(requireActivity(), "User Id Not found", Toast.LENGTH_LONG).show()
            return
        }
        viewModel.getRankingProfileData(userId).observe(requireActivity(), Observer {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.progressBar.visibility = View.GONE
                        val res = Gson().toJson(it)
                        Timber.tag("PSB").d(res)
                        setUserData(it.data)
                    }

                    Status.ERROR -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            requireActivity(), "Some thing went wrong", Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }

        })
    }

    private fun setUserData(userData: ProfileDataResponse?) {

        val likeDataString = Gson().toJson(userData?.playlists)
        PreferenceHelper.setStringPreference(requireContext(), "playlists", likeDataString)

        val userObj = userData?.user!!

        binding.tvEmail.text = userObj.email

        userObj.profile_pic.let {
            Glide.with(binding.ivUserImage.context).load(Const.IMAGE_BASE_URL + userObj.profile_pic)
                .placeholder(R.drawable.ic_profile).into(binding.ivUserImage)
        }

        userObj.total_listens.let {
            binding.tvTotalListensCount.text = userObj.total_listens.toString()
        }

        userObj.follower_data.follower_count.let {
            binding.tvFollowersCount.text = userObj.follower_data.follower_count.toString()
        }

        setViewPagerAdapter()

        userData.user.follower_data.followers?.let {
            isFollowing  = it.any { it.followed_id == myUserId }
        }
        updateFollowButton()
    }

    private fun updateFollowButton(){
        if(isFollowing){
            binding.btnFollow.text = "Unfollow"
        }else{
            binding.btnFollow.text = "Follow"
        }
    }

    private fun setViewPagerAdapter() {

        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        val adapter = ProfileViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 3

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = profileArray[position]
        }.attach()
    }


    private fun setProfileUserData(response: ProfileUserDataResponse?) {

        val likeDataString = Gson().toJson(response?.playlists)
        PreferenceHelper.setStringPreference(requireContext(), "playlists", likeDataString)

        val userDataString =  PreferenceHelper.getStringPreference(requireContext(),"user_data")
        val userModel = Gson().fromJson(userDataString, UserResponse::class.java)

        binding.tvEmail.text = userModel.email

        userModel.profile_pic.let {
            Glide.with(binding.ivUserImage.context).load(Const.IMAGE_BASE_URL + userModel.profile_pic)
                .placeholder(R.drawable.ic_profile).into(binding.ivUserImage)
        }

        userModel.total_listens.let {
            binding.tvTotalListensCount.text = userModel.total_listens.toString()
        }

        userModel.follower_data.follower_count.let {
            binding.tvFollowersCount.text = userModel.follower_data.follower_count.toString()
        }

        setViewPagerAdapter()
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the result to the current tab's fragment
        val currentFragment = childFragmentManager.fragments.firstOrNull()
        currentFragment?.onActivityResult(requestCode, resultCode, data)
    }


}