package com.pprior.quizz.ui.components.dialogs

import android.content.Context
import com.pprior.quizz.data.flow.FlowRepository
import com.pprior.quizz.data.models.Question

/**
 * Clase que representa un dialogo para editar preguntas de la lista.
 *
 * @param viewModel El viewmodel que gestiona las preguntas.
 * @param context El contexto del dialogo.
 * @param question La pregunta a editar.
 */
class EditQuestionDialog(
    private val flowRepository: FlowRepository,
    context: Context,
    private val question: Question
) : AddQuestionDialog(flowRepository, context) {

    init {
        with(binding) {
            // Rellenar los campos de texto con los datos de la pregunta
            questionTitle.setText(question.title)
            questionQuestion.setText(question.question)
        }
    }

    override fun saveQuestion() {
        val updatedQuestion = Question(
            binding.questionTitle.text.toString(),
            binding.questionQuestion.text.toString()
        )

        // Comprobar si la pregunta o el título están vacíos
        if (updatedQuestion.title.isEmpty() || updatedQuestion.question.isEmpty()) {
            return
        }

        // Actualizar la pregunta en la lista y cerrar el dialogo
        flowRepository.updateQuestion(question, updatedQuestion)
        dismiss()
    }
}