package com.cryptium.coincurrently.features.Portfolio.Sheets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.sheets.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetBody
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.sheets.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.components.DragHandle
import com.cryptium.coincurrently.features.Portfolio.Views.TransactionTypeView
import com.cryptium.coincurrently.features.Portfolio.Enums.PortfolioExportFormat
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SelectExportFormatSheet(
    freeExportsRemaining: Int?,
    formatSelected: (PortfolioExportFormat?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(dragHandleTitle = "Export Portfolio")

    ModalBottomSheet(
        onDismissRequest = {
            coroutineScope.launch {
                formatSelected.invoke(null)
            }
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colors.surfaceContainerLow,
        tonalElevation = 16.dp,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = {
            DragHandle(
                title = "Export Portfolio",
                closeAction = {
                    coroutineScope.launch {
                        sheetState.hide()
                        formatSelected.invoke(null)
                    }
                },
            )
        },
    ) {
        ModalBottomSheetSystemNavigationBar()
        ModalSheetBody(
            context = context,
            maxBodyHeight = maxBodyHeight,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Separator()

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 24.dp),
                ) {
                    freeExportsRemaining?.let { count ->
                        Text(
                            text = freeExportMessage(count),
                            style = satoshiBold(14),
                            color = MaterialTheme.colors.onSurfaceDim,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .semantics {
                                    contentDescription = freeExportAccessibilityLabel(count)
                                },
                        )
                    }

                    SectionHeader(Localization.string("General"))

                    ExportOption(
                        title = "CSV",
                        subtitle = Localization.string("Spreadsheet-friendly format."),
                        image = R.drawable.list,
                        format = PortfolioExportFormat.CSV,
                        sheetState = sheetState,
                        formatSelected = formatSelected,
                    )

                    ExportOption(
                        title = "JSON",
                        subtitle = Localization.string("Structured data format."),
                        image = R.drawable.converter,
                        format = PortfolioExportFormat.JSON,
                        sheetState = sheetState,
                        formatSelected = formatSelected,
                    )

                    ExportOption(
                        title = Localization.string("Plain Text"),
                        subtitle = Localization.string("Human-readable format."),
                        image = R.drawable.feedback,
                        format = PortfolioExportFormat.PlainText,
                        sheetState = sheetState,
                        formatSelected = formatSelected,
                    )

                    SectionHeader(Localization.string("Tax Software (Pro)"), modifier = Modifier.padding(top = 12.dp))

                    ExportOption(
                        title = "Koinly",
                        subtitle = Localization.string("Koinly CSV template."),
                        image = R.drawable.share,
                        format = PortfolioExportFormat.Koinly,
                        sheetState = sheetState,
                        formatSelected = formatSelected,
                    )

                    ExportOption(
                        title = "CoinTracker",
                        subtitle = Localization.string("CoinTracker CSV template."),
                        image = R.drawable.share,
                        format = PortfolioExportFormat.CoinTracker,
                        sheetState = sheetState,
                        formatSelected = formatSelected,
                    )

                    ExportOption(
                        title = "TokenTax",
                        subtitle = Localization.string("TokenTax manual CSV format."),
                        image = R.drawable.share,
                        format = PortfolioExportFormat.TokenTax,
                        sheetState = sheetState,
                        formatSelected = formatSelected,
                    )

                    ExportOption(
                        title = "ZenLedger",
                        subtitle = Localization.string("ZenLedger custom CSV template."),
                        image = R.drawable.share,
                        format = PortfolioExportFormat.ZenLedger,
                        sheetState = sheetState,
                        formatSelected = formatSelected,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = satoshiBold(12),
        color = MaterialTheme.colors.onSurfaceDimmer,
        modifier = modifier.padding(bottom = 8.dp),
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ExportOption(
    title: String,
    subtitle: String,
    image: Int,
    format: PortfolioExportFormat,
    sheetState: androidx.compose.material3.SheetState,
    formatSelected: (PortfolioExportFormat?) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    TransactionTypeView(
        title = title,
        subtitle = subtitle,
        image = image,
        viewTapped = {
            coroutineScope.launch {
                sheetState.hide()
                formatSelected(format)
            }
        },
    )
}

private fun freeExportMessage(count: Int): String {
    return if (count == 1) {
        Localization.string("You can export it 1 more time for free.")
    } else {
        Localization.format("You can export it %d more times for free.", count)
    }
}

private fun freeExportAccessibilityLabel(count: Int): String = freeExportMessage(count)
