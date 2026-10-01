package com.cryptium.coincurrently.features.Settings.Feedback
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.app.MainActivity
import com.cryptium.coincurrently.sharedui.buttons.PrimaryButton
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerHigh
import com.cryptium.coincurrently.sharedui.theme.trendDown
import com.cryptium.coincurrently.sharedui.inputs.CustomFreeInputField
import com.cryptium.coincurrently.core.extensions.satoshiMedium
import com.cryptium.coincurrently.sharedui.navigation.CustomTopAppBar
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.analytics.FeedbackSent
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.sharedui.components.SnackBarType
import com.cryptium.coincurrently.features.PriceAlerts.PriceAlertsViewModel
import kotlinx.coroutines.launch

@Composable
fun FeedbackScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val rating: MutableState<Int?> = remember { mutableStateOf(null) }
    val feedback: MutableState<String> = remember { mutableStateOf("") }
    var isLoading: Boolean by remember { mutableStateOf(false) }

    val viewModel: PriceAlertsViewModel = viewModel()

    BackHandler(onBack = {
        navigationViewModel.navigateBack()
    })

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        CustomTopAppBar(
            title = Localization.string("Help us Improve"),
            navigateBack = {
                navigationViewModel.navigateBack()
            }
        )

        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 24.dp,
                    bottom = 24.dp
                )
        ) {
            Text(
                text = Localization.string("How’s your experience with CoinCurrently?"),
                style = satoshiMedium(14),
                color = MaterialTheme.colors.onSurfaceDim
            )

            RatingView(
                rating = rating
            )

            InputField(
                feedback = feedback
            )
        }

        Separator(
            padding = PaddingValues(
                bottom = 24.dp,
                start = 16.dp,
                end = 16.dp
            )
        )

        PrimaryButton(
            text = Localization.string("Submit Feedback"),
            accessibilityLabel = if (rating.value != null) {
                Localization.string("Submit Feedback")
            } else {
                Localization.string("Submit: Disabled because invalid or no rating is entered")
            },
            isEnabled = rating.value != null,
            fullWidth = true,
            isLoading = isLoading,
            onClick = {
                if (rating.value == null) {
                    return@PrimaryButton
                }

                isLoading = true

                coroutineScope.launch {
                    if (
                        viewModel.sendFeedback(
                            rating = rating.value ?: 0,
                            feedback = feedback.value
                        )
                    ) {
                        AnalyticsManager.log(
                            FeedbackSent(ScreenLabel.Feedback, rating.value ?: 0),
                        )
                        navigationViewModel.navigateBack()
                        MainActivity.showSnackbar(
                            context,
                            SnackBarType.SUCCESS,
                            Localization.string("Feedback sent, thank you very much!")
                        )

                        if ((rating.value ?: 0) >= 4) {
                            MainActivity.showAppReview(context, true)
                        }
                    }

                    isLoading = false
                }
            },
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 24.dp
                )
        )
    }
}

@Composable
private fun RatingView(
    rating: MutableState<Int?>,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        (1 until 6).forEach { index ->
            val isSelected = rating.value == index

            Text(
                text = index.toString(),
                style = satoshiMedium(16),
                color = if (isSelected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface,
                modifier = Modifier
                    .semantics {
                        contentDescription = Localization.format("Vote %d of %d.", index, 5)
                        role = Role.Button
                        selected = isSelected
                        onClick {
                            rating.value = index
                            true
                        }
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        rating.value = index
                    }
                    .background(
                        color = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.surfaceContainerHigh,
                        shape = CircleShape
                    )
                    .padding(
                        vertical = 12.dp,
                        horizontal = 18.dp
                    )
            )
        }
    }
}

@Composable
private fun InputField(
    feedback: MutableState<String>
) {
    val accessibilityLabel = Localization.format(
        "%@. Textfield",
        if (feedback.value.isEmpty()) {
            Localization.string("Input feedback here")
        } else {
            Localization.format("feedback: %@", feedback.value)
        },
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .padding(top = 32.dp)
    ) {
        Text(
            text = Localization.string("How can we improve? (Optional)"),
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() }
        )

        CustomFreeInputField(
            startingValue = feedback.value,
            placeholder = Localization.string("Enter your feedback"),
            type = KeyboardType.Text,
            maxLines = 10,
            showError = feedback.value.count() > 800,
            accessibilityLabel = accessibilityLabel,
            valueChanged = {
                feedback.value = it
            }
        )

        Text(
            text = Localization.format("%d / 800 characters", feedback.value.count()),
            style = satoshiMedium(14),
            color = if (feedback.value.count() > 800) MaterialTheme.colors.trendDown else MaterialTheme.colors.onSurfaceDimmer,
            modifier = Modifier
                .padding(end = 16.dp)
                .semantics { invisibleToUser() }
        )
    }
}