package com.lrm.accountant.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.lrm.accountant.R
import com.lrm.accountant.adapter.AccountsListAdapter
import com.lrm.accountant.databinding.FragmentHomeBinding
import com.lrm.accountant.viewmodel.AccountsViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val accountsViewModel: AccountsViewModel by activityViewModels()

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

        val adapter = AccountsListAdapter(accountsViewModel) {
            Toast.makeText(requireContext(), "Item clicked", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerView.adapter = adapter
        accountsViewModel.accountsDataList.observe(viewLifecycleOwner) {list ->
            list.let { adapter.submitList(it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}