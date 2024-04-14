package com.newoverride.calculadora.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.newoverride.calculadora.Display
import com.newoverride.calculadora.R
import com.newoverride.calculadora.databinding.HomeViewBinding
import com.newoverride.calculadora.presenter.DisplayPresenter

class Home : AppCompatActivity(), Display.View {

    private var binding: HomeViewBinding? = null
    private var presenter: Display.Presenter? = null

    // SERVE PARA PASSAR O RESULTADO NO DISPLAY EXCLUINDO A OPERAÇÃO!
    companion object {
        var activeResult = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // INFLANDO LAYOUT!
        binding = HomeViewBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        // PASSANDO O CONTEXTO PARA VIEW!
        presenter = DisplayPresenter(this)

        // ATIVANDO FUNÇÃO ESCUTAR!
        escutarTeclado()
    }

    // EXIBIR NO DISPLAY OS VALORES!
    override fun showValue(value: String) {
        binding!!.txtDisplayValue.text = value
    }

    // FAZ ANIMAÇÃO NO TEXTO DO DISPLAY, DIMINUINDO O TAMANHO PARA NÃO DAR QUEBRA DE LINHA!
    // MAS QUANDO OS VALORES SÃO REMOVIDOS RETORNA AO TAMANHO ORIGINAL!
    override fun animateTextSize(currentLength: Int, targetSize: Float) {
        val captureSize = binding!!.txtDisplayValue.textSize
        val captureID = binding!!.txtDisplayValue
        // PASSA A RESPONSABILIDADE PARA O PRESENTER PARA A LÓGICA DA ANIMAÇÃO
        presenter!!.startAnimationTextSize(captureSize, this, captureID, targetSize)
    }

    // PASSA O RESULTADO PARA O DISPLAY ABAIXO!
    override fun showResult(value: String) {
        binding!!.txtResult.text = value
    }

    // PASSA RESPONSABILIDADE PARA O PRESENTER INICIAR A ANIMAÇÃO!
    // FAZ UMA ANIMAÇÃO DE MOLA EM BOTÕES DO TECLADO!
    override fun springAnimationView(view: View) {
        presenter!!.startSpringAnimationView(view)
    }

    // DESTRUINDO VARIÁVEIS DE CAMPO AO FECHAR O APP!
    override fun onDestroy() {
        binding = null
        presenter = null
        super.onDestroy()
    }

    // ESCUTANDO TECLADO!
    private fun escutarTeclado() {
        with(binding!!) {
            btnExit.setOnClickListener {
                springAnimationView(it)
                finish()
            }
            btnOne.setOnClickListener {
                springAnimationView(it)
                presenter!!.display(getString(R.string.one))
            }
            btnClear.setOnClickListener {
                springAnimationView(it)
                presenter!!.clearDisplay(getString(R.string.clean))
            }
            btnBack.setOnClickListener {
                springAnimationView(it)
                presenter!!.removeLast()
            }
            btnZero.setOnClickListener {
                springAnimationView(it)
                presenter!!.findZero(getString(R.string.zero))
            }
            btnTwo.setOnClickListener {
                springAnimationView(it)
                presenter!!.display(getString(R.string.two))
            }
            btnThree.setOnClickListener {
                springAnimationView(it)
                presenter!!.display(getString(R.string.three))
            }
            btnFour.setOnClickListener {
                springAnimationView(it)
                presenter!!.display(getString(R.string.four))
            }
            btnFive.setOnClickListener {
                springAnimationView(it)
                presenter!!.display(getString(R.string.five))
            }
            btnSix.setOnClickListener {
                springAnimationView(it)
                presenter!!.display(getString(R.string.six))
            }
            btnSeven.setOnClickListener {
                springAnimationView(it)
                presenter!!.display(getString(R.string.seven))
            }
            btnEight.setOnClickListener {
                springAnimationView(it)
                presenter!!.display(getString(R.string.eight))
            }
            btnNine.setOnClickListener {
                springAnimationView(it)
                presenter!!.display(getString(R.string.nine))
            }
            btnComma.setOnClickListener {
                springAnimationView(it)
                presenter!!.comma(getString(R.string.comma))
            }
            btnAddition.setOnClickListener {
                springAnimationView(it)
                presenter!!.addOperation(getString(R.string.addition))
            }
            btnSubtraction.setOnClickListener {
                springAnimationView(it)
                presenter!!.addOperation(getString(R.string.subtraction))
            }
            btnDivision.setOnClickListener {
                springAnimationView(it)
                presenter!!.addOperation(getString(R.string.division))
            }
            btnPercent.setOnClickListener {
                springAnimationView(it)
                presenter!!.addOperation(getString(R.string.percent))
            }
            btnMultiple.setOnClickListener {
                springAnimationView(it)
                presenter!!.addOperation(getString(R.string.multiple))
            }
            btnEqual.setOnClickListener {
                springAnimationView(it)
                activeResult = !activeResult
                presenter!!.calculateResult()
            }
        }
    }
}