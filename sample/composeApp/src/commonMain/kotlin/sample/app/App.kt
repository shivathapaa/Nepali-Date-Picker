/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package sample.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * One tab of the showcase. Each tab owns a full-featured demo screen so the sample can grow without
 * turning into a single unmanageable scroll.
 */
private class ShowcaseTab(val title: String, val screen: @Composable (Modifier) -> Unit)

private val showcaseTabs = listOf(
    ShowcaseTab("Pickers") { PickersShowcase(it) },
    ShowcaseTab("Wheel & Docked") { WheelDockedShowcase(it) },
    ShowcaseTab("Dialogs") { DialogsShowcase(it) },
    ShowcaseTab("Text fields") { TextFieldsShowcase(it) },
    ShowcaseTab("Customization") { CustomizationShowcase(it) },
    ShowcaseTab("Selectable dates") { SelectableDatesShowcase(it) },
    ShowcaseTab("Utilities") { UtilitiesShowcase(it) }
)

@Composable
fun App() {
    MaterialTheme {
        var selectedTab by rememberSaveable { mutableIntStateOf(0) }
        Scaffold(
            topBar = { TopAppBar(title = { Text("Nepali Date Picker") }) }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 12.dp) {
                    showcaseTabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(tab.title) }
                        )
                    }
                }
                showcaseTabs[selectedTab].screen(Modifier)
            }
        }
    }
}
