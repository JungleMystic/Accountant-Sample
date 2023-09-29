package com.lrm.accountant.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lrm.accountant.R
import com.lrm.accountant.databinding.ListItemBinding
import com.lrm.accountant.model.Account
import com.lrm.accountant.viewmodel.AccountsViewModel

class AccountsListAdapter(
    private val activity: Activity,
    private val context: Context,
    private val viewModel: AccountsViewModel,
    private val onItemClicked: (Account) -> Unit
): ListAdapter<Account, AccountsListAdapter.AccountViewHolder>(DiffCallback) {

    inner class AccountViewHolder(private val binding: ListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("NotifyDataSetChanged")
        fun bind(account: Account, itemPosition: Int) {
            binding.accountId.text = account.actId.toString()
            binding.accountName.text = account.actName
            binding.amount.text = "₹ ${account.amount.toString()}"
            binding.deleteRow.setOnClickListener {
                showDeleteDialog(account, itemPosition)
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

    private fun showDeleteDialog(account: Account, itemPosition: Int) {
        val dialogView = activity.layoutInflater.inflate(R.layout.custom_delete_dialog, null)
        val actIdTv = dialogView.findViewById<TextView>(R.id.act_id_dd)
        val actNameTv = dialogView.findViewById<TextView>(R.id.act_name_dd)
        val yesTv = dialogView.findViewById<TextView>(R.id.yes_tv)
        val noTv = dialogView.findViewById<TextView>(R.id.no_tv)

        actIdTv.text = account.actId.toString()
        actNameTv.text = account.actName

        val builder = AlertDialog.Builder(context)
        builder.setView(dialogView)
        builder.setCancelable(true)

        val deleteDialog = builder.create()
        deleteDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        deleteDialog.show()

        yesTv.setOnClickListener {
            viewModel.removeAccount(itemPosition)
            notifyDataSetChanged()
            deleteDialog.dismiss()
        }

        noTv.setOnClickListener {
            deleteDialog.dismiss()
        }
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