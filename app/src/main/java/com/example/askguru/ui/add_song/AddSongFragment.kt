package com.example.askguru.ui.add_song

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.askguru.R
import com.example.askguru.databinding.FragmentAddSongBinding
import com.example.askguru.network.ApiHelper
import com.example.askguru.network.RetrofitBuilder
import com.example.askguru.network.Status
import com.example.askguru.network.ViewModelFactory
import com.example.askguru.utils.Const
import com.example.askguru.utils.PreferenceHelper
import com.example.askguru.viewmodel.add_song.AddSongVm
import com.example.askguru.viewmodel.add_song.SearchList
import timber.log.Timber


class AddSongFragment : DialogFragment(),SearchSelectClickListener {

    private var _binding: FragmentAddSongBinding? =null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddSongVm

    private lateinit var adapter: SearchAdapter
    val searchList: ArrayList<SearchList> = ArrayList()
    val selectedList: ArrayList<SearchList> = ArrayList()

    override fun getTheme(): Int {
        return R.style.FullScreenDialogStyle
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAddSongBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService)))[AddSongVm::class.java]

        _binding?.edSearch!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(searchText: Editable?) {
                if(_binding?.edSearch!!.text.toString().trim().length>2){
                    getSearchListApiCall(searchText.toString())
                }else{
                    clearList()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.ivClosed.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnAdd.setOnClickListener {

             if(searchList.isEmpty())
               return@setOnClickListener

            searchList.forEach {
                if (it.isSelected) {
                    selectedList.add(it)
                }
            }

            Timber.tag("kp").e("selectedList size -- ${selectedList.size}")

            findNavController().apply {
                previousBackStackEntry
                    ?.savedStateHandle?.set("list",selectedList)
            }.navigateUp()


        }
    }




    private fun getSearchListApiCall(searchText: String) {

        val token = PreferenceHelper.getStringPreference(requireContext(), Const.PRE_AUTHORIZATION_TOKEN)

        viewModel.getSearchList(token,searchText).observe(requireActivity(), Observer {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.progress.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        binding.progress.visibility = View.GONE
                       //Toast.makeText(requireContext(),"success",Toast.LENGTH_SHORT).show()

                        setListData(it.data?.results)
                    }
                    Status.ERROR -> {
                        binding.progress.visibility = View.GONE
                    }
                }
            }
        })
    }

    private fun setListData(list: List<SearchList>?) {
        searchList.clear()
        searchList.addAll(list!!)

        adapter = SearchAdapter(searchList,this)
        _binding?.rvSearchList!!.layoutManager = LinearLayoutManager(requireActivity())
        _binding?.rvSearchList!!.addItemDecoration(
            DividerItemDecoration(_binding?.rvSearchList!!.context, ( _binding?.rvSearchList!!.layoutManager as LinearLayoutManager).orientation)
        )
        _binding?.rvSearchList!!.adapter = adapter

    }

    private fun clearList() {
        searchList.clear()
        adapter = SearchAdapter(searchList,this)
        _binding?.rvSearchList!!.layoutManager = LinearLayoutManager(requireActivity())
        _binding?.rvSearchList!!.addItemDecoration(
            DividerItemDecoration(_binding?.rvSearchList!!.context, ( _binding?.rvSearchList!!.layoutManager as LinearLayoutManager).orientation)
        )
        _binding?.rvSearchList!!.adapter = adapter
    }

    override fun onClickSong(position: Int, list: SearchList) {
        searchList.get(position).isSelected = !list.isSelected
        adapter.notifyDataSetChanged()

    }


}