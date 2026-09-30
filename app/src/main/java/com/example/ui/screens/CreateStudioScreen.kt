package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NyintAmber
import com.example.ui.theme.NyintCardBorderDark
import com.example.ui.theme.NyintCyan
import com.example.ui.theme.NyintEmerald
import com.example.ui.theme.NyintRose
import com.example.ui.theme.NyintViolet
import com.example.ui.viewmodel.MainViewModel

@Composable
fun CreateStudioScreen(
    viewModel: MainViewModel,
    onNavigateToChat: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Content Creator", "Image AI Studio")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Studio Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Creative Studio",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Professional AI content generator & visual design suite",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Custom Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = NyintCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = NyintCyan
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        if (selectedTabIndex == 0) {
            ContentCreatorTab(viewModel = viewModel, onNavigateToChat = onNavigateToChat)
        } else {
            ImageAiStudioTab(viewModel = viewModel, onNavigateToChat = onNavigateToChat)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContentCreatorTab(
    viewModel: MainViewModel,
    onNavigateToChat: () -> Unit
) {
    var userPrompt by remember { mutableStateOf("") }
    var selectedQuickAction by remember { mutableStateOf("Write") }
    var selectedFormat by remember { mutableStateOf("Facebook Post") }

    val quickActions = listOf(
        "Write", "Rewrite", "Improve", "Summarize", "Translate",
        "Expand", "Shorten", "Make Professional", "Make Natural", "Generate Ideas"
    )

    val formats = listOf(
        "Facebook Post", "TikTok Caption", "YouTube Description",
        "Blog Article", "Product Description", "Marketing Ad", "Email Pitch", "Story"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Action Chips
        item {
            Text(
                text = "Quick Action",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (action in quickActions) {
                    val isSelected = action == selectedQuickAction
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) NyintCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) NyintCyan else NyintCardBorderDark
                        ),
                        modifier = Modifier.clickable { selectedQuickAction = action }
                    ) {
                        Text(
                            text = action,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) NyintCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Format Selection
        item {
            Text(
                text = "Content Format",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (fmt in formats) {
                    val isSelected = fmt == selectedFormat
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) NyintViolet.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) NyintViolet else NyintCardBorderDark
                        ),
                        modifier = Modifier.clickable { selectedFormat = fmt }
                    ) {
                        Text(
                            text = fmt,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) NyintViolet else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Prompt Input
        item {
            Text(
                text = "Topic, Keywords, or Draft",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = userPrompt,
                onValueChange = { userPrompt = it },
                placeholder = {
                    Text(
                        "e.g. Introduce a new premium cat café in Yangon with cozy aesthetic and artisan coffee...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NyintCyan,
                    unfocusedBorderColor = NyintCardBorderDark
                )
            )
        }

        // Generate Button
        item {
            Button(
                onClick = {
                    val fullInstruction = """
                        [$selectedQuickAction Task: $selectedFormat]
                        Action: $selectedQuickAction
                        Format: $selectedFormat
                        Topic / Details: ${userPrompt.ifEmpty { "High engagement and viral reach with bilingual English & Myanmar appeal." }}
                        
                        Please produce high quality, natural, and compelling content with hooks, body, hashtags, and call to action.
                    """.trimIndent()
                    viewModel.triggerQuickAction(fullInstruction)
                    onNavigateToChat()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NyintCyan),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate with AI in Chatbox",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 15.sp
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImageAiStudioTab(
    viewModel: MainViewModel,
    onNavigateToChat: () -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    var selectedCapability by remember { mutableStateOf("Generate Concept") }

    val capabilities = listOf(
        "Generate Concept", "Remove Background Guidance", "Product Ad Staging",
        "Social Graphic", "Poster Design", "Fashion / Editorial", "Thumbnail"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = NyintRose.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, NyintRose.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = NyintRose,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Image AI supports real image models (gemini-2.5-flash-image / DALL-E 3) when configured, or multimodal analysis with your camera & gallery photos.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Text(
                text = "Image Workflow",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (cap in capabilities) {
                    val isSelected = cap == selectedCapability
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) NyintRose.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) NyintRose else NyintCardBorderDark
                        ),
                        modifier = Modifier.clickable { selectedCapability = cap }
                    ) {
                        Text(
                            text = cap,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) NyintRose else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Describe Image or Desired Transformation",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                placeholder = {
                    Text(
                        "e.g. Clean modern luxury perfume bottle on wet dark slate with holographic neon lighting...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NyintRose,
                    unfocusedBorderColor = NyintCardBorderDark
                )
            )
        }

        item {
            Button(
                onClick = {
                    val fullInstruction = """
                        [Image AI Studio: $selectedCapability]
                        Workflow: $selectedCapability
                        Details: ${prompt.ifEmpty { "High detail aesthetic visual concept with modern lighting and framing." }}
                        
                        If image generation is supported by the active model, generate or outline the precise prompt and visual composition parameters.
                    """.trimIndent()
                    viewModel.triggerQuickAction(fullInstruction)
                    onNavigateToChat()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NyintRose),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Brush,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Launch Visual Studio in Chatbox",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
