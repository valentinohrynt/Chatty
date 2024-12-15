package com.inoo.chatty.ui.main.chat.receiverlist

import android.annotation.SuppressLint
import android.app.Application
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.ViewModel
import com.inoo.chatty.model.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@SuppressLint("Range")
@HiltViewModel
class ReceiverListViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiverListUiState())
    val uiState = _uiState.asStateFlow()

    private val _contactList = MutableStateFlow<List<Contact>>(emptyList())
    val contactList = _contactList.asStateFlow()

    fun loadContacts() {
        val contactList = mutableListOf<Pair<String, String>>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor = application.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null, null, null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val name =
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phone =
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                contactList.add(Pair(name, phone))
            }
        }

        contactList.forEach { (name, phone) ->
            val contact = Contact(name, phone)
            _contactList.value = _contactList.value + contact
        }
    }
}

data class ReceiverListUiState(
    val isLoading: Boolean = true,
    val success: String? = null,
    val error: String? = null
)