package com.example.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File

@Composable
fun UpdateDeleteScreen(
    contactId: Int,
    viewModel: ContactViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToMap: (String, String) -> Unit
) {
    var currentContact by remember { mutableStateOf<Contact?>(null) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    LaunchedEffect(contactId) {
        val contact = viewModel.getContactById(contactId)
        if (contact != null) {
            currentContact = contact
            name = contact.name
            phone = contact.phone
            email = contact.email
            address = contact.address
        } else {
            onNavigateBack()
        }
    }

    if (currentContact == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // VALIDATION
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,}\$".toRegex()
    val phoneRegex = "^\\+?[0-9\\s-]{7,15}\$".toRegex()
    val isEmailError = email.isNotEmpty() && !emailRegex.matches(email)
    val isPhoneError = phone.isNotEmpty() && !phoneRegex.matches(phone)
    val isFormValid = name.isNotBlank() && emailRegex.matches(email) && phoneRegex.matches(phone)
    // ---------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val contact = currentContact!!
        val imageSource = when {
            contact.photoPath.isNotEmpty() -> File(contact.photoPath)
            contact.photoUrl.isNotEmpty() -> contact.photoUrl
            else -> android.R.drawable.sym_def_app_icon
        }

        AsyncImage(
            model = imageSource,
            contentDescription = "Update Photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .background(Color(0xFFEEEEEE))
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Име") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Телефон") },
            isError = isPhoneError,
            supportingText = {
                if (isPhoneError) Text("Невалиден телефонен номер")
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Имейл") },
            isError = isEmailError,
            supportingText = {
                if (isEmailError) Text("Невалиден имейл адрес")
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Адрес") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                modifier = Modifier.weight(1f).padding(end = 4.dp),
                enabled = isFormValid,
                onClick = {
                    val updatedContact = contact.copy(
                        name = name,
                        phone = phone,
                        email = email,
                        address = address
                    )
                    viewModel.updateContact(updatedContact)
                    onNavigateBack()
                }
            ) {
                Text("Запази")
            }
            Button(
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                onClick = {
                    viewModel.deleteContact(contact)
                    onNavigateBack()
                }
            ) {
                Text("Изтрий")
            }
            Button(
                modifier = Modifier.weight(1f).padding(start = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                onClick = {
                    onNavigateToMap(contact.name, contact.address)
                }
            ) {
                Text("Карта")
            }
        }
    }
}