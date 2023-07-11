package com.wind.tryon

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.wind.tryon.ui.components.CardItem
import com.wind.tryon.ui.components.DialogBoxLoading
import com.wind.tryon.ui.components.ResultCard

class SelectPhotosActivity : ComponentActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                val context = LocalContext.current
                //衣服选择
                val clothImageData:MutableState<Uri?> = remember { mutableStateOf(null) }
                val clothPhotoLauncher =
                    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                        clothImageData.value = it
                    }
                //人物照选择
                val personImageData:MutableState<Uri?>  = remember {mutableStateOf(null)}
                val personPhotoLauncher =
                    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                        personImageData.value = it
                    }
                Row{
                    CardItem(
                        modifier = Modifier.clickable {
                            clothPhotoLauncher.launch("image/*")
                        },
                        name = "cloth",
                        clothImageData.value
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    CardItem(
                        modifier = Modifier.clickable {
                            personPhotoLauncher.launch("image/*")
                        },
                        name = "person",
                        personImageData.value,
                    )
                }
                val imageViewModel:ImageViewModel by viewModels()
                Button(
                    modifier = Modifier.padding(30.dp),
                    onClick = {
                    if (personImageData.value == null){
                        Toast.makeText(
                            context,
                            "请选择你的上半身照片",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (clothImageData.value == null){
                        Toast.makeText(
                            context,
                            "请选择衣服照片",
                            Toast.LENGTH_SHORT
                        ).show()
                    }else {
                        clothImageData.value!!.path?.let { Log.d("cloth path: ", it) }
                        personImageData.value!!.path?.let { Log.d("person path: ", it) }
                        val clothInputStream = contentResolver.openInputStream(clothImageData.value!!)
                        val clothBase64String = clothInputStream?.let { getBase64String(it) }
                        val personInputStream = contentResolver.openInputStream(personImageData.value!!)
                        val personBase64String = personInputStream?.let { getBase64String(it) }
                        if (clothBase64String != null) {
                            if (personBase64String != null) {
                                imageViewModel.getImageData(clothBase64String,personBase64String)

                            }
                        }
                    }
                }) {
                    Text("进行试穿")
                }
                val resultUrl:MutableState<String?>  = remember {mutableStateOf(null)}
                imageViewModel.imageData.value?.let{
                    if (imageViewModel.imageData.value!!.message=="handle ok!"){
                        resultUrl.value = imageViewModel.imageData.value!!.image_url
                        Toast.makeText(
                            context,
                            "试穿成功！",
                            Toast.LENGTH_SHORT
                        ).show()
                    }else{
                        Toast.makeText(
                            context,
                            "试穿失败，请重试...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                ResultCard(
                    resultUrl.value,
                    modifier = Modifier.padding(10.dp)
                )
                if (imageViewModel.loadState.value == 0){
                    DialogBoxLoading()
                }
            }
        }
    }
}
