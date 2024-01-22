package com.example.askguru.ui.profile

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.canhub.cropper.CropImageContract
import com.example.askguru.R
import com.example.askguru.databinding.FragmentProfileBinding
import com.example.askguru.network.ApiHelper
import com.example.askguru.network.RetrofitBuilder
import com.example.askguru.network.Status
import com.example.askguru.network.ViewModelFactory
import com.example.askguru.ui.MainActivity
import com.example.askguru.ui.login.LoginActivity
import com.example.askguru.utils.Const
import com.example.askguru.utils.Const.Companion.PRE_AUTHORIZATION_TOKEN
import com.example.askguru.utils.PreferenceHelper
import com.example.askguru.viewmodel.login.UserResponse
import com.example.askguru.viewmodel.profile.ProfileDataResponse
import com.example.askguru.viewmodel.profile.ProfileVm
import com.example.askguru.viewmodel.profile.UpdateProfileRequest
import com.example.askguru.viewmodel.profile.UserProfileUpdate
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.w3c.dom.Text
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream


class ProfileFragment : Fragment {

    //Add empty constructor to avoid the exception
    constructor() : super()

    private val profileArray = arrayOf("All Playlist", "Likes", "Contributions")

    private lateinit var binding: FragmentProfileBinding

    private lateinit var viewModel: ProfileVm

    private var myUserId: String? = null
    private var isFollowing: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))
        )[ProfileVm::class.java]

        myUserId = PreferenceHelper.getStringPreference(requireActivity(), Const.PRE_USER_ID)

        if (PreferenceHelper.getBooleanPreference(requireActivity(), "is_fromRanking", false)) {
            val userId = PreferenceHelper.getStringPreference(requireActivity(), "rank_user_id")


            // used in ProfilePlatListDetailsFragment screen
            PreferenceHelper.setBooleanPreference(requireContext(), "is_show_delete_button", false)

            PreferenceHelper.setBooleanPreference(requireActivity(), "is_fromRanking", false)
            //PreferenceHelper.setStringPreference(requireActivity(), "rank_user_id", "")
            PreferenceHelper.setBooleanPreference(requireContext(), "is_fromRanking_click", true)

            binding.llFollow.visibility = View.VISIBLE

            getRankProfileApiCall(userId)

        } else if (PreferenceHelper.getBooleanPreference(
                requireContext(),
                Const.PRE_IS_LOGIN,
                false
            )
        ) {
            binding.llLogin.visibility = View.GONE
            binding.llFollow.visibility = View.GONE
            getProfileDataApiCall()

            PreferenceHelper.setBooleanPreference(requireContext(), "is_show_delete_button", true)

            myProfileAction()
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

    private fun myProfileAction() {
        binding.ivUserImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.imgEditBio.setOnClickListener {
            binding.imgEditBio.visibility = View.GONE
            binding.tvBio.visibility = View.GONE
            binding.etBio.visibility = View.VISIBLE
            binding.etBio.setText(binding.tvBio.text)
            binding.etBio.requestFocus()
            binding.etBio.imeOptions = EditorInfo.IME_ACTION_DONE
            showKeyboard(binding.etBio, requireContext())
        }



        binding.etBio.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                requireActivity().hideKeyboard()
                binding.etBio.visibility = View.GONE
                binding.tvBio.visibility = View.VISIBLE
                binding.tvBio.text = binding.etBio.text
                updateBio(binding.etBio.text.toString())
                return@setOnEditorActionListener true
            }
            false
        }
    }
    fun Activity.hideKeyboard() {
        val view: View? = currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun showKeyboard(editText: EditText, context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                //openCropActivity(uri)

                val file = getFileFromUri(uri)
                if (file != null) {
                    // Use the file for further processing
                    binding.ivUserImage.loadCornerImage(file.toString())

                    val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                    val parts: MutableList<MultipartBody.Part> = ArrayList()
                    parts.add(body)

                    val token = "Bearer " + PreferenceHelper.getStringPreference(
                        requireActivity(),
                        PRE_AUTHORIZATION_TOKEN
                    )

                    viewModel.uploadProfilePic(token, parts).observe(requireActivity()) {
                        it.let { resource ->
                            when (resource.status) {
                                Status.LOADING -> {
                                    binding.progressBar.visibility = View.VISIBLE
                                }

                                Status.SUCCESS -> {
                                    binding.progressBar.visibility = View.GONE
                                    /*Toast.makeText(
                                        requireActivity(),
                                        "Profile picture uploaded successfully",
                                        Toast.LENGTH_LONG
                                    ).show()*/

                                    getUserdataApiCall(token)
                                }

                                Status.ERROR -> {
                                    binding.progressBar.visibility = View.GONE
                                    Toast.makeText(
                                        requireActivity(),
                                        "Some thing went wrong",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                            }
                        }
                    }
                } else {
                    // Handle error in getting the file from Uri
                }
            }
        }
    private fun getFileFromUri(uri: Uri): File? {
        val context = requireContext().applicationContext
        val contentResolver = context.contentResolver

        val scheme = uri.scheme
        if (scheme == null || scheme == ContentResolver.SCHEME_FILE) {
            return File(uri.path!!)
        } else if (scheme == ContentResolver.SCHEME_CONTENT) {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    val inputStream = contentResolver.openInputStream(uri)
                    inputStream?.use { input ->
                        val outputFile = File(context.cacheDir, displayName)
                        val outputStream = FileOutputStream(outputFile)
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                        return outputFile
                    }
                }
            }
        }
        return null
    }

    /* private fun openCropActivity(uri: Uri) {
         cropImage.launch(
             options(uri = uri) {
                 setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                 setScaleType(CropImageView.ScaleType.FIT_CENTER)

                 setCropShape(CropImageView.CropShape.OVAL)
                 setAspectRatio(1, 1)

                 setActivityMenuIconColor(Color.WHITE)
                 setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                 setOutputCompressQuality(90)
                 setRequestedSize(
                     0,
                     0,
                     CropImageView.RequestSizeOptions.RESIZE_INSIDE
                 )
                 setCropMenuCropButtonIcon(this@EditProfileActivity.getColorCompat(R.color.white))
                 setActivityBackgroundColor(Color.BLACK)
                 setToolbarColor(Color.BLACK)

             }
         )
     }*/

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent.let {
                binding.ivUserImage.loadCornerImage(it.toString())
            }
            //profileUri = result.getUriFilePath(this)
        } else {
            // an error occurred
            val exception = result.error
        }
    }

    private fun followApiCall() {
        val userId = PreferenceHelper.getStringPreference(requireActivity(), "rank_user_id")
        val token = "Bearer " + PreferenceHelper.getStringPreference(
            requireActivity(),
            PRE_AUTHORIZATION_TOKEN
        )

        if (userId.isNullOrEmpty()) {
            Toast.makeText(requireActivity(), "User Id Not found", Toast.LENGTH_LONG).show()
            return
        }

        viewModel.followUnFollow(token, userId, isFollowing).observe(requireActivity(), Observer {
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
                        Toast.makeText(
                            requireActivity(),
                            "Some thing went wrong",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }
        })

    }

    private fun getProfileDataApiCall() {
        val token =
            PreferenceHelper.getStringPreference(requireContext(), PRE_AUTHORIZATION_TOKEN)

        Log.e("kp", "token ==> $token")

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

        binding.tvUserName.text = userObj.username
        //binding.tvEmail.text = userObj.email

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

        userData.user.follower_data.followers.let {
            isFollowing = it.any { it.followed_id == myUserId }
        }

        userObj.biography.let {
            binding.tvBio.text = it
        }

        updateFollowButton()
    }

    private fun updateFollowButton() {
        if (isFollowing) {
            binding.btnFollow.text = "Unfollow"
        } else {
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
        binding.imgEditBio.visibility = View.VISIBLE

        val likeDataString = Gson().toJson(response?.playlists)
        PreferenceHelper.setStringPreference(requireContext(), "playlists", likeDataString)

        val userDataString = PreferenceHelper.getStringPreference(requireContext(), "user_data")
        val userModel = Gson().fromJson(userDataString, UserResponse::class.java)

        binding.tvEmail.text = userModel.email
        binding.tvUserName.text = userModel.username

        userModel.profile_pic.let {
            Glide.with(binding.ivUserImage.context)
                .load(Const.IMAGE_BASE_URL + userModel.profile_pic)
                .placeholder(R.drawable.ic_profile).into(binding.ivUserImage)
        }

        userModel.total_listens.let {
            binding.tvTotalListensCount.text = userModel.total_listens.toString()
        }

        userModel.follower_data.follower_count.let {
            binding.tvFollowersCount.text = userModel.follower_data.follower_count.toString()
        }
        userModel.biography?.let { bio ->
            binding.tvBio.text = bio
        } ?: run {
            binding.tvBio.text = "I love music"
        }

        setViewPagerAdapter()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the result to the current tab's fragment
        val currentFragment = childFragmentManager.fragments.firstOrNull()
        currentFragment?.onActivityResult(requestCode, resultCode, data)
    }


    fun ImageView.loadCornerImage(path: String) {
        Glide.with(this.context)
            .load(path)
            .placeholder(R.drawable.ic_profile)
            .transform(CenterInside(), RoundedCorners(10))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(this)
    }

    fun getMimeType(file: File): String? {
        val extension = getExtension(file.name)
        return if (extension!!.length > 0) MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            extension.substring(1)
        ) else "application/octet-stream"
    }

    fun getExtension(uri: String?): String? {
        if (uri == null) {
            return null
        }
        val dot = uri.lastIndexOf(".")
        return if (dot >= 0) {
            uri.substring(dot)
        } else {
            // No extension.
            ""
        }
    }

    private fun updateBio(text: String) {
        val token = "Bearer " + PreferenceHelper.getStringPreference(
            requireActivity(),
            PRE_AUTHORIZATION_TOKEN
        )

        val updateRequest = UpdateProfileRequest(
            UserProfileUpdate(
                biography = text
            )
        )

        viewModel.updateBio(token, updateRequest).observe(requireActivity()) {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.progressBar.visibility = View.GONE

                        Toast.makeText(
                            requireActivity(),
                            "Updated successfully",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    Status.ERROR -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            requireActivity(),
                            "Some thing went wrong",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }
        }
    }

    private fun getUserdataApiCall(accessToken: String) {
        viewModel.getUserData(accessToken).observe(requireActivity()){
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        binding.progressBar.visibility = View.GONE
                        val userDataString = Gson().toJson(it.data)
                        PreferenceHelper.setStringPreference(requireContext(), "user_data", userDataString)
                        Toast.makeText(
                            requireActivity(),
                            "Profile picture uploaded successfully",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Status.ERROR -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Something went wrong try again", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}

//