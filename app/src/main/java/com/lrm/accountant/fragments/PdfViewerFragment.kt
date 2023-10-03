package com.lrm.accountant.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.lrm.accountant.constants.TAG
import com.lrm.accountant.databinding.FragmentPdfViewerBinding
import java.io.File

class PdfViewerFragment : Fragment() {

    private var _binding: FragmentPdfViewerBinding? = null
    private val binding get() = _binding!!

    private val navArgs: PdfViewerFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPdfViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backIcon.setOnClickListener { this.findNavController().navigateUp() }

        val filePath = navArgs.filePath
        Log.i(TAG, "onViewCreated: PDF Viewer -> filePath -> $filePath")
        val file = File(filePath)
        Log.i(TAG, "onViewCreated: PDF Viewer -> file -> $file")
        val pdfFile = FileProvider.getUriForFile(requireContext(), requireActivity().applicationContext.packageName + ".provider", file)
        Log.i(TAG, "onViewCreated: PDF Viewer -> pdfFile -> $pdfFile")
        binding.pdfView.fromUri(pdfFile).load()

        binding.fileName.text = filePath.substring(filePath.lastIndexOf("/") + 1)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}