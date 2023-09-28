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

class AccountsViewModel: ViewModel() {

    private val _accountsDataList = MutableLiveData<List<Account>>()
    val accountsDataList: LiveData<List<Account>> get() = _accountsDataList

    init {
        getData()
    }

    // To retrieve data from the Web Api
    private fun getData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.i(TAG, "getData: current thread -> ${Thread.currentThread().name}")
                val data = AccountApi.retrofitService.getData()
                if (data != "") {
                    val listType = object: TypeToken<List<Account>>() {}.type
                    val dataList: List<Account> = Gson().fromJson(data, listType)
                    _accountsDataList.postValue(dataList)
                }
            } catch (e: Exception) {
                _accountsDataList.value = listOf()
                Log.i(TAG, "getData: Exception -> ${e.printStackTrace()}")
            }
        }
    }
}