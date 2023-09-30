package com.lrm.accountant.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.lrm.accountant.R
import com.lrm.accountant.activities.ScanDocActivity
import com.lrm.accountant.constants.TAG
import com.lrm.accountant.databinding.FragmentScanDocBinding
import com.lrm.accountant.utils.PdfConverter
import com.lrm.accountant.utils.createDirectory

class ScanDocFragment : Fragment() {

    private var _binding: FragmentScanDocBinding? = null
    private val binding get() = _binding!!

    private var uri = Uri.EMPTY

    private val imageUri = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            uri = intent?.getStringExtra("IMAGE_URI")?.toUri()
            Log.i(TAG, "activityResult: uri-> $uri")
            if (uri != null || uri != Uri.EMPTY) {
                binding.scannedImage.setImageURI(uri)
                binding.exportPdf.visibility = View.VISIBLE
            } else {
                binding.scannedImage.setImageResource(R.drawable.box_bg)
                binding.exportPdf.visibility = View.INVISIBLE
            }
        } else {
            binding.scannedImage.setImageResource(R.drawable.box_bg)
            binding.exportPdf.visibility = View.INVISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanDocBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backIcon.setOnClickListener { this.findNavController().navigateUp() }

        binding.scanButton.setOnClickListener {
            imageUri.launch(Intent(requireActivity(), ScanDocActivity::class.java))
        }

        binding.exportPdf.setOnClickListener { exportAsPdf() }
    }

    private fun exportAsPdf() {
        createDirectory()
        val pdfConverter = PdfConverter()
        if (uri != Uri.EMPTY) {
            val bitmap = BitmapFactory.decodeStream(requireActivity().contentResolver.openInputStream(uri))
            pdfConverter.convertBitmapToPdf(bitmap, requireContext(), "Scanned Doc")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}