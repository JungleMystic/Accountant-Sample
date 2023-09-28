package com.lrm.accountant.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lrm.accountant.databinding.ListItemBinding
import com.lrm.accountant.model.Account
import com.lrm.accountant.viewmodel.AccountsViewModel

class AccountsListAdapter(
    private val viewModel: AccountsViewModel,
    private val onItemClicked: (Account) -> Unit
): ListAdapter<Account, AccountsListAdapter.AccountViewHolder>(DiffCallback) {

    inner class AccountViewHolder(private val binding: ListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(account: Account, itemPosition: Int) {
            binding.accountId.text = account.actId.toString()
            binding.accountName.text = account.actName
            binding.amount.text = account.amount.toString()
            binding.deleteRow.setOnClickListener {
                viewModel.removeAccount(itemPosition)
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        return AccountViewHolder(
            ListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account = getItem(position)
        holder.bind(account, position)
        holder.itemView.setOnClickListener { onItemClicked(account) }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Account>() {
        override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem.actId == newItem.actId
        }

        override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem == newItem
        }
    }
}