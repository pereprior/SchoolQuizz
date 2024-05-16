package com.orbys.vote.ui.view.fragments.add

import com.orbys.vote.R
import com.orbys.vote.domain.models.Answer
import com.orbys.vote.domain.models.AnswerType
import com.orbys.vote.domain.models.Question
import kotlinx.coroutines.flow.MutableStateFlow

class AddBooleanQuestion : AddFragment() {

    override val answerType = AnswerType.BOOLEAN

    override fun createQuestionFromInput() = Question(
        question = binding.questionQuestion.text.toString(),
        answers = MutableStateFlow(
            listOf(
                Answer(this.getString(R.string.true_answer_placeholder)),
                Answer(this.getString(R.string.false_answer_placeholder))
            )
        ),
        answerType = answerType,
        isAnonymous = binding.anonymousQuestionOption.isChecked,
        timeOut = binding.timeoutInput.text.toString().toIntOrNull() ?: 0
    )

}