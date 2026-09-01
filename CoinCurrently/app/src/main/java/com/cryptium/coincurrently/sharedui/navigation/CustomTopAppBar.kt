package com.cryptium.coincurrently.sharedui.navigation
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.modifiers.CappedFontScale
import com.cryptium.coincurrently.sharedui.modifiers.DynamicTypeSizeCap
import com.cryptium.coincurrently.core.extensions.satoshiBold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(
    title: String,
    accessibilityTitle: String? = null,
    navigateBack: () -> Unit,
    actions: List<@Composable (() -> Unit)>? = null
) {
    CappedFontScale(DynamicTypeSizeCap.Large) {
    TopAppBar(
        title = {
            Text(
                title,
                style = satoshiBold(22),
                modifier = Modifier.semantics {
                    heading()
                    if (accessibilityTitle != null) {
                        contentDescription = accessibilityTitle
                    }
                },
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colors.surface,
        ),
        windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        navigationIcon = {
            IconButton(onClick = {
                navigateBack.invoke()
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Navigate back"
                )
            }
        },
        actions = {
            actions?.forEach {
                it.invoke()
            }
        }
    )
    }
}