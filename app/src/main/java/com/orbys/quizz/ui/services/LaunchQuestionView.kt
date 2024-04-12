package com.orbys.quizz.ui.services

import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.WindowManager
import com.orbys.quizz.R
import com.orbys.quizz.data.constants.ENDPOINT
import com.orbys.quizz.data.constants.SERVER_PORT
import com.orbys.quizz.data.constants.URL_ENTRY
import com.orbys.quizz.data.constants.host
import com.orbys.quizz.databinding.ServiceLaunchQuestionBinding
import com.orbys.quizz.domain.models.Bar
import com.orbys.quizz.domain.repositories.QuestionRepositoryImpl
import com.orbys.quizz.ui.MainActivity
import com.orbys.quizz.ui.components.utils.QRCodeGenerator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LaunchQuestionView : ConstraintLayout, View.OnTouchListener {
    // Variables para gestionar la vista
    private var windowManager: WindowManager
    private lateinit var layoutParams: WindowManager.LayoutParams
    private var binding: ServiceLaunchQuestionBinding

    private val repository = QuestionRepositoryImpl.getInstance()

    // Coordenadas del widget
    private var x: Int = 0
    private var y: Int = 0
    private var onChangeX: Float = 0f
    private var onChangeY: Float = 0f

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        inflateLayoutParamsAsFloatingLayout()
        binding = ServiceLaunchQuestionBinding.inflate(LayoutInflater.from(context))
        bindOnQuestion()

        addView(binding.root)
        setOnTouchListener(this)

        layoutParams.x = x
        layoutParams.y = y

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(this, layoutParams)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {

            MotionEvent.ACTION_DOWN -> {
                x = layoutParams.x
                y = layoutParams.y
                onChangeX = event.rawX
                onChangeY = event.rawY
            }

            //Change the position of the widget
            MotionEvent.ACTION_MOVE -> {
                layoutParams.x = (x + event.rawX - onChangeX).toInt()
                layoutParams.y = (y + event.rawY - onChangeY).toInt()
                windowManager.updateViewLayout(this, layoutParams)
            }

        }

        return true
    }

    private fun inflateLayoutParamsAsFloatingLayout() {
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
    }

    private fun bindOnQuestion() {
        val question = repository.question.value
        val  url = "$URL_ENTRY$host:$SERVER_PORT$ENDPOINT/${question.id}"

        with(binding) {

            questionTypeIcon.setImageResource(question.icon)

            // Establece la imagen del código QR.
            qrCode.setImageBitmap(QRCodeGenerator().encodeAsBitmap(url))

            // Establece el texto de la pregunta.
            this.question.text = question.question

            // Establece el evento de clic en el botón de cerrar.
            closeButton.setImageResource(R.drawable.baseline_arrow_forward_24)
            closeButton.setOnClickListener {
                windowManager.removeView(this@LaunchQuestionView)

                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }

            // Lanza una corrutina para recoger los recuentos de respuestas y establecerlos en la interfaz de usuario.
            question.answers.forEach { answer ->
                val bar = Bar(answer.answer.toString(), height = answer.count.value)
                barView.addBar(bar)

                GlobalScope.launch {
                    // Recoge los cambios en count para cada respuesta
                    answer.count.collect { newCount ->
                        bar.height = newCount
                        barView.invalidate()
                    }
                }
            }

        }

    }

}