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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

private val Purple = Color(0xFF7C4DFF)
private val DeepPurple = Color(0xFF6035D5)
private val Lavender = Color(0xFFEDE7FF)
private val Page = Color(0xFFF8F7FC)

private data class EditState(val brightness: Float=0f, val contrast: Float=1f, val saturation: Float=1f, val warmth: Float=0f, val exposure:Float=0f, val fade:Float=0f, val filter:String="Original")
private fun matrixFor(s:EditState):FloatArray {
    val c=s.contrast
    val b=(s.brightness+s.exposure)*255f
    val sat=AndroidColorMatrix().apply { setSaturation(s.saturation) }
    val adj=AndroidColorMatrix(floatArrayOf(c,0f,0f,0f,b,0f,c,0f,0f,b,0f,0f,c,0f,b,0f,0f,0f,1f,0f))
    sat.postConcat(adj)
    // warmth adds red and removes a little blue, approximated by channel offsets
    if(s.warmth!=0f) sat.postConcat(AndroidColorMatrix(floatArrayOf(1f,0f,0f,0f,s.warmth*24f,0f,1f,0f,0f,0f,0f,0f,1f,0f,-s.warmth*24f,0f,0f,0f,1f,0f)))
    if(s.fade>0f) sat.postConcat(AndroidColorMatrix(floatArrayOf(1f,0f,0f,0f,s.fade*24f,0f,1f,0f,0f,s.fade*24f,0f,0f,1f,0f,s.fade*24f,0f,0f,0f,1f,0f)))
    if(s.filter=="Mono") sat.setSaturation(0f)
    if(s.filter=="Vintage") sat.postConcat(AndroidColorMatrix(floatArrayOf(.92f,0f,0f,0f,8f,0f,.92f,0f,0f,6f,0f,0f,.85f,0f,2f,0f,0f,0f,1f,0f)))
    if(s.filter=="Cool") sat.postConcat(AndroidColorMatrix(floatArrayOf(1f,0f,0f,0f,-4f,0f,1f,0f,0f,0f,0f,0f,1.08f,0f,8f,0f,0f,0f,1f,0f)))
    if(s.filter=="Warm") sat.postConcat(AndroidColorMatrix(floatArrayOf(1.08f,0f,0f,0f,8f,0f,1f,0f,0f,3f,0f,0f,.92f,0f,-5f,0f,0f,0f,1f,0f)))
    return sat.array
}
private fun composeMatrix(s:EditState)=ComposeColorMatrix(matrixFor(s))

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState:Bundle?) { super.onCreate(savedInstanceState); setContent { ElifyEditor() } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElifyEditor() {
    val ctx=androidx.compose.ui.platform.LocalContext.current
    var original by remember { mutableStateOf<Bitmap?>(null) }
    var state by remember { mutableStateOf(EditState()) }
    var category by remember { mutableStateOf("Adjust") }
    var showOriginal by remember { mutableStateOf(false) }
    val cats=listOf("Adjust","Retouch","Filters","Effects","Makeup","Reshape","Body","Crop","Background","Text","Stickers","Collage","Camera")
    val tools=mapOf(
        "Retouch" to listOf("Skin smooth","Blemish / Spot","Dark circles","Teeth whitening","Eye brightening","Healing","Clone","Dodge / Burn","Selective brush"),
        "Effects" to listOf("Glow","Bloom","Vignette","Grain","VHS","RGB split","Lens flare","Light leak","Glitch","Motion blur","Neon","Tilt shift","Film burn"),
        "Makeup" to listOf("Lipstick","Blush","Eyeshadow","Eyeliner","Brows","Lashes","Contour","Highlighter","Hair tint"),
        "Reshape" to listOf("Face width","Jaw","Chin","Nose","Eyes","Lips","Forehead","Liquify","Restore"),
        "Body" to listOf("Waist","Hips","Legs","Arms","Shoulders","Height","Mesh warp","Restore"),
        "Crop" to listOf("Free crop","1:1","4:3","3:4","16:9","9:16","Rotate","Flip","Straighten","Perspective"),
        "Background" to listOf("Manual cutout","Refine mask","Restore mask","Blur","Color","Gradient","Replace background","Object eraser","Chroma selection"),
        "Text" to listOf("Add text","Font","Color","Gradient","Shadow","Stroke","Spacing","Rotation","Opacity"),
        "Stickers" to listOf("Import PNG","Sticker packs","Emoji","Shapes","Frames"),
        "Collage" to listOf("2–20 photos","Templates","Grid","Frame","Spacing","Rounded corners","Background"),
        "Camera" to listOf("Front / Back","Timer 3/5/10s","Flash","Zoom","Grid","Ratio","Exposure","Focus","Live filters")
    )
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri:Uri?-> uri?.let { ctx.contentResolver.openInputStream(it)?.use { stream-> original=BitmapFactory.decodeStream(stream) } }; state=EditState(); showOriginal=false }
    val matrix=composeMatrix(state)
    Scaffold(containerColor=Page,topBar={TopAppBar(title={Text("Elify",color=DeepPurple,fontWeight=FontWeight.Bold)},actions={
        IconButton(onClick={state=EditState()}){Icon(Icons.Default.RestartAlt,"Reset",tint=Purple)}
        IconButton(onClick={showOriginal=!showOriginal}){Icon(Icons.Default.Compare,"Compare",tint=Purple)}
    })}){ pad->
        Column(Modifier.fillMaxSize().padding(pad).padding(horizontal=12.dp)) {
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
                Text("Photo Studio",color=DeepPurple,fontWeight=FontWeight.SemiBold)
                TextButton(onClick={picker.launch("image/*")}){Icon(Icons.Default.AddPhotoAlternate,null);Text(" Open photo")}
            }
            Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(24.dp)).background(Color.White),contentAlignment=Alignment.Center){
                val bmp=original
                if(bmp==null) Column(horizontalAlignment=Alignment.Center){Icon(Icons.Default.Image,color=Purple,modifier=Modifier.size(64.dp));Text("Choose a photo to begin",color=DeepPurple)}
                else Image(bmp.asImageBitmap(),null,Modifier.fillMaxWidth().padding(6.dp),contentScale=ContentScale.Fit,colorFilter=if(showOriginal) null else ColorFilter.colorMatrix(matrix))
            }
            ScrollableTabRow(selectedTabIndex=cats.indexOf(category).coerceAtLeast(0),containerColor=Page,contentColor=Purple){
                cats.forEach{c->Tab(selected=category==c,onClick={category=c},text={Text(c,fontSize=12.sp)})}
            }
            Column(Modifier.fillMaxWidth().height(205.dp).padding(top=6.dp)){
                when(category){
                    "Adjust"->Column{
                        ControlSlider("Brightness",state.brightness,-1f..1f){state=state.copy(brightness=it)}
                        ControlSlider("Contrast",state.contrast,0.25f..2f){state=state.copy(contrast=it)}
                        ControlSlider("Saturation",state.saturation,0f..2f){state=state.copy(saturation=it)}
                        ControlSlider("Warmth",state.warmth,-1f..1f){state=state.copy(warmth=it)}
                        ControlSlider("Exposure",state.exposure,-1f..1f){state=state.copy(exposure=it)}
                        ControlSlider("Fade",state.fade,0f..1f){state=state.copy(fade=it)}
                    }
                    "Filters"->Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                        listOf("Original","Mono","Vintage","Cool","Warm").forEach{f->FilterChip(selected=state.filter==f,onClick={state=state.copy(filter=f)},label={Text(f)})}
                    }
                    else->{
                        Text("Tools",color=DeepPurple,fontWeight=FontWeight.Bold)
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){
                            (tools[category]?: listOf("More tools","Settings","Presets")).forEach{tool->
                                AssistChip(onClick={Toast.makeText(ctx,"$tool — coming in next module",Toast.LENGTH_SHORT).show()},label={Text(tool,fontSize=11.sp)})
                            }
                        }
                        Text("این بخش در حال توسعه است؛ ابزارهای این دسته هنوز پردازش واقعی ندارند.",fontSize=11.sp,color=DeepPurple)
                    }
                }
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Button(onClick={picker.launch("image/*")},modifier=Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=Purple)){Icon(Icons.Default.PhotoLibrary,null);Text("Open")}
                Button(onClick={
                    val src=original
                    if(src==null)Toast.makeText(ctx,"Select a photo first",Toast.LENGTH_SHORT).show()
                    else {
                        val out=Bitmap.createBitmap(src.width,src.height,Bitmap.Config.ARGB_8888)
                        val canvas=Canvas(out);val paint=Paint(Paint.ANTI_ALIAS_FLAG)
                        paint.colorFilter=ColorMatrixColorFilter(AndroidColorMatrix(matrixFor(state)))
                        canvas.drawBitmap(src,0f,0f,paint)
                        val cv=ContentValues().apply{put(MediaStore.Images.Media.DISPLAY_NAME,"Elify_"+System.currentTimeMillis()+".jpg");put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/Elify")}
                        val uri=ctx.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv)
                        uri?.let{ctx.contentResolver.openOutputStream(it)?.use{stream->out.compress(Bitmap.CompressFormat.JPEG,95,stream)}}
                        Toast.makeText(ctx,"Saved in Pictures/Elify",Toast.LENGTH_SHORT).show()
                    }
                },modifier=Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=DeepPurple)){Icon(Icons.Default.Save,null);Text("Save")}
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
@Composable
private fun ControlSlider(title:String,value:Float,range:ClosedFloatingPointRange<Float>,onChange:(Float)->Unit){
    Row(verticalAlignment=Alignment.CenterVertically){
        Text(title,Modifier.width(68.dp),fontSize=12.sp,color=DeepPurple)
        Slider(value=value,onValueChange=onChange,valueRange=range,modifier=Modifier.weight(1f),colors=SliderDefaults.colors(thumbColor=Purple,activeTrackColor=Purple))
        Text(String.format("%.2f",value),Modifier.width(38.dp),fontSize=10.sp)
    }
}
