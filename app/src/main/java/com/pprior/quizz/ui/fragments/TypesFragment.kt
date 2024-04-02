package com.pprior.quizz.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pprior.quizz.databinding.FragmentTypesBinding
import com.pprior.quizz.ui.activities.dialogs.AddQuestionActivity

class TypesFragment : Fragment() {

    private lateinit var binding: FragmentTypesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTypesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setQuestionTypesCards()
    }

    private fun setQuestionTypesCards() {
        binding.yesNoCard.setOnClickListener { startAddQuestionActivity() }
        binding.starCard.setOnClickListener { startAddQuestionActivity() }
        binding.barCard.setOnClickListener { startAddQuestionActivity() }
        binding.otherCard.setOnClickListener { startAddQuestionActivity() }
    }

    private fun startAddQuestionActivity() {
        val intent = Intent(context, AddQuestionActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

}