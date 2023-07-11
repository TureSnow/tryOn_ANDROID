package com.wind.tryon.ui.components

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.wind.tryon.R

@Composable
fun ResultCard(
    imageUrl:String?,
    modifier: Modifier
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val size = 320.dp
        if (imageUrl==null){
            Image(
                painter = painterResource(R.drawable.sample_result) ,
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(size)
                    .height(size)
                    .clip(RoundedCornerShape(10.dp))
                    .shadow(1.dp, RoundedCornerShape(10.dp))
            )
        }else{
            Image(
                painter = rememberImagePainter(
                    data = imageUrl,
                    builder = {
                        placeholder(R.drawable.sample_result)
                        crossfade(true)
                    }),
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(size)
                    .height(size)
                    .clip(RoundedCornerShape(10.dp))
                    .shadow(1.dp, RoundedCornerShape(10.dp))
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "试穿结果")
    }
}

@Composable
fun CardItem(modifier: Modifier,
             name: String,
             uri: Uri?){
    Button(onClick = {  },
        modifier,
        shape =  RoundedCornerShape(10.dp),
        colors =  ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
    ){
        PhotoCard(
            modifier,
            name,
            uri
        )
    }
}

/**
 * type:PhotoCard is for cloth:0?person:1?
 */
@Composable
private fun PhotoCard(
    modifier: Modifier,
    name: String,
    uri: Uri?
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val size = 150.dp
        if (uri == null){
            val id = if (name == "cloth"){
                R.drawable.sample_cloth
            }else{
                R.drawable.sample_person
            }
            Image(
                painter = painterResource(id) ,
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(size)
                    .height(size)
                    .clip(RoundedCornerShape(10.dp))
            )
        }else{
            val context = LocalContext.current
            val bitmap1: Bitmap? = null
            val bitmap = remember { mutableStateOf(bitmap1) }
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
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(size)
                        .height(size)
                        .clip(RoundedCornerShape(10.dp))
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "$name")
    }
}