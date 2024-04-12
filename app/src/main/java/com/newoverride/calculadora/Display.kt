package com.newoverride.calculadora

import android.content.Context
import android.widget.TextView

interface Display {

    interface Presenter {
        fun display(value: String)
        fun clearDisplay(value: String)
        fun removeLast()
        fun findZero(value: String)
        fun comma(value: String)
        fun countDigits()
        fun autoScore()
        fun addOperation(value: String)
        fun calculateResult()
        fun startAnimationTextSize(
            value: Float,
            context: Context,
            captureID: TextView,
            targetSize: Float
        )
        fun startSpringAnimationView(view: android.view.View)
    }

    interface View {
        fun showValue(value: String)
        fun animateTextSize(currentLength: Int, targetSize: Float)
        fun showResult(value: String)
        fun springAnimationView(view: android.view.View)
    }
}