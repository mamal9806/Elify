package com.mamad.elify

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.ColorMatrix as AndroidColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ColorMatrix as ComposeColorMatrix
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrixColorFilter as ComposeMatrixFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

private val Purple = Color(0xFF7C4DFF)
private val DeepPurple = Color(0xFF6035D5)
private val Lavender = Color(0xFFEDE7FF)
private val Page = Color(0xFFF8F7FC)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ElifyEditor() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElifyEditor() {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var original by remember { mutableStateOf<Bitmap?>(null) }
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var warmth by remember { mutableFloatStateOf(0f) }
    var activeFilter by remember { mutableStateOf("Original") }
    var category by remember { mutableStateOf("Adjust") }
    val categories = listOf("Adjust","Retouch","Filters","Effects","Makeup","Reshape","Body","Crop","Background","Text","Stickers","Collage","Camera")
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { ctx.contentResolver.openInputStream(it)?.use { stream -> original = BitmapFactory.decodeStream(stream) } }
        brightness = 0f; contrast = 1f; saturation = 1f; warmth = 0f; activeFilter = "Original"
    }
    val previewMatrix = ComposeColorMatrix().apply {
        setToSaturation(saturation)
        val c = contrast
        val b = brightness * 255f
        val contrastMatrix = ComposeColorMatrix(floatArrayOf(
            c,0f,0f,0f,b,
            0f,c,0f,0f,b,
            0f,0f,c,0f,b,
            0f,0f,0f,1f,0f
        ))
        times(contrastMatrix)
    }
    Scaffold(
        containerColor = Page,
        topBar = { TopAppBar(title={ Text("Elify", color=DeepPurple, fontWeight=FontWeight.Bold) }, actions={
            IconButton(onClick={ brightness=0f;contrast=1f;saturation=1f;warmth=0f;activeFilter="Original" }) { Icon(Icons.Default.RestartAlt,"Reset",tint=Purple) }
        }) }
    ) { insets ->
        Column(Modifier.fillMaxSize().padding(insets).padding(horizontal=12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) {
                Text("Photo Studio", color=DeepPurple, fontWeight=FontWeight.SemiBold)
                TextButton(onClick={ photoPicker.launch("image/*") }) { Icon(Icons.Default.AddPhotoAlternate,null); Text(" Open photo") }
            }
            Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(24.dp)).background(Color.White), contentAlignment=Alignment.Center) {
                val photo = original
                if (photo == null) {
                    Column(horizontalAlignment=Alignment.Center) {
                        Icon(Icons.Default.Image,color=Purple,modifier=Modifier.size(64.dp))
                        Text("Choose a photo to begin",color=DeepPurple)
                    }
                } else Image(photo.asImageBitmap(), null, Modifier.fillMaxWidth().padding(6.dp), contentScale=ContentScale.Fit, colorFilter=ColorFilter.colorMatrix(previewMatrix))
            }
            Spacer(Modifier.height(8.dp))
            ScrollableTabRow(selectedTabIndex=categories.indexOf(category).coerceAtLeast(0), containerColor=Page, contentColor=Purple) {
                categories.forEach { item -> Tab(selected=category==item,onClick={category=item},text={Text(item,fontSize=12.sp)}) }
            }
            Column(Modifier.fillMaxWidth().height(170.dp).padding(top=6.dp)) {
                when(category) {
                    "Adjust" -> {
                        ControlSlider("Brightness",brightness,-1f..1f){brightness=it}
                        ControlSlider("Contrast",contrast,0.25f..2f){contrast=it}
                        ControlSlider("Saturation",saturation,0f..2f){saturation=it}
                        ControlSlider("Warmth",warmth,-1f..1f){warmth=it}
                    }
                    "Filters" -> Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                        listOf("Original","Mono","Vintage","Cool","Warm").forEach { name ->
                            FilterChip(selected=activeFilter==name,onClick={
                                activeFilter=name
                                saturation=if(name=="Mono") 0f else 1f
                                brightness=if(name=="Warm") 0.08f else if(name=="Vintage") 0.04f else 0f
                                contrast=if(name=="Vintage") 0.9f else 1f
                            },label={Text(name)})
                        }
                    }
                    else -> Text(category+" tools planned in modular roadmap.", color=DeepPurple, fontSize=13.sp)
                }
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                Button(onClick={photoPicker.launch("image/*")},modifier=Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=Purple)) { Icon(Icons.Default.PhotoLibrary,null); Text("Open") }
                Button(onClick={
                    val src=original
                    if(src==null) Toast.makeText(ctx,"Select a photo first",Toast.LENGTH_SHORT).show()
                    else {
                        val outBitmap=Bitmap.createBitmap(src.width,src.height,Bitmap.Config.ARGB_8888)
                        val canvas=Canvas(outBitmap)
                        val paint=Paint(Paint.ANTI_ALIAS_FLAG)
                        val cm=AndroidColorMatrix()
                        cm.setSaturation(saturation)
                        val c=contrast
                        val b=brightness*255f
                        val adjust=AndroidColorMatrix(floatArrayOf(c,0f,0f,0f,b,0f,c,0f,0f,b,0f,0f,c,0f,b,0f,0f,0f,1f,0f))
                        cm.postConcat(adjust)
                        paint.colorFilter=ColorMatrixColorFilter(cm)
                        canvas.drawBitmap(src,0f,0f,paint)
                        val cv=ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME,"Elify_"+System.currentTimeMillis()+".jpg")
                            put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg")
                            put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/Elify")
                        }
                        val uri=ctx.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv)
                        uri?.let { ctx.contentResolver.openOutputStream(it)?.use { stream -> outBitmap.compress(Bitmap.CompressFormat.JPEG,95,stream) } }
                        Toast.makeText(ctx,"Saved in Pictures/Elify",Toast.LENGTH_SHORT).show()
                    }
                },modifier=Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=DeepPurple)) { Icon(Icons.Default.Save,null);Text("Save") }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ControlSlider(title:String,value:Float,range:ClosedFloatingPointRange<Float>,onChange:(Float)->Unit) {
    Row(verticalAlignment=Alignment.CenterVertically) {
        Text(title,modifier=Modifier.width(68.dp),fontSize=12.sp,color=DeepPurple)
        Slider(value=value,onValueChange=onChange,valueRange=range,modifier=Modifier.weight(1f),colors=SliderDefaults.colors(thumbColor=Purple,activeTrackColor=Purple))
        Text(String.format("%.2f",value),modifier=Modifier.width(38.dp),fontSize=10.sp)
    }
}
