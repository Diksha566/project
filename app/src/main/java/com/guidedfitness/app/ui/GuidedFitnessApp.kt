package com.guidedfitness.app.ui

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guidedfitness.app.navigation.GuidedFitnessNavGraph

@androidx.compose.runtime.Composable
fun GuidedFitnessApp(
    viewModel: com.guidedfitness.app.ui.viewmodel.AppViewModel = viewModel(
        factory = com.guidedfitness.app.ui.viewmodel.AppViewModelFactory(
            LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    GuidedFitnessNavGraph(viewModelProvider = { viewModel })
}
