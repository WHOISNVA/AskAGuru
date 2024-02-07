package com.example.askguru.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.askguru.R
import com.example.askguru.databinding.FragmentSettingsBinding
import com.example.askguru.ui.SplashActivity
import com.example.askguru.utils.Const
import com.example.askguru.utils.PreferenceHelper


class SettingsFragment : DialogFragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun getTheme(): Int {
        return R.style.FullScreenDialogStyle
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root

        //return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = activity?.findNavController(R.id.nav_host_fragment_activity_main)

        if (PreferenceHelper.getBooleanPreference(requireContext(), Const.PRE_IS_LOGIN, false)) {
            binding.llLogout.visibility = View.VISIBLE
            binding.llDeleteAccount.visibility = View.VISIBLE
            binding.view.visibility = View.VISIBLE
        } else {
            binding.llLogout.visibility = View.GONE
            binding.llDeleteAccount.visibility = View.GONE
            binding.view.visibility = View.GONE
        }

        binding.ivCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.llPrivacy.setOnClickListener {
            openUrl()
        }

        binding.llGeneral.setOnClickListener { openUrl() }

        binding.llFeedback.setOnClickListener { openUrl() }

        binding.llLogout.setOnClickListener {
            showLogoutDialog("Logout", "Are you sure you want to logout?")
        }

        binding.llDeleteAccount.setOnClickListener {
            showLogoutDialog("Delete Account", "Are you sure you want to Delete Account?")
        }
    }

    private fun openUrl() {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse("https://askaguru.com/explore")
        startActivity(openURL)
    }

    private fun showLogoutDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(title)
        builder.setMessage("Are you sure you want to logout")
        builder.setPositiveButton("Yes") { dialog, which ->
            PreferenceHelper.deleteAllSharedPrefs(requireContext())
            dialog.dismiss()
            requireContext().startActivity(
                Intent(
                    requireActivity(),
                    SplashActivity::class.java
                ).apply {
                    flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                })
        }
        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

}
