package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AIModelInfo
import com.example.data.model.AIModelRegistry
import com.example.data.model.AIProvider
import com.example.ui.theme.NyintAmber
import com.example.ui.theme.NyintCardBorderDark
import com.example.ui.theme.NyintCyan
import com.example.ui.theme.NyintEmerald
import com.example.ui.theme.NyintViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectorDropdown(
    currentProvider: AIProvider,
    currentModelId: String,
    onModelSelected: (AIProvider, String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentModel = remember(currentModelId) {
        AIModelRegistry.findModel(currentModelId) ?: AIModelRegistry.ALL_MODELS.first()
    }

    // Top Selector Button
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
        border = androidx.compose.foundation.BorderStroke(1.dp, NyintCardBorderDark),
        modifier = modifier
            .testTag("model_selector_trigger")
            .clip(RoundedCornerShape(20.dp))
            .clickable { showSheet = true }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Provider status dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = Color(currentProvider.badgeColorHex),
                        shape = CircleShape
                    )
            )

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currentModel.displayName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Model",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            ModelSelectionSheetContent(
                selectedProvider = currentProvider,
                selectedModelId = currentModelId,
                onSelect = { provider, modelId ->
                    onModelSelected(provider, modelId)
                    showSheet = false
                },
                onOpenSettings = {
                    showSheet = false
                    onOpenSettings()
                }
            )
        }
    }
}

@Composable
fun ModelSelectionSheetContent(
    selectedProvider: AIProvider,
    selectedModelId: String,
    onSelect: (AIProvider, String) -> Unit,
    onOpenSettings: () -> Unit
) {
    var filterProvider by remember { mutableStateOf<AIProvider?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Select AI Model & Provider",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Switch between Google, OpenAI, and Anthropic Claude",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("configure_keys_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = NyintCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("API Keys", color = NyintCyan, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Provider Filter Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProviderFilterChip(
                label = "All",
                isSelected = filterProvider == null,
                onClick = { filterProvider = null }
            )
            for (p in listOf(AIProvider.GEMINI, AIProvider.OPENAI, AIProvider.CLAUDE)) {
                ProviderFilterChip(
                    label = p.shortName,
                    isSelected = filterProvider == p,
                    color = Color(p.badgeColorHex),
                    onClick = { filterProvider = p }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        val modelsToShow = remember(filterProvider) {
            if (filterProvider == null) {
                AIModelRegistry.ALL_MODELS
            } else {
                AIModelRegistry.getModelsForProvider(filterProvider!!)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(modelsToShow) { model ->
                val isSelected = model.id == selectedModelId
                ModelItemCard(
                    model = model,
                    isSelected = isSelected,
                    onSelect = { onSelect(model.provider, model.id) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ProviderFilterChip(
    label: String,
    isSelected: Boolean,
    color: Color = NyintCyan,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, color) else null,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun ModelItemCard(
    model: AIModelInfo,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) NyintCyan else NyintCardBorderDark
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("model_item_${model.id}")
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = model.displayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Provider Tag
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(model.provider.badgeColorHex).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = model.provider.shortName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(model.provider.badgeColorHex),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (model.supportsVision) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NyintEmerald.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Vision",
                                fontSize = 10.sp,
                                color = NyintEmerald,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = model.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = NyintCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
