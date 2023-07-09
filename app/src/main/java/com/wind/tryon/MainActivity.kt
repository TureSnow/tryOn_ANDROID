package com.wind.tryon

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.wind.tryon.database.TryOnItemViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setContent {
            val imageViewModel:ImageViewModel by viewModels()
            val tryOnItemViewModel:TryOnItemViewModel by viewModels()
//            tryOnItemViewModel.deleteAll()
            MainScreen(imageViewModel,tryOnItemViewModel)
        }
    }
}


@Composable
fun PickImageFromGallery() {
    Scaffold(
        content = {
        var uri:Uri? = null
        val imageData = remember { mutableStateOf(uri) }
        val context = LocalContext.current
        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                imageData.value = it
            }
        Column(
            modifier = Modifier.padding(16.dp), content = {
                Button(onClick = {
                    launcher.launch(
                        "image/*"
                    )
                }, content = {
                    Text(text = "Select Image From Gallery")
                })
                imageData.let {
                    val bitmap1:Bitmap? = null
                    val bitmap = remember { mutableStateOf(bitmap1) }
                    val uri = it.value
                    if (uri != null) {
                        if (Build.VERSION.SDK_INT < 28) {
                            bitmap.value = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)

                        } else {
                            val source = ImageDecoder.createSource(context.contentResolver, uri)
                            bitmap.value = ImageDecoder.decodeBitmap(source)
                        }

                        bitmap.value?.let { btm ->
                            Image(
                                bitmap = btm.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.size(400.dp)
                            )
                        }
                    }

                }
            })
    })
}

