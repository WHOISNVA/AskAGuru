package com.example.askguru.ui.ranking

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.askguru.R
import com.example.askguru.databinding.FragmentProfileBinding
import com.example.askguru.databinding.FragmentRankingBinding
import com.example.askguru.network.ApiHelper
import com.example.askguru.network.RetrofitBuilder
import com.example.askguru.network.Status
import com.example.askguru.network.ViewModelFactory
import com.example.askguru.ui.home.HomeAdapter
import com.example.askguru.utils.Const
import com.example.askguru.utils.PreferenceHelper
import com.example.askguru.viewmodel.home.HomeVm
import com.example.askguru.viewmodel.home.PlayListResponseItem
import com.example.askguru.viewmodel.ranking.*


class RankingFragment : Fragment(),RankProfileClickListener {

    private var _binding: FragmentRankingBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: RankingVm

    private lateinit var adapter: RankAdapter
    val rankList: ArrayList<RankListResponseItem> = ArrayList()

    private lateinit var adapterRecommended: RankAdapterRecommended
    val rankRecommendedList: ArrayList<RankingByRecommendedItem> = ArrayList()


    private lateinit var adapterFollower: RankAdapterFollower
    val rankFollowerList: ArrayList<RankingByFollowerCountResponseItem> = ArrayList()


    var isMostListensSelected = true
    var isFollowersSelected = false
    var isRecommendedSelected = false

    var firstUserId =""
    var secondUserId =""
    var thirdUserId =""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRankingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService)))[RankingVm::class.java]

        setListAdapter()

        if (isMostListensSelected)
            getRankListApiCall()



        binding.tvMostListens.setOnClickListener {
            isMostListensSelected = true
            isFollowersSelected = false
            isRecommendedSelected = false


            binding.tvMostListens.background =
                (requireActivity().resources.getDrawable(R.drawable.orange_bg))
            binding.tvFollowers.background =
                (requireActivity().resources.getDrawable(R.drawable.orange_stroke))
            binding.tvRecommended.background =
                (requireActivity().resources.getDrawable(R.drawable.orange_stroke))



            getRankListApiCall()
        }
        binding.tvFollowers.setOnClickListener {
            isMostListensSelected = false
            isFollowersSelected = true
            isRecommendedSelected = false

            binding.tvFollowers.background =
                (requireActivity().resources.getDrawable(R.drawable.orange_bg))
            binding.tvMostListens.background =
                (requireActivity().resources.getDrawable(R.drawable.orange_stroke))
            binding.tvRecommended.background =
                (requireActivity().resources.getDrawable(R.drawable.orange_stroke))

            setFollowerListAdapter()
            getRankFollowerListApiCall()
        }
        binding.tvRecommended.setOnClickListener {
            isMostListensSelected = false
            isFollowersSelected = false
            isRecommendedSelected = true

            binding.tvRecommended.background =
                (requireActivity().resources.getDrawable(R.drawable.orange_bg))
            binding.tvFollowers.background =
                (requireActivity().resources.getDrawable(R.drawable.orange_stroke))
            binding.tvMostListens.background =
                (requireActivity().resources.getDrawable(R.drawable.orange_stroke))

            setRecommendedListAdapter()
            getRankRecommendedListApiCall()

        }

        PreferenceHelper.setBooleanPreference(requireContext(),"is_fromRanking_click",false)

    }


    /**** Get ranking List Api call */
    private fun setListAdapter() {
        adapter = RankAdapter(rankList,this)
        _binding?.rvRankList!!.layoutManager = LinearLayoutManager(requireActivity())
        _binding?.rvRankList!!.adapter = adapter
    }

    private fun getRankListApiCall() {
        viewModel.getRankList().observe(requireActivity(), Observer {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {

                        _binding?.frame!!.visibility = View.GONE
                        _binding?.rvRankList!!.visibility = View.GONE

                        _binding?.shimmer!!.startShimmer()
                        _binding?.shimmer!!.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        _binding?.shimmer!!.visibility = View.GONE
                        if (it.data?.isNotEmpty() == true) {
                            setListData(it.data)
                        }

                        _binding?.frame!!.visibility = View.VISIBLE
                        _binding?.rvRankList!!.visibility = View.VISIBLE
                    }
                    Status.ERROR -> {
                        _binding?.shimmer!!.stopShimmer()
                        _binding?.shimmer!!.visibility = View.GONE

                        _binding?.frame!!.visibility = View.VISIBLE
                        _binding?.rvRankList!!.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    private fun setListData(list: RankListResponse) {

        adapter.cleatList()

        //var tempList = list;

        for (i in 0..2) {

            when (i) {
                0 -> {
                    _binding?.tvFirstName!!.text = list[i].username
                    list[i].total_listens.let {
                        _binding?.tvFirstListenCount!!.text = list[i].total_listens.toString()
                    }
                    _binding?.ivFirst.let {
                        Glide.with(_binding?.ivFirst!!.context)
                            .load(Const.IMAGE_BASE_URL + list[i].profile_pic)
                            .placeholder(R.drawable.ic_profile)
                            .into(_binding?.ivFirst!!)
                    }

                    firstUserId = list[i].user_id

                }
                1 -> {
                    _binding?.tvSecondName!!.text = list[i].username
                    list[i].total_listens.let {
                        _binding?.tvSecondListenCount!!.text = list[i].total_listens.toString()
                    }
                    _binding?.ivSecond.let {
                        Glide.with(_binding?.ivSecond!!.context)
                            .load(Const.IMAGE_BASE_URL + list[i].profile_pic)
                            .placeholder(R.drawable.ic_profile)
                            .into(_binding?.ivSecond!!)
                    }
                    secondUserId = list[i].user_id

                }
                2 -> {
                    _binding?.tvThirdName!!.text = list[i].username
                    list[i].total_listens.let {
                        _binding?.tvThirdListenCount!!.text = list[i].total_listens.toString()
                    }
                    _binding?.ivThird.let {
                        Glide.with(_binding?.ivThird!!.context)
                            .load(Const.IMAGE_BASE_URL + list[i].profile_pic)
                            .placeholder(R.drawable.ic_profile)
                            .into(_binding?.ivThird!!)
                    }
                    thirdUserId = list[i].user_id
                }
            }
        }


        list.subList(0, 3).clear()

//        list.removeAt(0)
//        list.removeAt(1)
//        list.removeAt(2)
        //adapter.addList(list)

        rankList.addAll(list)

        adapter = RankAdapter(rankList,this)
        _binding?.rvRankList!!.layoutManager = LinearLayoutManager(requireActivity())
        _binding?.rvRankList!!.adapter = adapter


        binding.llFirst.setOnClickListener {
            PreferenceHelper.setBooleanPreference(requireContext(),"is_fromRanking",true)
            PreferenceHelper.setStringPreference(requireContext(),"rank_user_id",firstUserId)



            val bundle = Bundle()
            bundle.putString("user_id", firstUserId)
            bundle.putBoolean("is_ranking",true)
            findNavController().navigate(R.id.navigation_profile_data,bundle)
        }
        binding.llSecond.setOnClickListener {
            PreferenceHelper.setBooleanPreference(requireContext(),"is_fromRanking",true)
            PreferenceHelper.setStringPreference(requireContext(),"rank_user_id",secondUserId)


            val bundle = Bundle()
            bundle.putString("user_id", secondUserId)
            bundle.putBoolean("is_ranking",true)
            findNavController().navigate(R.id.navigation_profile_data,bundle)

        }
        binding.llThird.setOnClickListener {
            PreferenceHelper.setBooleanPreference(requireContext(),"is_fromRanking",true)
            PreferenceHelper.setStringPreference(requireContext(),"rank_user_id",thirdUserId)



            val bundle = Bundle()
            bundle.putString("user_id", thirdUserId)
            bundle.putBoolean("is_ranking",true)
            findNavController().navigate(R.id.navigation_profile_data,bundle)



        }

    }



    /*** get Recommended List Api call */

    private fun setRecommendedListAdapter() {
        adapterRecommended = RankAdapterRecommended(rankRecommendedList,this)
        _binding?.rvRankList!!.layoutManager = LinearLayoutManager(requireActivity())
        _binding?.rvRankList!!.adapter = adapterRecommended
    }
    private fun getRankRecommendedListApiCall() {
        viewModel.getRankByRecommendedList().observe(requireActivity(), Observer {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {

                        _binding?.frame!!.visibility = View.GONE
                        _binding?.rvRankList!!.visibility = View.GONE

                        _binding?.shimmer!!.startShimmer()
                        _binding?.shimmer!!.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        _binding?.shimmer!!.visibility = View.GONE
                        if (it.data?.isNotEmpty() == true) {
                            setRecommendedListData(it.data)
                        }

                        _binding?.frame!!.visibility = View.VISIBLE
                        _binding?.rvRankList!!.visibility = View.VISIBLE
                    }
                    Status.ERROR -> {
                        _binding?.shimmer!!.stopShimmer()
                        _binding?.shimmer!!.visibility = View.GONE

                        _binding?.frame!!.visibility = View.VISIBLE
                        _binding?.rvRankList!!.visibility = View.VISIBLE
                    }
                }
            }
        })

    }
    private fun setRecommendedListData(list: RankingByRecommendedResponse) {

        for (i in 0..2) {

            when (i) {
                0 -> {
                    _binding?.tvFirstName!!.text = list[i].user.username
                    list[i].accepted_recs_count.let {
                        _binding?.tvFirstListenCount!!.text = list[i].accepted_recs_count.toString()
                    }
                    _binding?.ivFirst.let {
                        Glide.with(_binding?.ivFirst!!.context)
                            .load(Const.IMAGE_BASE_URL + list[i].user.profile_pic)
                            .placeholder(R.drawable.ic_profile)
                            .into(_binding?.ivFirst!!)
                    }

                }
                1 -> {
                    _binding?.tvSecondName!!.text = list[i].user.username
                    list[i].accepted_recs_count.let {
                        _binding?.tvSecondListenCount!!.text = list[i].accepted_recs_count.toString()
                    }
                    _binding?.ivSecond.let {
                        Glide.with(_binding?.ivSecond!!.context)
                            .load(Const.IMAGE_BASE_URL + list[i].user.profile_pic)
                            .placeholder(R.drawable.ic_profile)
                            .into(_binding?.ivSecond!!)
                    }

                }
                2 -> {
                    _binding?.tvThirdName!!.text = list[i].user.username
                    list[i].accepted_recs_count.let {
                        _binding?.tvThirdListenCount!!.text = list[i].accepted_recs_count.toString()
                    }
                    _binding?.ivThird.let {
                        Glide.with(_binding?.ivThird!!.context)
                            .load(Const.IMAGE_BASE_URL + list[i].user.profile_pic)
                            .placeholder(R.drawable.ic_profile)
                            .into(_binding?.ivThird!!)
                    }
                }
            }


        }

//        list.removeAt(0)
//        list.removeAt(1)
//        list.removeAt(2)

        adapterRecommended.addList(list)

    }



    /*** Get Follower list *****/
    private fun setFollowerListAdapter() {
        adapterFollower = RankAdapterFollower(rankFollowerList)
        _binding?.rvRankList!!.layoutManager = LinearLayoutManager(requireActivity())
        _binding?.rvRankList!!.adapter = adapterFollower

    }

    private fun getRankFollowerListApiCall() {
        viewModel.getRankByFollowerList().observe(requireActivity(), Observer {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {

                        _binding?.frame!!.visibility = View.GONE
                        _binding?.rvRankList!!.visibility = View.GONE

                        _binding?.shimmer!!.startShimmer()
                        _binding?.shimmer!!.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        _binding?.shimmer!!.visibility = View.GONE
                        if (it.data?.isNotEmpty() == true) {
                            setFollowerListData(it.data)
                        }

                        _binding?.frame!!.visibility = View.VISIBLE
                        _binding?.rvRankList!!.visibility = View.VISIBLE
                    }
                    Status.ERROR -> {
                        _binding?.shimmer!!.stopShimmer()
                        _binding?.shimmer!!.visibility = View.GONE

                        _binding?.frame!!.visibility = View.VISIBLE
                        _binding?.rvRankList!!.visibility = View.VISIBLE
                    }
                }
            }
        })

    }

    private fun setFollowerListData(list: RankingByFollowerCountResponse) {

        for (i in 0..list.size) {

            when (i) {
                0 -> {
                    _binding?.tvFirstName!!.text = list[i].username


                    list[i].follower_data.follower_count.let {
                        _binding?.tvFirstListenCount!!.text = list[i].follower_data.follower_count.toString()
                    }
                    _binding?.ivFirst.let {
                        Glide.with(_binding?.ivFirst!!.context)
                            .load(Const.IMAGE_BASE_URL + list[i].profile_pic)
                            .placeholder(R.drawable.ic_profile)
                            .into(_binding?.ivFirst!!)
                    }

                }
                1 -> {

                    _binding?.tvSecondName!!.text = list[i].username
                    list[i].follower_data.follower_count.let {
                        _binding?.tvSecondListenCount!!.text = list[i].follower_data.follower_count.toString()
                    }
                    _binding?.ivSecond.let {
                        Glide.with(_binding?.ivSecond!!.context)
                            .load(Const.IMAGE_BASE_URL + list[i].profile_pic)
                            .placeholder(R.drawable.ic_profile)
                            .into(_binding?.ivSecond!!)
                    }

                }
                2 -> {

                    _binding?.tvThirdName!!.text = list[i].username
                    list[i].follower_data.follower_count.let {
                        _binding?.tvThirdListenCount!!.text = list[i].follower_data.follower_count.toString()
                    }
                    _binding?.ivThird.let {
                        Glide.with(_binding?.ivThird!!.context)
                            .load(Const.IMAGE_BASE_URL + list[i].profile_pic)
                            .placeholder(R.drawable.ic_profile)
                            .into(_binding?.ivThird!!)
                    }
                }
            }

            if(i>2){

            }
        }
//        list.removeAt(0)
//        list.removeAt(1)
//        list.removeAt(2)

        adapterFollower.addList(list)
    }

    override fun onRankProfileClick(userId: String) {
        Log.e("kp","userId ==> $userId")

        PreferenceHelper.setBooleanPreference(requireContext(),"is_fromRanking",true)
        PreferenceHelper.setStringPreference(requireContext(),"rank_user_id",userId)



        val bundle = Bundle()
        bundle.putString("user_id", userId)
        bundle.putBoolean("is_ranking",true)

        findNavController().navigate(R.id.navigation_profile_data,bundle)
    }


}