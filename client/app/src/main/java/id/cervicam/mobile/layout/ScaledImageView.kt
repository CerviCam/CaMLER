package id.cervicam.mobile.layout

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import kotlin.math.ceil


class ScaledImageView(
    context: Context?,
    attrs: AttributeSet?
) : ImageView(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val d: Drawable? = drawable
        if (d != null) {
            val width: Int
            val height: Int
            if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
                height = MeasureSpec.getSize(heightMeasureSpec)
                width = ceil(height * d.intrinsicWidth.toFloat() / d.intrinsicHeight.toDouble()).toInt()
            } else {
                width = MeasureSpec.getSize(widthMeasureSpec)
                height = ceil(width * d.intrinsicHeight.toFloat() / d.intrinsicWidth.toDouble()).toInt()
            }
            setMeasuredDimension(width, height)
            super.onMeasure(width, height)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }
}