/*
 * Copyright © Anton Sorokin 2024. All rights reserved
 */

package com.saionji.mysensor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.saionji.mysensor.ui.SensorsApp
import com.saionji.mysensor.ui.theme.MyFirstAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyFirstAppTheme {
                // A surface container using the 'background' color from the theme
                SensorsApp()
            }
        }
    }
}

