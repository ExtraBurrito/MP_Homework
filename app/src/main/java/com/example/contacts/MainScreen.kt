package com.example.contacts

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun MainScreen(
    viewModel: ContactViewModel,
    onNavigateToUpdate: (Int) -> Unit
) {
    val context = LocalContext.current
    val photoRepo = remember { PhotoRepository(context) }
    val scope = rememberCoroutineScope()


    val contacts by viewModel.allContacts.observeAsState(emptyList())


    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var photoPath by remember { mutableStateOf<String?>(null) }


    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            scope.launch {
                photoPath = photoRepo.saveFromUri(it)
            }
        }
    }

    // Валидация кнопок (замена TextWatcher)
    val isEmailValid = email.contains("@") || email.isEmpty()
    val isPhoneValid = phone.length >= 7 || phone.isEmpty()
    val isFormValid = email.contains("@") && phone.length >= 7 && email.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Поля ввода
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
            isError = !isPhoneValid,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Имейл") },
            isError = !isEmailValid,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Адрес") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = photoUrl,
            onValueChange = { photoUrl = it },
            label = { Text("Photo URL (опционално)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Превью картинки (заменяет ImageView)
            AsyncImage(
                model = photoPath ?: android.R.drawable.ic_menu_gallery,
                contentDescription = "Preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(60.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = { 
                pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
            }) {
                Text("Снимка")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                enabled = isFormValid,
                onClick = {
                    val contact = Contact(
                        name = name,
                        phone = phone,
                        email = email,
                        address = address,
                        photoUrl = photoUrl,
                        photoPath = photoPath ?: ""
                    )
                    viewModel.addContact(contact)


                    name = ""; phone = ""; email = ""; address = ""; photoUrl = ""; photoPath = null
                }
            ) {
                Text("Добави")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        LazyColumn(modifier = Modifier.weight(1f)) {
            items(contacts) { contact ->
                ContactItem(contact = contact, onClick = { onNavigateToUpdate(contact.id) })
                Divider()
            }
        }
    }
}


@Composable
fun ContactItem(contact: Contact, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val imageSource = when {
            contact.photoPath.isNotEmpty() -> File(contact.photoPath)
            contact.photoUrl.isNotEmpty() -> contact.photoUrl
            else -> android.R.drawable.sym_def_app_icon
        }

        AsyncImage(
            model = imageSource,
            contentDescription = "Contact photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(50.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(text = contact.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = contact.phone, fontSize = 14.sp)
        }
    }
}