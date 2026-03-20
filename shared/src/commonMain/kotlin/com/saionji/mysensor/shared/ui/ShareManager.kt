package com.saionji.mysensor.shared.ui

import androidx.compose.ui.graphics.ImageBitmap

expect class ShareManager {
    fun share(imageBitmap: ImageBitmap)
}