package com.example.askguru.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.askguru.repository.MainRepository
import com.example.askguru.viewmodel.add_song.AddSongVm
import com.example.askguru.viewmodel.create_list.CreateListVm
import com.example.askguru.viewmodel.home.HomeVm
import com.example.askguru.viewmodel.login.LoginViewModel
import com.example.askguru.viewmodel.notification.NotificationsViewModel
import com.example.askguru.viewmodel.profile.ProfileVm
import com.example.askguru.viewmodel.ranking.RankingVm
import com.example.askguru.viewmodel.signup.SignUpVm


class ViewModelFactory (private val apiHelper: ApiHelper) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(MainRepository(apiHelper)) as T
        }

        else if (modelClass.isAssignableFrom(SignUpVm::class.java)) {
            return SignUpVm(MainRepository(apiHelper)) as T
        }

        else if (modelClass.isAssignableFrom(HomeVm::class.java)) {
            return HomeVm(MainRepository(apiHelper)) as T
        }

        else if (modelClass.isAssignableFrom(RankingVm::class.java)) {
            return RankingVm(MainRepository(apiHelper)) as T
        }
        else if (modelClass.isAssignableFrom(ProfileVm::class.java)) {
            return ProfileVm(MainRepository(apiHelper)) as T
        }
        else if (modelClass.isAssignableFrom(AddSongVm::class.java)) {
            return AddSongVm(MainRepository(apiHelper)) as T
        }
        else if (modelClass.isAssignableFrom(CreateListVm::class.java)) {
            return CreateListVm(MainRepository(apiHelper)) as T
        }
        else if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
            return NotificationsViewModel(MainRepository(apiHelper)) as T
        }





        throw IllegalArgumentException("Unknown class name")
    }

}