package com.viakid.driver.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.viakid.driver.ui.screen.auth.LoginScreen
import com.viakid.driver.ui.screen.certification.CertificationScreen
import com.viakid.driver.ui.screen.exam.ExamScreen
import com.viakid.driver.ui.screen.order.grab.GrabScreen
import com.viakid.driver.ui.screen.orderdetail.OrderDetailScreen
import com.viakid.driver.ui.screen.schedule.ScheduleScreen
import com.viakid.driver.ui.screen.taskboard.TaskBoardScreen
import com.viakid.driver.ui.screen.training.CourseDetailScreen
import com.viakid.driver.ui.screen.training.TrainingScreen
import com.viakid.driver.ui.screen.profile.ProfileScreen
import com.viakid.driver.data.local.TokenManager
import kotlinx.coroutines.flow.first

// 路由定义
/**
 * 应用屏幕路由密封类，定义所有可导航的屏幕路径
 *
 * @property route 路由路径字符串
 */
sealed class Screen(val route: String) {
    /** 登录页路由 */
    data object Login : Screen("login")

    /** 资质认证页路由 */
    data object Certification : Screen("certification")

    /** 培训中心页路由 */
    data object Training : Screen("training")

    /** 课程详情页路由 */
    data object CourseDetail : Screen("course/{courseId}") {
        /**
         * 创建课程详情路由
         *
         * @param courseId 课程ID
         * @return String 完整的路由路径
         */
        fun createRoute(courseId: String): String = "course/$courseId"
    }

    /** 考试页路由 */
    data object Exam : Screen("exam")

    /** 任务看板页路由 */
    data object TaskBoard : Screen("taskboard")

    /** 抢单页路由 */
    data object Grab : Screen("grab")

    /** 订单详情页路由 */
    data object OrderDetail : Screen("order/{orderId}") {
        /**
         * 创建订单详情路由
         *
         * @param orderId 订单ID
         * @return String 完整的路由路径
         */
        fun createRoute(orderId: String): String = "order/$orderId"
    }

    /** 排班管理页路由 */
    data object Schedule : Screen("schedule")

    /** 个人中心页路由 */
    data object Profile : Screen("profile")
}

/**
 * 底部导航项密封类，定义底部导航栏的各个项目
 *
 * @property route 导航路由
 * @property label 显示标签
 * @property selectedIcon 选中时的图标
 * @property unselectedIcon 未选中时的图标
 */
sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    /** 任务看板导航项 */
    data object TaskBoard : BottomNavItem(
        route = Screen.TaskBoard.route,
        label = "任务看板",
        selectedIcon = Icons.AutoMirrored.Filled.ListAlt,
        unselectedIcon = Icons.AutoMirrored.Outlined.ListAlt
    )

    /** 抢单导航项 */
    data object Grab : BottomNavItem(
        route = Screen.Grab.route,
        label = "抢单",
        selectedIcon = Icons.Filled.Bolt,
        unselectedIcon = Icons.Outlined.Bolt
    )

    /** 排班导航项 */
    data object Schedule : BottomNavItem(
        route = Screen.Schedule.route,
        label = "排班",
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    )

    /** 个人中心导航项 */
    data object Profile : BottomNavItem(
        route = Screen.Profile.route,
        label = "我的",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
}

/**
 * 底部导航项列表，用于构建底部导航栏
 */
val bottomNavItems: List<BottomNavItem> = listOf(
    BottomNavItem.TaskBoard,
    BottomNavItem.Grab,
    BottomNavItem.Schedule,
    BottomNavItem.Profile
)

/**
 * ViaKid司机端应用主界面，包含导航和屏幕管理
 *
 * @param tokenManager Token管理器，用于检查登录状态
 */
@Composable
fun ViaKidApp(
    tokenManager: TokenManager
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 判断是否显示底部导航
    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    // 判断是否已登录（启动时从 DataStore 恢复）
    var isLoggedIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isLoggedIn = tokenManager.isLoggedIn.first()
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach {
                        /**
                         * @param item 当前遍历的底部导航项
                         */
                            item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (currentDestination?.hierarchy?.any { it.route == item.route } == true)
                                        item.selectedIcon
                                    else
                                        item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) {
        /**
         * @param innerPadding 底部导航栏的内边距，用于避免内容被遮挡
         */
            innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Screen.TaskBoard.route else Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 登录页
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        /**
                         * @param needsCert 是否需要完成资质认证
                         * @param needsTraining 是否需要完成培训
                         */
                            needsCert, needsTraining ->
                        isLoggedIn = true
                        if (needsCert) {
                            navController.navigate(Screen.Certification.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } else if (needsTraining) {
                            navController.navigate(Screen.Training.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.TaskBoard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    },
                    onNavigateToCertification = {
                        navController.navigate(Screen.Certification.route)
                    },
                    onNavigateToTraining = {
                        navController.navigate(Screen.Training.route)
                    }
                )
            }

            // 资质认证页
            composable(Screen.Certification.route) {
                CertificationScreen(
                    onBack = { navController.popBackStack() },
                    onComplete = {
                        navController.navigate(Screen.Training.route) {
                            popUpTo(Screen.Certification.route) { inclusive = true }
                        }
                    },
                    onSkip = {
                        navController.navigate(Screen.Training.route) {
                            popUpTo(Screen.Certification.route) { inclusive = true }
                        }
                    }
                )
            }

            // 培训中心页
            composable(Screen.Training.route) {
                TrainingScreen(
                    onNavigateToCourse = {
                        /**
                         * @param courseId 课程ID，用于导航到课程详情页
                         */
                            courseId ->
                        navController.navigate(Screen.CourseDetail.createRoute(courseId))
                    },
                    onNavigateToExam = {
                        navController.navigate(Screen.Exam.route)
                    },
                    onNavigateToCertificate = { /* TODO */ },
                    onBack = { navController.popBackStack() }
                )
            }

            // 课程详情页
            composable(
                route = Screen.CourseDetail.route,
                arguments = listOf(navArgument("courseId") { type = NavType.StringType })
            ) {
                /**
                 * @param backStackEntry 当前导航回栈 entry，包含路由参数
                 */
                    backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
                CourseDetailScreen(
                    courseId = courseId,
                    onBack = { navController.popBackStack() },
                    onPrevious = { /* TODO */ },
                    onNext = { /* TODO */ },
                    onComplete = { /* TODO */ }
                )
            }

            // 考试页
            composable(Screen.Exam.route) {
                ExamScreen(
                    onBack = { navController.popBackStack() },
                    onComplete = {
                        /**
                         * @param passed 是否通过考试
                         * @param score 考试分数
                         */
                            passed, score ->
                        navController.navigate(Screen.Training.route) {
                            popUpTo(Screen.Training.route) { inclusive = true }
                        }
                    }
                )
            }

            // 任务看板页（合并首页功能）
            composable(Screen.TaskBoard.route) {
                TaskBoardScreen(
                    onNavigateToOrderDetail = {
                        /**
                         * @param orderId 订单ID，用于导航到订单详情页
                         */
                            orderId ->
                        navController.navigate(Screen.OrderDetail.createRoute(orderId))
                    },
                    onNavigateToCalendar = { /* TODO */ }
                )
            }

            // 抢单页
            composable(Screen.Grab.route) {
                GrabScreen(
                    onNavigateToOrderDetail = {
                        /**
                         * @param orderId 订单ID，用于导航到订单详情页
                         */
                            orderId ->
                        navController.navigate(Screen.OrderDetail.createRoute(orderId))
                    }
                )
            }

            // 订单详情页
            composable(
                route = Screen.OrderDetail.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) {
                /**
                 * @param backStackEntry 当前导航回栈 entry，包含路由参数
                 */
                    backStackEntry ->
                OrderDetailScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // 排班管理页
            composable(Screen.Schedule.route) {
                ScheduleScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // 个人中心页
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        isLoggedIn = false
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
