/*
 * Copyright © Anton Sorokin 2024. All rights reserved
 */

package com.saionji.mysensor.data

import androidx.compose.ui.graphics.Color

data class MySensor(
    var valueType : String?,
    var value     : String?,
    var color     : Color = Color.Transparent
)