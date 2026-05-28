package com.example.contacts

import android.widget.Toast
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
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
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
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var duplicateMessage by remember { mutableStateOf("") }

    val checkForDuplicates = { checkName: String, checkPhone: String ->
        contacts.find { it.name.equals(checkName, ignoreCase = true) }?.let {
            "Контактът с име '${checkName}' вече съществува!"
        } ?: contacts.find { it.phone == checkPhone }?.let {
            "Телефонният номер '${checkPhone}' вече се използва!"
        }
    }
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            scope.launch {
                photoPath = photoRepo.saveFromUri(it)
            }
        }
    }
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val parsedContact = parseQrToContact(result.contents)
            if (parsedContact != null) {
                val error = checkForDuplicates(parsedContact.name, parsedContact.phone)
                if (error != null) {
                    duplicateMessage = error
                    showDuplicateDialog = true
                } else {
                    viewModel.addContact(parsedContact)
                    Toast.makeText(context, "Контактът е импортиран!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Грешка: Неразпознат QR формат", Toast.LENGTH_LONG).show()
            }
        }
    }
    val isEmailValid = email.contains("@") || email.isEmpty()
    val isPhoneValid = phone.length >= 7 || phone.isEmpty()
    val isFormValid = email.contains("@") && phone.length >= 7 && email.isNotEmpty()

    if (showDuplicateDialog) {
        AlertDialog(
            onDismissRequest = { showDuplicateDialog = false },
            title = {
                Text(text = "Внимание", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = duplicateMessage, fontSize = 16.sp)
            },
            confirmButton = {
                Button(onClick = { showDuplicateDialog = false }) {
                    Text("ОК")
                }
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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
            AsyncImage(
                model = photoPath ?: android.R.drawable.ic_menu_gallery,
                contentDescription = "Preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(50.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(4.dp)
            ) {
                Text("Снимка", fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Button(
                onClick = {
                    val options = ScanOptions().apply {
                        setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                        setPrompt("Поставете QR кода в рамката")
                        setCameraId(0)
                        setBeepEnabled(true)
                        setBarcodeImageEnabled(true)
                        setOrientationLocked(true)
                        setCaptureActivity(CustomScannerActivity::class.java)
                    }
                    scanLauncher.launch(options)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                contentPadding = PaddingValues(4.dp)
            ) {
                Text("QR Скенер", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.width(4.dp))

            Button(
                enabled = isFormValid,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(4.dp),
                onClick = {
                    val error = checkForDuplicates(name, phone)
                    if (error != null) {
                        duplicateMessage = error
                        showDuplicateDialog = true
                    } else {
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
                }
            ) {
                Text("Добави", fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(contacts) { contact ->
                ContactItem(contact = contact, onClick = { onNavigateToUpdate(contact.id) })
                HorizontalDivider()
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
fun parseQrToContact(qrText: String): Contact? {
    try {
        if (qrText.uppercase().startsWith("MECARD:")) {
            var name = ""
            var phone = ""
            var email = ""
            var address = ""
            val data = qrText.removePrefix("MECARD:").removePrefix("mecard:").removeSuffix(";;")
            val fields = data.split(";")
            for (field in fields) {
                when {
                    field.uppercase().startsWith("N:") -> name = field.drop(2)
                    field.uppercase().startsWith("TEL:") -> phone = field.drop(4)
                    field.uppercase().startsWith("EMAIL:") -> email = field.drop(6)
                    field.uppercase().startsWith("ADR:") -> address = field.drop(4)
                }
            }
            if (name.isNotEmpty() && phone.isNotEmpty()) {
                return Contact(name = name, phone = phone, email = email, address = address, photoUrl = "", photoPath = "")
            }
        } else {
            val parts = qrText.split(",")
            if (parts.size >= 2) {
                return Contact(
                    name = parts[0].trim(),
                    phone = parts[1].trim(),
                    email = parts.getOrNull(2)?.trim() ?: "",
                    address = parts.getOrNull(3)?.trim() ?: "",
                    photoUrl = "",
                    photoPath = ""
                )
            }
        }
    } catch (e: Exception) {
        return null
    }
    return null
}