package com.newoverride.calculadora.presenter

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.newoverride.calculadora.Display
import com.newoverride.calculadora.view.Home
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

class DisplayPresenter(

    private val view: Display.View

) : Display.Presenter {
    // VARIÁVEIS DE CAMPO!
    private var currentValue: String = ""
    private var digitsOnly: String = ""
    private var targetTextSize: Float = 50f

    // FUNÇÃO PARA AUTO-PONTUAR OS VALORES DO DISPLAY!
    override fun autoScore() {
        val regex = "(?<=op)|(?=op)".replace("op", "[+\\-x÷%]")
        val result = currentValue.split(regex.toRegex()).map { it.trim() }
        val newList = mutableListOf<String>()

        for (part in result) {
            if (part !in listOf("+", "-", "x", "÷", "%")) {
                if (!part.matches(Regex("\\d{1,3}(\\.\\d{3})*(,\\d+)?"))) {
                    // REFORMATAR APENAS SE A PARTE NÃO ESTÁ NO FORMATO DE NÚMERO COM SEPARADORES DE MILHAR!
                    // E PONTENCIAL SEPARADOR DECIMAL COM VÍRGULA!
                    val autoDot = part.filter { it.isDigit() }
                        .reversed()
                        .chunked(3)
                        .joinToString(".")
                        .reversed()
                    newList.add(autoDot)
                } else {
                    // SE JÁ ESTÁ NO FORMATO, APENAS ADICIONE À LISTA!
                    newList.add(part)
                }
            } else {
                newList.add(part)
            }
        }
        currentValue = newList.joinToString("")
        calculateResult()
        view.showValue(currentValue)
    }

    // VERIFICA A TECLA DIGITADA E ADICIONA NO DISPLAY!
    // OBS: NÚMEROS DE 1 A 9!
    override fun display(value: String) {
        val regex = "(x0|÷0|\\+0|-0|%0)".toRegex()
        val result = regex.findAll(currentValue).map { it.value }.toList()
        if (currentValue.takeLast(2) == result.joinToString("").takeLast(2)) {
            currentValue = currentValue.dropLast(1)
            currentValue += value
        } else {
            currentValue += value
        }
        countDigits()
        autoScore()
    }

    // APAGA TODOS OS DÍGITOS DO DISPLAY!
    override fun clearDisplay(value: String) {
        digitsOnly = value
        currentValue = value
        view.showValue(currentValue)
        view.showResult(currentValue)
    }

    // REMOVE O ÚLTIMO DÍGITO!
    override fun removeLast() {
        if (currentValue.isNotEmpty()) {
            countDigits()
            currentValue = currentValue.dropLast(1)
            digitsOnly.length - 1
            view.showResult(currentValue)
        } else {
            view.showResult(currentValue)
        }
        autoScore()
    }

    // GERÊNCIA DE ZEROS NO DISPLAY!
    override fun findZero(value: String) {
        if (currentValue == "" || currentValue == "0") {
            countDigits()
            view.showValue(value)
        } else {
            val regex = "(x0|÷0|\\+0|-0|%0)".toRegex()
            val result = regex.findAll(currentValue).map { it.value }.toList()
            if (currentValue.takeLast(2) != result.joinToString("").takeLast(2)) {
                currentValue += value
            }
            countDigits()
            autoScore()
        }
    }

    // GERÊNCIA DE VÍRGULAS!
    override fun comma(value: String) {
        countDigits()
        var confirmComma = false
        if (currentValue == "" || currentValue == "0") {
            countDigits()
            currentValue = "0$value"
            view.showValue(currentValue)
        } else {
            val regex = "(?<=op)|(?=op)".replace("op", "[+\\-x÷%]")
            val result = currentValue.split(regex.toRegex()).map { it.trim() }
            for (part in result) {
                confirmComma = !part.contains(",") && currentValue.last().isDigit()
            }
            if (confirmComma) {
                currentValue += value
            }
            view.showValue(currentValue)
            countDigits()
        }
    }

    // GERÊNCIA DO TOTAL DE NÚMEROS QUE HÁ NO DISPLAY CONSIDERANDO APENAS DÍGITOS!
    // SE HOUVER MAIS DE 15 ELE REMOVE O ÚLTIMO!
    override fun countDigits() {
        digitsOnly = currentValue.filter { it.isDigit() }
        if (digitsOnly.length > 15) {
            currentValue = currentValue.dropLast(1)
        }
        if (currentValue.length >= 10) {
            if (targetTextSize > 30f) {
                targetTextSize = max(30f, targetTextSize - 5f)
                view.animateTextSize(currentValue.length, targetTextSize)
            }
        } else if (targetTextSize < 50f) {
            targetTextSize = min(50f, targetTextSize + 30f)
            view.animateTextSize(currentValue.length, targetTextSize)
        }
    }

    // ADICIONA UM OPERADOR MATEMÁTICO!
    override fun addOperation(value: String) {
        if (currentValue.isNotEmpty()) {
            val onlyDigit = currentValue.filter { it.isDigit() }
            if (currentValue.last().isDigit() && onlyDigit.length < 15) {
                Log.i("Teste", "dwqdqwd")
                currentValue += value
                countDigits()
                calculateResult()
                view.showValue(currentValue)
            } else {
                countDigits()
                if (!currentValue.last().isDigit() && currentValue.last() in listOf(
                        '+',
                        '-',
                        'x',
                        '÷',
                    )
                ) {
                    currentValue = currentValue.dropLast(1) + value
                    if (currentValue.takeLast(1) == "%") {
                        currentValue = currentValue.dropLast(1)
                    }
                    view.showValue(currentValue)
                } else {
                    currentValue += value
                    if (currentValue.last() == '%') {
                        currentValue = currentValue.dropLast(1)
                    }
                    view.showValue(currentValue)
                }
            }
        }
    }

    // CALCULA O RESULTADO!
    override fun calculateResult() {
        if (currentValue.isEmpty()) {
            return
        }
        val regex = "([+x\\-%÷]|\\d+\\.?\\d*)"
        val fieldDotComma = currentValue.replace(".", "").replace(",", ".")
        val elementos = regex.toRegex().findAll(fieldDotComma).map { it.value }.toList()
        var total = elementos.first().toDouble()
        var lastOperation = "+"
        for (index in 1 until elementos.size) {
            val part = elementos[index]

            if (part in listOf("+", "-", "x", "%", "÷")) {
                lastOperation = part
            } else {
                if (index + 1 < elementos.size && elementos[index + 1] == "%") {
                    when (lastOperation) {
                        "+" -> {
                            total += total * (part.toDouble() / 100)
                        }

                        "-" -> {
                            total -= total * (part.toDouble() / 100)
                        }

                        "x" -> {
                            total *= (part.toDouble() / 100)
                        }

                        "÷" -> {
                            total /= (part.toDouble() / 100)
                        }
                    }
                    // PULAR O "%" DEPOIS DE TRATAR A PERCENTAGEM!
                    continue
                }
                val number = part.toDouble()
                when (lastOperation) {
                    "+" -> total += number
                    "-" -> total -= number
                    "x" -> total *= number
                    "÷" -> if (number == 0.0) {
                        view.showResult("∞")
                        return
                    } else {
                        total /= number
                    }
                }
            }
        }
        val formattedResult = NumberFormat.getNumberInstance(Locale("pt", "BR")).format(total)
        if (Home.activeResult) {
            currentValue = formattedResult
            view.showValue(currentValue)
        }
        view.showResult(formattedResult)
        view.showValue(formattedResult)
        Home.activeResult = false
    }

    // INICIA A ANIMAÇÃO NA VIEW!
    override fun startAnimationTextSize(
        value: Float,
        context: Context,
        captureID: TextView,
        targetSize: Float
    ) {
        val animator = ValueAnimator.ofFloat(
            captureID.textSize / context.resources.displayMetrics.scaledDensity,
            targetSize
        ).apply {
            duration = 300
            addUpdateListener {
                val animatedValue = it.animatedValue as Float
                captureID.textSize = animatedValue
            }
        }
        animator.start()
    }

    // PASSA RESPONSABILIDADE PARA O PRESENTER INICIAR A ANIMAÇÃO!
    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("ServiceCast")
    override fun startSpringAnimationView(view: View) {
        val springAnimationX = SpringAnimation(view, DynamicAnimation.SCALE_X, 1f).apply {
            spring.dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
            spring.stiffness = SpringForce.STIFFNESS_LOW
        }
        val springAnimationY = SpringAnimation(view, DynamicAnimation.SCALE_Y, 1f).apply {
            spring.dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
            spring.stiffness = SpringForce.STIFFNESS_LOW
        }
        view.scaleX = 0.9f
        view.scaleY = 0.9f
        springAnimationX.start()
        springAnimationY.start()

        // ADICIONAR VIBRAÇÃO AO PRESSIONAR QUALQUER BOTÃO!
        // OBTENHA O SERVIÇO DE VIBRAÇÃO!
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                view.context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECIATION")
            view.context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as Vibrator
        }

        // VERIFIQUE A VERSÃO DO ANDROID, POIS A API DE VIBRAÇÃO MUDOU NO ANDROID OREO (API 26)!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(40)
        }
    }
}