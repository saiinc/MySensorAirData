package com.saionji.mysensor.ui

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import android.util.TypedValue

fun errorPopUp(
    context: Context,
    message: String,
    backgroundColor: Int,
    textColor: Int,
    strokeColor: Int
) {
    val toast = Toast(context)
    val textView = TextView(context).apply {
        text = message
        setTextColor(textColor)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        setPadding(32, 16, 32, 16)
        background = GradientDrawable().apply {
            cornerRadius = 24f
            setColor(backgroundColor)
            setStroke(2, strokeColor)
        }
        gravity = Gravity.CENTER
    }

    toast.view = textView
    toast.duration = Toast.LENGTH_SHORT
    toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 150)
    toast.show()
}