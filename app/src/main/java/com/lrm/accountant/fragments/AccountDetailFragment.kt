package com.lrm.accountant.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.lrm.accountant.databinding.FragmentAccountDetailBinding
import com.lrm.accountant.model.Account
import com.lrm.accountant.viewmodel.AccountsViewModel

class AccountDetailFragment : Fragment() {

    private var _binding: FragmentAccountDetailBinding? = null
    private val binding get() = _binding!!

    private val accountsViewModel: AccountsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backIcon.setOnClickListener { this.findNavController().navigateUp() }

        accountsViewModel.accountData.observe(viewLifecycleOwner) {account ->
            if (account != null) {
                bind(account)
            }
        }
    }

    private fun bind(account: Account) {
        binding.apply {
            fragmentLabel.text = account.actName
            actId.text = account.actId.toString()
            actName.text = account.actName
            amount.text = "₹ ${account.amount}"

            if (account.dsdate != null) {
                dsdate.text = account.dsdate.toString()
            } else dsdate.text = "-"

            if (account.module != null) {
                module.text = account.module.toString()
            } else module.text = "-"

            if (account.scrnname != null) {
                scrnname.text = account.scrnname.toString()
            } else scrnname.text = "-"

            actname1.text = account.actname1
            actmaintype.text = account.actmaintype
            actunder.text = account.actunder.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}