package com.lrm.accountant.fragments

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
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

    // Shared ViewModel
    private val accountsViewModel: AccountsViewModel by activityViewModels()

    // Late Initialization of No Internet Network Dialog
    private lateinit var networkDialog: Dialog

    // Lazily initializing the rotate animation which is to animate refresh button
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

        // This registers the network callbacks, to check the change in network
        accountsViewModel.checkForInternet(requireContext())

        // Initializing the network dialog
        networkDialog = Dialog(requireContext())
        networkDialog.apply {
            setCancelable(false)
            setContentView(R.layout.custom_dialog_network)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            findViewById<TextView>(R.id.go_settings_tv).setOnClickListener {
                val intent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
                startActivity(intent)
            }
        }

        // This will get the data from the Web Api
        getData()

        // Swipe down to refresh, this will get the data from the Web Api
        binding.swipeRefreshLayout.setOnRefreshListener {
            getData()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        // Setting the Adapter to the RecyclerView and pass the data to it
        val adapter = AccountsListAdapter(requireActivity(), requireContext(), accountsViewModel) {
            // Setting the account on which the user clicked on
            accountsViewModel.setAccountData(it)
            // Navigating to Account detail fragment
            val action = HomeFragmentDirections.actionHomeFragmentToAccountDetailFragment()
            this.findNavController().navigate(action)
        }
        binding.recyclerView.adapter = adapter
        accountsViewModel.accountsDataList.observe(viewLifecycleOwner) {list ->
            // Hide or Show the buttons and textview based on list's value
            if (list.isEmpty()) {
                binding.apply {
                    noRecordsTv.visibility = View.VISIBLE
                    recyclerView.visibility = View.INVISIBLE
                    totalAmount.visibility = View.INVISIBLE
                    exportPdf.visibility = View.INVISIBLE
                    exportPdfV2.visibility = View.INVISIBLE
                    exportExcel.visibility = View.INVISIBLE
                }
            } else {
                binding.apply {
                    noRecordsTv.visibility = View.INVISIBLE
                    recyclerView.visibility = View.VISIBLE
                    totalAmount.visibility = View.VISIBLE
                    exportPdf.visibility = View.VISIBLE
                    exportPdfV2.visibility = View.VISIBLE
                    exportExcel.visibility = View.VISIBLE
                }
                var amount = 0.0
                list.forEach { amount += it.amount }
                binding.totalAmount.text = HtmlCompat
                    .fromHtml(resources.getString(R.string.total_amount, amount.toString()), HtmlCompat.FROM_HTML_MODE_LEGACY)
                list.let { adapter.submitList(it) }
                Log.i(TAG, "onViewCreated: listData-> $list")
            }
        }

        binding.refreshButton.setOnClickListener {
            getData()
            binding.refreshButton.startAnimation(rotateAnim)
        }

        // This checks the permission first, then exports the file to App folder
        binding.exportPdf.setOnClickListener {
            if (hasWriteExternalStoragePermission()) {
                exportAsPdf()
            } else requestWriteExternalStoragePermission()
        }

        // This checks the permission first, then exports the file to App folder
        binding.exportPdfV2.setOnClickListener {
            if (hasWriteExternalStoragePermission()) {
                exportAsPdfV2()
            } else requestWriteExternalStoragePermission()
        }

        // This checks the permission first, then navigates to ScanDoc Fragment
        binding.scanFab.setOnClickListener {
            if (hasCameraPermission()) {
                val action = HomeFragmentDirections.actionHomeFragmentToScanDocFragment()
                this.findNavController().navigate(action)
            } else requestCameraPermission()
        }

        // Long Click Listener to show Developer Profile dialog
        binding.appIcon.setOnLongClickListener {
            showDeveloperInfoDialog()
            true
        }

        // Long Click Listener to show Developer Profile dialog
        binding.appName.setOnLongClickListener {
            showDeveloperInfoDialog()
            true
        }

        // This checks the permission first, then exports the file to App folder
        binding.exportExcel.setOnClickListener {
            if (hasWriteExternalStoragePermission()) {
                exportAsExcel()
            } else requestWriteExternalStoragePermission()
        }

        accountsViewModel.onlineStatus.observe(viewLifecycleOwner) { status ->
            // Show or hide network dialog based on the status observed from LiveData
            if (!status) {
                networkDialog.show()
            } else {
                networkDialog.dismiss()
                getData()
            }
        }
    }

    private fun getData() {
        if (accountsViewModel.isOnline(requireContext())) {
            accountsViewModel.getData()
        } else networkDialog.show()
    }

    // This function exports the excel to our App folder
    private fun exportAsExcel() {
        val excelExport = ExcelExport()
        val data = accountsViewModel.accountsDataList.value!!
        val workbook = excelExport.createWorkbook(data)
        excelExport.createExcel(requireContext(), workbook)
    }

    // This function exports the PDF v2 to our App folder
    private fun exportAsPdfV2() {
        val pdfConverter = PdfConverter()
        pdfConverter.createPdf(requireActivity(), requireContext(), accountsViewModel.accountsDataList.value!!)
    }

    // This function exports the PDF to our App folder
    private fun exportAsPdf() {
        val onError: (Exception) -> Unit = { Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_SHORT).show()}
        val onFinish: (File) -> Unit = { Toast.makeText(requireContext(), "PDF exported successfully", Toast.LENGTH_SHORT).show() }
        val pdfExport = PdfExport()
        pdfExport.createUserTable(accountsViewModel.accountsDataList.value!!.toList(), onFinish, onError)
    }

    // This checks the Permission
    private fun hasWriteExternalStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else true
    }

    // This requests the Permission
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

    // This checks the Permission
    private fun hasCameraPermission(): Boolean {
        return EasyPermissions.hasPermissions(requireContext(), Manifest.permission.CAMERA)
    }

    // This requests the Permission
    private fun requestCameraPermission() {
        EasyPermissions.requestPermissions(
            this,
            "Permission is required to make call",
            CAMERA_PERMISSION_CODE,
            Manifest.permission.CAMERA
        )
    }

    // Easy Permissions Callback function
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.permissionPermanentlyDenied(this, perms.first())) {
            SettingsDialog.Builder(requireContext()).build().show()
        }
    }

    // Easy Permissions Callback function
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

    // This shows the Developer Profile dialog
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