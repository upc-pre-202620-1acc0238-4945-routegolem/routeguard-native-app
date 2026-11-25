package pe.edu.upc.routeguard.stakeholderassetmanagement.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.core.designsystem.components.BigButton
import pe.edu.upc.routeguard.core.designsystem.components.ErrorText
import pe.edu.upc.routeguard.core.designsystem.components.FormField
import pe.edu.upc.routeguard.core.designsystem.components.InfoCard
import pe.edu.upc.routeguard.core.designsystem.components.LoadingBox
import pe.edu.upc.routeguard.core.designsystem.components.ScreenHeader
import pe.edu.upc.routeguard.core.designsystem.components.SecondaryButton
import pe.edu.upc.routeguard.core.designsystem.components.StatusChip
import pe.edu.upc.routeguard.core.designsystem.theme.BrandBorder
import pe.edu.upc.routeguard.core.designsystem.theme.BrandCardWhite
import pe.edu.upc.routeguard.core.designsystem.theme.BrandNavy

@Composable
fun StakeholdersScreen(
    modifier: Modifier = Modifier,
    viewModel: StakeholdersViewModel = hiltViewModel(),
    onRegisterDriver: () -> Unit = {},
    onRegisterParent: () -> Unit = {},
    onRegisterChild: () -> Unit = {},
    onCreateGroup: () -> Unit = {}
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(R.string.stakeholders_title))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Filter Pills Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StakeholderTab.entries.forEach { tab ->
                    val isSelected = state.tab == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onTabSelected(tab) },
                        label = {
                            Text(
                                stringResource(tab.labelRes),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandNavy,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Real-Time Reactive Search Bar
            FormField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = stringResource(R.string.stakeholders_search_in_tab, stringResource(state.tab.labelRes).lowercase())
            )
        }

        ErrorText(state.errorMessage, Modifier.padding(horizontal = 16.dp))

        if (state.isLoading && state.drivers.isEmpty() && state.parents.isEmpty() && state.groups.isEmpty()) {
            LoadingBox()
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (state.tab) {
                StakeholderTab.DRIVERS -> {
                    item {
                        BigButton(
                            text = stringResource(R.string.stakeholders_btn_register_driver),
                            onClick = onRegisterDriver,
                            containerColor = BrandNavy,
                            icon = Icons.Filled.Add
                        )
                    }
                    val filteredDrivers = if (searchQuery.isBlank()) state.drivers else state.drivers.filter {
                        it.fullName.contains(searchQuery, ignoreCase = true) ||
                            it.licenseNumber.value.contains(searchQuery, ignoreCase = true) ||
                            it.phone.value.contains(searchQuery, ignoreCase = true)
                    }
                    items(filteredDrivers, key = { it.id.value }) { driver ->
                        val initials = driver.fullName.split(" ").filter { it.isNotBlank() }.take(2).map { it.first().uppercaseChar() }.joinToString("").ifBlank { "DR" }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandCardWhite),
                            elevation = CardDefaults.cardElevation(2.dp),
                            border = BorderStroke(1.dp, BrandBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(BrandNavy.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(initials, fontWeight = FontWeight.Bold, color = BrandNavy)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(driver.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrandNavy)
                                    Text(
                                        stringResource(R.string.stakeholders_driver_sub, driver.licenseNumber.toString(), driver.phone.toString()),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                StatusChip(text = stringResource(R.string.stakeholders_badge_enabled), containerColor = Color(0xFFD1FAE5), contentColor = Color(0xFF064E3B))
                            }
                        }
                    }
                }

                StakeholderTab.PARENTS -> {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SecondaryButton(stringResource(R.string.stakeholders_btn_register_parent), onRegisterParent, modifier = Modifier.weight(1f))
                            SecondaryButton(stringResource(R.string.stakeholders_btn_register_child), onRegisterChild, modifier = Modifier.weight(1f))
                        }
                    }
                    val filteredParents = if (searchQuery.isBlank()) state.parents else state.parents.filter {
                        it.fullName.contains(searchQuery, ignoreCase = true) ||
                            it.email.value.contains(searchQuery, ignoreCase = true) ||
                            it.phone.value.contains(searchQuery, ignoreCase = true) ||
                            it.children.any { child -> child.fullName.contains(searchQuery, ignoreCase = true) }
                    }
                    items(filteredParents, key = { it.id.value }) { parent ->
                        val initials = parent.fullName.split(" ").filter { it.isNotBlank() }.take(2).map { it.first().uppercaseChar() }.joinToString("").ifBlank { "PD" }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandCardWhite),
                            elevation = CardDefaults.cardElevation(2.dp),
                            border = BorderStroke(1.dp, BrandBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFEF3C7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(initials, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(parent.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrandNavy)
                                        Text(
                                            stringResource(R.string.stakeholders_parent_meta, parent.email.toString(), parent.phone.toString()),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                                if (parent.children.isNotEmpty()) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        parent.children.forEach { child ->
                                            StatusChip(
                                                text = stringResource(R.string.stakeholders_child_age, child.fullName, child.age),
                                                containerColor = Color(0xFFF1F5F9),
                                                contentColor = Color(0xFF334155)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                StakeholderTab.GROUPS -> {
                    item {
                        BigButton(
                            text = stringResource(R.string.stakeholders_btn_create_group),
                            onClick = onCreateGroup,
                            containerColor = BrandNavy,
                            icon = Icons.Filled.Add
                        )
                    }
                    val filteredGroups = if (searchQuery.isBlank()) state.groups else state.groups.filter {
                        it.name.contains(searchQuery, ignoreCase = true)
                    }
                    items(filteredGroups, key = { it.id.value }) { group ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandCardWhite),
                            elevation = CardDefaults.cardElevation(2.dp),
                            border = BorderStroke(1.dp, BrandBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(group.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrandNavy)
                                    Text(stringResource(R.string.stakeholders_group_students_count, group.studentIds.size), style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                                }
                                StatusChip(
                                    text = if (group.isFinalized) stringResource(R.string.stakeholders_group_finalized) else stringResource(R.string.stakeholders_group_draft),
                                    containerColor = if (group.isFinalized) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                                    contentColor = if (group.isFinalized) Color(0xFF064E3B) else Color(0xFF92400E)
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
