package org.maplibre.navigation.android.navigation.v5.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

object MapImageUtils {

    @JvmStatic
    fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        return (drawable as? BitmapDrawable)?.bitmap
            ?: run {
                val bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )

                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
    }
}
