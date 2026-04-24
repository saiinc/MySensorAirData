package com.saionji.mysensor.shared.ui

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.popoverPresentationController

@OptIn(ExperimentalForeignApi::class)
actual class ShareManager {

    actual fun share(imageBitmap: ImageBitmap) {
        val image = imageBitmap.toUIImage() ?: return
        val presenter = topViewController() ?: return

        val activityVC = UIActivityViewController(
            activityItems = listOf(image),
            applicationActivities = null
        )

        // На iPad без sourceView/share sheet может падать
        activityVC.popoverPresentationController?.sourceView = presenter.view

        presenter.presentViewController(
            viewControllerToPresent = activityVC,
            animated = true,
            completion = null
        )
    }

    private fun topViewController(): UIViewController? {
        val keyWindow = UIApplication.sharedApplication.windows
            .firstOrNull { (it as? UIWindow)?.isKeyWindow() == true } as? UIWindow
            ?: return null

        var top = keyWindow.rootViewController ?: return null
        while (top.presentedViewController != null) {
            top = top.presentedViewController!!
        }
        return top
    }

    @OptIn(BetaInteropApi::class)
    private fun ImageBitmap.toUIImage(): UIImage? {
        val skiaImage = Image.makeFromBitmap(this.asSkiaBitmap())
        val pngData = skiaImage.encodeToData() ?: return null

        val nsData = pngData.bytes.usePinned { pinned ->
            NSData.create(
                bytes = pinned.addressOf(0),
                length = pngData.size.toULong()
            )
        }

        return UIImage.imageWithData(nsData)
    }
}