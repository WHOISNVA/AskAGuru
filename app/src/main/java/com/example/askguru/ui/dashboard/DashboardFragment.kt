package com.example.askguru.ui.dashboard

import android.content.DialogInterface.OnMultiChoiceClickListener
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.askguru.R
import com.example.askguru.databinding.FragmentDashboardBinding
import com.example.askguru.network.ApiHelper
import com.example.askguru.network.RetrofitBuilder
import com.example.askguru.network.Status
import com.example.askguru.network.ViewModelFactory
import com.example.askguru.ui.add_song.SearchAdapter
import com.example.askguru.ui.add_song.SearchSelectClickListener
import com.example.askguru.ui.login.LoginActivity
import com.example.askguru.utils.Const
import com.example.askguru.utils.PreferenceHelper
import com.example.askguru.viewmodel.add_song.SearchList
import com.example.askguru.viewmodel.create_list.CreateListVm
import timber.log.Timber


class DashboardFragment : Fragment(), SearchSelectClickListener {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SearchAdapter
    val searchList: ArrayList<SearchList> = ArrayList()

    //private val playlistModel by lazy { PlaylistDetailsFragmentArgs.fromBundle(requireArguments()).playlistModel }
    private val playlistModel by lazy { DashboardFragmentArgs.fromBundle(requireArguments()).playlistModel }

    var selectedGenre ="";

    var count =0

    var genreList = arrayOf(
        "Afrobeat", "Blues", "Classical","Country","Electronic","Funk",
        "Hip-Hop", "Instrumental", "Jazz", "Kpop", "Latin","Metal",
        "Pop","Punk","Reggae","Rock","Soul","gospel"
    )
    var checkedSites = booleanArrayOf(
        false, false, false, false, false,false,
        false, false, false, false,false, false,
        false, false, false,false, false, false)

    private lateinit var viewModel: CreateListVm

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService)))[CreateListVm::class.java]

        if (PreferenceHelper.getBooleanPreference(requireContext(), Const.PRE_IS_LOGIN, false)) {
            binding.llCreatePlayList.visibility = View.VISIBLE
            binding.llLogin.visibility = View.GONE


        } else {
            binding.llCreatePlayList.visibility = View.GONE
            binding.llLogin.visibility = View.VISIBLE
        }

        binding.tvLogin.setOnClickListener {
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
        }

        val navController = activity?.findNavController(R.id.nav_host_fragment_activity_main)
        binding.tvAddSong.setOnClickListener {
            navController?.navigate(R.id.add_song)
        }


        binding.tvAddGenre.setOnClickListener {
            openDialog( binding.btnAdd)
        }

        binding.btnAdd.setOnClickListener {

            if(selectedGenre.isEmpty()){
                Toast.makeText(requireActivity(),"Select genre",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(playlistModel!=null){
                val pos = PreferenceHelper.getIntegerPreference(requireActivity(),"pos",0)
                val songId = playlistModel?.playlist!!.songId

                callCreateListApi(songId)
            }else{

                if(searchList.isEmpty())
                    return@setOnClickListener

                apiCall(count)


            }

        }

        findNavController().currentBackStackEntry
            ?.savedStateHandle?.let { handle ->
                handle.getLiveData<ArrayList<SearchList>>("list")
                    .observe(viewLifecycleOwner) { res ->
                        //res is the value you passed from FragmentB

                        Timber.tag("kp").e("list size -- ${res.size}")

                        setListData(res)
                    }
            }

        playlistModel?.let {
            val pos = PreferenceHelper.getIntegerPreference(requireActivity(),"pos",0)
            val title =  ""+playlistModel?.playlist!!.recommendations[pos].songTitle
            binding.edtSongTitle.setText(title)

            Glide.with(binding.ivSongThumb.context).load(playlistModel?.playlist?.recommendations?.get(pos)?.artwork).into(binding.ivSongThumb)
        }
    }




    private fun setListData(list: List<SearchList>?) {
        //searchList.clear()
        searchList.addAll(list!!)

        adapter = SearchAdapter(searchList,this)
        _binding?.rvSearchList!!.layoutManager = LinearLayoutManager(requireActivity())
        _binding?.rvSearchList!!.addItemDecoration(
            DividerItemDecoration(_binding?.rvSearchList!!.context, ( _binding?.rvSearchList!!.layoutManager as LinearLayoutManager).orientation)
        )
        _binding?.rvSearchList!!.adapter = adapter

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClickSong(position: Int, list: SearchList) {

    }

    fun openDialog(v: View?) {

        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Select Genre")

        //Building the list to be shown in AlertDialog
        builder.setMultiChoiceItems(genreList, checkedSites, OnMultiChoiceClickListener { dialog, which, isChecked -> // Update the current item's checked status
            Timber.tag("kp").e("genreList ${genreList[which]}")
            binding.tvAddGenre.text = genreList[which]
            selectedGenre = genreList[which]
            dialog.dismiss()
            })
        val dialog = builder.create()
        dialog.show()
    }


    private fun callCreateListApi(songId: String) {

        val token = "Bearer "+PreferenceHelper.getStringPreference(requireActivity(), Const.PRE_AUTHORIZATION_TOKEN)

        viewModel.createPlatList(token,selectedGenre,songId).observe(requireActivity(), Observer {

            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireActivity(), "Create list Successfully", Toast.LENGTH_LONG).show()
                        findNavController().popBackStack()
                    }
                    Status.ERROR -> {
                        Timber.tag("kp").e("ERROR")
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireActivity(), "Something went wrong", Toast.LENGTH_LONG).show()
                    }
                }
            }

        })

    }


    private fun apiCall(pos: Int) {
        Timber.tag("kp").e("searchList.size -- ${searchList.size}")
        Timber.tag("kp").e("pos -- ${pos}")


        if(pos<searchList.size) {
            val songId = searchList.get(pos).id
            apiCallCreateList(songId)
        }else{
            Toast.makeText(requireActivity(), "Create list Successfully", Toast.LENGTH_LONG).show()
            searchList.clear()
            adapter.notifyDataSetChanged()
            selectedGenre =""
            binding.tvAddGenre.text = "Add Genre"

        }


    }

    private fun apiCallCreateList(songId: String) {

        val token = "Bearer "+PreferenceHelper.getStringPreference(requireActivity(), Const.PRE_AUTHORIZATION_TOKEN)

        viewModel.createPlatList(token,selectedGenre,songId).observe(requireActivity(), Observer {

            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireActivity(), "Create list Successfully", Toast.LENGTH_LONG).show()
                        count = count+1
                        apiCall(count)
                    }
                    Status.ERROR -> {
                        Timber.tag("kp").e("ERROR")
                        binding.progressBar.visibility = View.GONE

                        count = count+1
                        apiCall(count)
                    }
                }
            }

        })

    }


}