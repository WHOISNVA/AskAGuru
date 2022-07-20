package com.dapperartisancompany.askaguru

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.swipeable
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dapperartisancompany.askaguru.baselayout.MaterialMusicScaffold
import com.dapperartisancompany.askaguru.layout.Header
import com.dapperartisancompany.askaguru.layout.HomeScreen
import com.dapperartisancompany.askaguru.navigation.BottomBarScreen
import com.dapperartisancompany.askaguru.navigation.BottomNavGraph
import com.dapperartisancompany.askaguru.viewmodels.MainViewModel
import com.dapperartisancompany.askaguru.viewmodels.PlayerSwipeableState
import com.dapperartisancompany.askaguru.viewmodels.PlayerViewModel
import com.example.askaguru.screens.ProfileScreen
import com.example.askaguru.screens.SettingsScreen

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainLayout() {
    val navController = rememberNavController()
    val mainViewModel = viewModel<MainViewModel>()
    val playerViewModel = viewModel<PlayerViewModel>()
    MaterialMusicScaffold(
        topBar = {
       Header()
        },
        bottomBar = {
            Column {
                BottomBar(navController)
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                )
            }
        },
        player = {
           MusicPlayer()
        }
    )
    {
        NavHost(
            navController = navController,
            startDestination = BottomBarScreen.Home.route
        ) {
            composable(route = BottomBarScreen.Home.route) {
                HomeScreen(mainViewModel = mainViewModel, playerViewModel = playerViewModel)
            }
            composable(route = BottomBarScreen.Profile.route) {
                ProfileScreen()
            }
            composable(route = BottomBarScreen.Settings.route) {
                SettingsScreen()
            }
        }

        }
    }

@Composable
fun BottomBar(navController: NavHostController) {
    val screens = listOf(
        BottomBarScreen.Home,
        BottomBarScreen.Profile,
        BottomBarScreen.Settings,
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    BottomNavigation {
        screens.forEach { screen ->
            AddItem(
                screen = screen,
                currentDestination = currentDestination,
                navController = navController
            )
        }
    }
}



@Composable
@Preview
fun MusicPlayer(){
    var sliderPosition by remember { mutableStateOf(0F)}

    Card(
        modifier = Modifier
            .padding(16.dp)
            .background(color = Color.Black)
            .fillMaxWidth(),
        elevation =  5.dp,
    ) {
        Row(modifier = Modifier
            .background(color = Color.Black),
            verticalAlignment = Alignment.CenterVertically) {
            Image(
                painterResource(
                id = R.drawable.flygod_album),
                contentDescription = "",
                modifier = Modifier
                    .width(60.dp)
                    .height(60.dp)
                    .padding(0.dp, 0.dp, 0.dp, 0.dp))
            Column(modifier = Modifier
                .padding(20.dp,0.dp,10.dp,0.dp )) {
                Text(text = "WESTSIDE GUNN", color = Color.White, fontSize = 15.sp, )
                Text(text = "Lunchin", color = Color.White, fontSize = 13.sp)
            }
                Image(
                    painter = painterResource(id = R.drawable.ic_pause_foreground),
                    modifier = Modifier
                        .padding(90.dp, 0.dp, 40.dp, 0.dp)
                        .height(60.dp)
                        .width(60.dp),
                     contentDescription = "Pause Button" )
            }
        }
    }


@Composable
fun RowScope.AddItem(
    screen: BottomBarScreen,
    currentDestination: NavDestination?,
    navController: NavHostController
)

{
    BottomNavigationItem(
        icon = {
            Image(
                painter = painterResource(id = screen.icon),
                contentDescription = "",
                modifier = Modifier
                    .height(25.dp)
                    .width(25.dp)
            )
        },
        selected = currentDestination?.hierarchy?.any {
            it.route == screen.route
        } == true,
        unselectedContentColor = LocalContentColor.current.copy(alpha = ContentAlpha.disabled),
        onClick = {
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        }
    )
}