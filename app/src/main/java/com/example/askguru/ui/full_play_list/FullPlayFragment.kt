package com.example.askguru.ui.full_play_list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.askguru.R
import com.example.askguru.databinding.FragmentCreateListBinding
import com.example.askguru.databinding.FragmentFullPlayBinding


class FullPlayFragment : Fragment() {

    private var _binding: FragmentFullPlayBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
       // return inflater.inflate(R.layout.fragment_full_play, container, false)
        _binding = FragmentFullPlayBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //val navController = activity?.findNavController(R.id.nav_host_fragment_activity_main)

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }


    }

}