package com.wind.tryon

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.wind.tryon.database.TryOnItem
import com.wind.tryon.database.TryOnItemViewModel
import com.wind.tryon.ui.components.CardItem
import com.wind.tryon.ui.components.DialogBoxLoading
import com.wind.tryon.ui.components.ResultCard

sealed class BottomNavigationScreens(val route: String, @StringRes val resourceId: Int, @DrawableRes val iconId: Int) {
    object TryOn : BottomNavigationScreens("tryon", R.string.tryon_route, R.drawable.cloth)
    object Wardrobe : BottomNavigationScreens("wardrobe", R.string.wardrobe_route, R.drawable.wardrobe)
}



@Composable
fun MainScreen(imageViewModel:ImageViewModel,tryOnItemViewModel:TryOnItemViewModel) {

    val navController = rememberNavController()

    val bottomNavigationItems = listOf(
        BottomNavigationScreens.TryOn,
        BottomNavigationScreens.Wardrobe
    )
    Scaffold(
        bottomBar = {
            TryOnAppBottomNavigation(navController, bottomNavigationItems)
        },
    ) {
        MainScreenNavigationConfigurations(navController,imageViewModel,tryOnItemViewModel)
    }
}

@Composable
private fun MainScreenNavigationConfigurations(
    navController: NavHostController,
    imageViewModel:ImageViewModel,
    tryOnItemViewModel:TryOnItemViewModel
) {
    NavHost(navController, startDestination = BottomNavigationScreens.TryOn.route) {
        composable(BottomNavigationScreens.Wardrobe.route) {
            TryOnHistory(tryOnItemViewModel)
        }
        composable(BottomNavigationScreens.TryOn.route) {
            TryOnScreen(imageViewModel,tryOnItemViewModel)
        }
    }
}



@Composable
private fun TryOnAppBottomNavigation(
    navController: NavHostController,
    items: List<BottomNavigationScreens>
) {
    BottomNavigation (
        backgroundColor = Color.White){
        val currentRoute = currentRoute(navController)
        items.forEach { screen ->
            BottomNavigationItem(
                icon = { Icon(painter = painterResource(screen.iconId),
                                contentDescription = null,
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(10.dp)))},
                label = { Text(stringResource(id = screen.resourceId)) },
                selected = currentRoute == screen.route,
                alwaysShowLabel = false, // This hides the title for the unselected items
                onClick = {
                    // This if check gives us a "singleTop" behavior where we do not create a
                    // second instance of the composable if we are already on that destination
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route)
                    }
                }
            )
        }
    }
}

@Composable
private fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}
@Composable
fun TryOnScreen(
    imageViewModel:ImageViewModel,
    tryOnItemViewModel:TryOnItemViewModel
    ){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        val context = LocalContext.current
        //衣服选择
        val clothImageData: MutableState<Uri?> = remember { mutableStateOf(null) }
        val clothPhotoLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                clothImageData.value = it
            }
        //人物照选择
        val personImageData: MutableState<Uri?>  = remember { mutableStateOf(null) }
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
        OutlinedButton(
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
                    val clothInputStream = context.contentResolver.openInputStream(clothImageData.value!!)
                    val clothBase64String = clothInputStream?.let { getBase64String(it) }
                    val personInputStream = context.contentResolver.openInputStream(personImageData.value!!)
                    val personBase64String = personInputStream?.let { getBase64String(it) }
                    if (clothBase64String != null) {
                        if (personBase64String != null) {
                            imageViewModel.getImageData(clothBase64String,personBase64String)
                        }
                    }
                }
            },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White,backgroundColor = Color.Black)
            ) {
            Text( text = "进行试穿" )
        }
        val resultUrl: MutableState<String?>  = remember { mutableStateOf(null) }
        imageViewModel.imageData.value?.let{
            if (imageViewModel.imageData.value!!.message=="handle ok!"){
                resultUrl.value = imageViewModel.imageData.value!!.image_url
                Toast.makeText(
                    context,
                    "试穿成功！",
                    Toast.LENGTH_SHORT
                ).show()
                val clothInputStream = context.contentResolver.openInputStream(clothImageData.value!!)
                val clothBase64String = clothInputStream?.let { getBase64String(it) }
                val personInputStream = context.contentResolver.openInputStream(personImageData.value!!)
                val personBase64String = personInputStream?.let { getBase64String(it) }
                //插入数据库
                tryOnItemViewModel.insert(
                    TryOnItem(
                        url = imageViewModel.imageData.value!!.image_url,
                        cloth_base64 = clothBase64String!!,
                        person_base64 = personBase64String!!))
                Log.d("----------------","insert url:"+imageViewModel.imageData.value!!.image_url+
                                                    "\n cloth_uri: "+clothImageData.value+
                                                    "\n person_uri: "+personImageData.value)
            }else{
                Toast.makeText(
                    context,
                    "试穿失败，请重试...",
                    Toast.LENGTH_SHORT
                ).show()
            }
            imageViewModel.imageData.value = null
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

@Composable
fun TryOnHistory(itemViewModel:TryOnItemViewModel){
    val itemList = itemViewModel.fetchAll().observeAsState(arrayListOf())
    val context = LocalContext.current
    LazyColumn(
        content = {
            items(
                items = itemList.value,
                itemContent = {
                    HistoryCard( it, onClick = {
                        itemViewModel.deleteById(it.itemId)
                        Toast.makeText(
                            context,
                            "删除成功",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                }
            )
        },
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .height(650.dp)
    )
}

@Composable
fun HistoryCard(item:TryOnItem, onClick: () -> Unit){
    Row(modifier = Modifier
        .padding(10.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(Color.DarkGray),
        verticalAlignment = Alignment.CenterVertically
        ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val imageBytes1 = Base64.decode(item.cloth_base64, Base64.DEFAULT)
            val clothBitmap = BitmapFactory.decodeByteArray(imageBytes1, 0, imageBytes1.size)
            clothBitmap?.let { btm ->
                Image(
                    bitmap = btm.asImageBitmap(),
                    contentDescription = null,
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(200.dp)
                        .height(125.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .padding(10.dp)
                )
            }
            val imageBytes2 = Base64.decode(item.person_base64, Base64.DEFAULT)
            val personBitmap = BitmapFactory.decodeByteArray(imageBytes2, 0, imageBytes2.size)
            personBitmap?.let { btm ->
                Image(
                    bitmap = btm.asImageBitmap(),
                    contentDescription = null,
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(200.dp)
                        .height(125.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .padding(10.dp)
                )
            }
            OutlinedButton(
                modifier =Modifier.padding(10.dp),
                onClick = onClick,
                shape = RoundedCornerShape(50), // = 50% percent
                //or shape = CircleShape
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
            ){
                Text(text = "删除该项记录")
            }
        }
        Image(
            painter = rememberImagePainter(
                data = item.url,
                builder = {
                    placeholder(R.drawable.sample_result)
                    crossfade(true)
                }),
            contentDescription = null,
            alignment = Alignment.Center,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(250.dp)
                .height(270.dp)
                .clip(RoundedCornerShape(40.dp))
                .padding(10.dp)
        )
    }

}



