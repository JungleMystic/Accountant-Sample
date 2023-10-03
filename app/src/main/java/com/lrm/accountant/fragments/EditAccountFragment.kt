package com.lrm.accountant.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.lrm.accountant.constants.TAG
import com.lrm.accountant.databinding.FragmentEditAccountBinding
import com.lrm.accountant.model.Account
import com.lrm.accountant.viewmodel.AccountsViewModel

class EditAccountFragment : Fragment() {

    private var _binding: FragmentEditAccountBinding? = null
    private val binding get() = _binding!!

    private val accountsViewModel: AccountsViewModel by activityViewModels()
    private val navArgs: EditAccountFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backIcon.setOnClickListener { this.findNavController().navigateUp() }

        val accountId = navArgs.id
        val account = accountsViewModel.getAccount(accountId)
        Log.i(TAG, "onViewCreated: EditAccount -> Account Data -> $account")
        bind(account)
    }

    private fun updateAccount(id: Int) {
        val accountName = binding.accountName.text.toString()
        val amount = binding.amount.text.toString().toDouble()
        accountsViewModel.updateAccount(id, accountName, amount)

        val action = EditAccountFragmentDirections.actionEditAccountFragmentToHomeFragment()
        this.findNavController().navigate(action)
    }

    private fun bind(account: Account) {
        binding.accountName.setText(account.actName)
        binding.amount.setText(account.amount.toString())
        binding.updateButton.setOnClickListener {
            updateAccount(account.actId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}