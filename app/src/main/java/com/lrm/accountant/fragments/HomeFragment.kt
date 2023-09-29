package com.lrm.accountant.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.lrm.accountant.R
import com.lrm.accountant.adapter.AccountsListAdapter
import com.lrm.accountant.constants.TAG
import com.lrm.accountant.databinding.FragmentHomeBinding
import com.lrm.accountant.viewmodel.AccountsViewModel

class HomeFragment : Fragment() {

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

        val adapter = AccountsListAdapter(accountsViewModel) {
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
            } else {
                binding.noRecordsTv.visibility = View.INVISIBLE
                binding.recyclerView.visibility = View.VISIBLE
                binding.totalAmount.visibility = View.VISIBLE
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}