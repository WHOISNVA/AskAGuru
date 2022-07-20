package com.dapperartisancompany.askaguru.navigation

import com.dapperartisancompany.askaguru.R

sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: Int
) {
    object Home : BottomBarScreen(
        route = "home",
        title = "Home",
        icon = R.drawable.house_icon
    )

    object Profile : BottomBarScreen(
        route = "profile",
        title = "Add",
        icon = R.drawable.new_playlist
    )

    object Settings : BottomBarScreen(
        route = "settings",
        title = "Notifications",
        icon = R.drawable.notifications
    )
}
