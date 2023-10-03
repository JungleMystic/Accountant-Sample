package com.lrm.accountant.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
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

    private val _onlineStatus = MutableLiveData<Boolean>()
    val onlineStatus: LiveData<Boolean> get() = _onlineStatus

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

    fun getAccount(id: Int): Account {
        val account = _accountsDataList.value?.single { it.actId == id }
        return account!!
    }

    fun updateAccount(id: Int, name: String, amount: Double) {
        val account = getAccount(id)
        Log.i(TAG, "updateAccount: getAccount -> $account")
        val updatedAccount = Account(id, name, account.actmaintype, account.actname1, account.actunder, amount, account.dsdate, account.module, account.scrnname)
        Log.i(TAG, "updateAccount: Updated -> $updatedAccount")
        _accountsDataList.value?.remove(account)
        _accountsDataList.value?.add(updatedAccount)
        Log.i(TAG, "updateAccount: New Accounts Data list -> ${accountsDataList.value}")
    }

    // To remove an item from the accounts list
    fun removeAccount(position: Int) {
        _accountsDataList.postValue(_accountsDataList.value.apply { this?.removeAt(position) })
    }

    // Setting the account chosen by user
    fun setAccountData(account: Account) {
        _accountData.value = account
    }

    // Setting the Online status
    private fun setOnlineStatus(status: Boolean) {
        _onlineStatus.postValue(status)
    }

    // This checks the Internet access
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
        return false
    }

    // This registers the network callback functions and sets the online status
    fun checkForInternet(context: Context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                setOnlineStatus(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                setOnlineStatus(false)
            }
        })
    }
}