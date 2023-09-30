package com.lrm.accountant.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.lrm.accountant.R
import com.lrm.accountant.adapter.AccountsListAdapter
import com.lrm.accountant.constants.CAMERA_PERMISSION_CODE
import com.lrm.accountant.constants.TAG
import com.lrm.accountant.constants.WRITE_EXTERNAL_STORAGE_PERMISSION_CODE
import com.lrm.accountant.databinding.FragmentHomeBinding
import com.lrm.accountant.utils.ExcelExport
import com.lrm.accountant.utils.PdfConverter
import com.lrm.accountant.utils.PdfExport
import com.lrm.accountant.viewmodel.AccountsViewModel
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class HomeFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val accountsViewModel: AccountsViewModel by activityViewModels()
    private val rotateAnim: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_anim)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.blue)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefreshLayout.setOnRefreshListener {
            accountsViewModel.getData()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        val adapter = AccountsListAdapter(requireActivity(), requireContext(), accountsViewModel) {
            accountsViewModel.setAccountData(it)
            val action = HomeFragmentDirections.actionHomeFragmentToAccountDetailFragment()
            this.findNavController().navigate(action)
        }
        binding.recyclerView.adapter = adapter
        accountsViewModel.accountsDataList.observe(viewLifecycleOwner) {list ->
            if (list.isEmpty()) {
                binding.noRecordsTv.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.INVISIBLE
                binding.totalAmount.visibility = View.INVISIBLE
                binding.exportPdf.visibility = View.INVISIBLE
                binding.exportPdfV2.visibility = View.INVISIBLE
                binding.exportExcel.visibility = View.INVISIBLE
            } else {
                binding.noRecordsTv.visibility = View.INVISIBLE
                binding.recyclerView.visibility = View.VISIBLE
                binding.totalAmount.visibility = View.VISIBLE
                binding.exportPdf.visibility = View.VISIBLE
                binding.exportPdfV2.visibility = View.VISIBLE
                binding.exportExcel.visibility = View.VISIBLE
                var amount = 0.0
                list.forEach { amount += it.amount }
                binding.totalAmount.text = HtmlCompat
                    .fromHtml(resources.getString(R.string.total_amount, amount.toString()), HtmlCompat.FROM_HTML_MODE_LEGACY)
                list.let { adapter.submitList(it) }
                Log.i(TAG, "onViewCreated: listData-> $list")
            }
        }

        binding.refreshButton.setOnClickListener {
            accountsViewModel.getData()
            binding.refreshButton.startAnimation(rotateAnim)
        }

        binding.exportPdf.setOnClickListener {
            if (hasWriteExternalStoragePermission()) {
                exportAsPdf()
            } else requestWriteExternalStoragePermission()
        }

        binding.exportPdfV2.setOnClickListener {
            if (hasWriteExternalStoragePermission()) {
                exportAsPdfV2()
            } else requestWriteExternalStoragePermission()
        }

        binding.scanFab.setOnClickListener {
            if (hasCameraPermission()) {
                val action = HomeFragmentDirections.actionHomeFragmentToScanDocFragment()
                this.findNavController().navigate(action)
            } else requestCameraPermission()
        }

        binding.appIcon.setOnLongClickListener {
            showDeveloperInfoDialog()
            true
        }

        binding.appName.setOnLongClickListener {
            showDeveloperInfoDialog()
            true
        }

        binding.exportExcel.setOnClickListener {
            exportAsExcel()
        }
    }

    private fun exportAsExcel() {
        val excelExport = ExcelExport()
        val data = accountsViewModel.accountsDataList.value!!
        val workbook = excelExport.createWorkbook(data)
        excelExport.createExcel(requireContext(), workbook)
    }

    private fun exportAsPdfV2() {
        val pdfConverter = PdfConverter()
        pdfConverter.createPdf(requireActivity(), requireContext(), accountsViewModel.accountsDataList.value!!)
    }

    private fun exportAsPdf() {
        val onError: (Exception) -> Unit = { Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_SHORT).show()}
        val onFinish: (File) -> Unit = { Toast.makeText(requireContext(), "PDF exported successfully", Toast.LENGTH_SHORT).show() }
        val pdfExport = PdfExport()
        pdfExport.createUserTable(accountsViewModel.accountsDataList.value!!.toList(), onFinish, onError)
    }

    private fun hasWriteExternalStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else true
    }

    private fun requestWriteExternalStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "Permission is required to record audio",
                WRITE_EXTERNAL_STORAGE_PERMISSION_CODE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    private fun hasCameraPermission(): Boolean {
        return EasyPermissions.hasPermissions(requireContext(), Manifest.permission.CAMERA)
    }

    private fun requestCameraPermission() {
        EasyPermissions.requestPermissions(
            this,
            "Permission is required to make call",
            CAMERA_PERMISSION_CODE,
            Manifest.permission.CAMERA
        )
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.permissionPermanentlyDenied(this, perms.first())) {
            SettingsDialog.Builder(requireContext()).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun showDeveloperInfoDialog() {
        val dialogView = requireActivity().layoutInflater.inflate(R.layout.custom_developer_info, null)
        val imageLink = "https://firebasestorage.googleapis.com/v0/b/gdg-vizag-f9bf0.appspot.com/o/gdg_vizag%2Fdeveloper%2FRammohan_L_pic.png?alt=media&token=6e55ba28-e0ca-45c6-b50b-be1955da2566"
        val devImage = dialogView.findViewById<CircleImageView>(R.id.dev_image)
        Glide.with(requireContext()).load(imageLink).placeholder(R.drawable.loading_icon_anim).into(devImage)

        val devGithubLink = dialogView.findViewById<CircleImageView>(R.id.dev_github_link)
        val devYoutubeLink = dialogView.findViewById<CircleImageView>(R.id.dev_youtube_link)

        devGithubLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/JungleMystic"))
            startActivity(intent)
        }

        devYoutubeLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com/@junglemystic"))
            startActivity(intent)
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        builder.setCancelable(true)

        val developerDialog = builder.create()
        developerDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        developerDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}