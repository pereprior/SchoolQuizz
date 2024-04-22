package com.orbys.quizz.ui.view.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.orbys.quizz.R
import com.orbys.quizz.data.constants.DOWNLOAD_ENDPOINT
import com.orbys.quizz.data.constants.SERVER_PORT
import com.orbys.quizz.data.constants.URL_ENTRY
import com.orbys.quizz.data.constants.hostIP
import com.orbys.quizz.databinding.FragmentDownloadBinding
import com.orbys.quizz.ui.components.QRCodeGenerator
import com.orbys.quizz.ui.services.FloatingViewService
import kotlin.system.exitProcess

/**
 * Fragmento que contiene los elementos para descargar el fichero con los resultados de la pregunta.
 */
class DownloadFragment: Fragment() {

    private lateinit var binding: FragmentDownloadBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDownloadBinding.inflate(inflater, container, false)
        val url = "$URL_ENTRY$hostIP:$SERVER_PORT$DOWNLOAD_ENDPOINT"

        with(binding) {
            val backButton: ImageButton = activity?.findViewById(R.id.back_button) ?: return root
            val closeButton: ImageButton = activity?.findViewById(R.id.close_button) ?: return root

            backButton.setOnClickListener {
                context?.startService(Intent(context, FloatingViewService::class.java))
                activity?.finish()
            }
            closeButton.setOnClickListener {
                activity?.finish()
                exitProcess(0)
            }

            qrCode.setImageBitmap(QRCodeGenerator().encodeAsBitmap(url))
            qrUrl.text = url

            return root
        }

    }

}