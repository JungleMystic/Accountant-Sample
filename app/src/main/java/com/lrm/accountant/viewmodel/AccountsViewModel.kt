package com.lrm.accountant.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lrm.accountant.constants.TAG
import com.lrm.accountant.model.Account
import com.lrm.accountant.network.AccountApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccountsViewModel : ViewModel() {

    private val _accountsDataList = MutableLiveData<MutableList<Account>>(mutableListOf())
    val accountsDataList: LiveData<MutableList<Account>> get() = _accountsDataList

    private val _accountData = MutableLiveData<Account>()
    val accountData: LiveData<Account> get() = _accountData

    init {
        getData()
    }

    // To retrieve data from the Web Api
    fun getData() {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                Log.i(TAG, "getData: current thread -> ${Thread.currentThread().name}")
                val data = AccountApi.retrofitService.getData()
                if (data != "") {
                    val listType = object : TypeToken<List<Account>>() {}.type
                    val dataList: MutableList<Account> = Gson().fromJson(data, listType)
                    _accountsDataList.postValue(dataList)
                }
            }
        } catch (e: Exception) {
            _accountsDataList.value = mutableListOf()
            e.printStackTrace()
            Log.i(TAG, "getData: Exception -> ${e.message}")
        }
    }

    // To remove an item from the accounts list
    fun removeAccount(position: Int) {
        _accountsDataList.postValue(_accountsDataList.value.apply { this?.removeAt(position) })
    }

    fun setAccountData(account: Account) {
        _accountData.value = account
    }
}