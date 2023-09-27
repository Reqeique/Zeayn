@file:OptIn(InternalAPI::class)

package com.reqeique.zeayn

import android.content.Context
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import com.reqeique.zeayn.util.LLMApi

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow

import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropScaffoldState
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults.colors
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getSelectedText

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.reqeique.zeayn.ui.theme.ZeaynTheme
import com.reqeique.zeayn.util.Error

import com.reqeique.zeayn.util.Result
import com.reqeique.zeayn.util.Success

import com.reqeique.zeayn.util.ktorHttpClient
import com.reqeique.zeayn.viewmodel.MainViewModel
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import io.ktor.util.InternalAPI
import kotlinx.coroutines.launch

import java.util.concurrent.Executors

@ExperimentalGetImage
private lateinit var auth: FirebaseAuth

class MainActivity : ComponentActivity() {

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      auth = Firebase.auth
      val viewModel: MainViewModel by viewModels()
      setContent {
         ZeaynTheme {

            Surface(
               modifier = Modifier.fillMaxSize(),
               color = MaterialTheme.colorScheme.background
            ) {



               Home(viewModel)
            }
         }
      }
   }
}

@Composable
@Preview(
   showSystemUi = false, showBackground = true, backgroundColor = 0xFFFFFFFF,
   device = "spec:width=411dp,height=891dp"
)
fun SplashScreen(otherContent: @Composable ConstraintLayoutScope.() -> Unit = {}) {

   Scaffold {
      ConstraintLayout(modifier = Modifier.fillMaxSize(), content = {
         val (titleRef, otherContentRef) = createRefs()

         Image(
            painterResource(id = R.drawable.ic_launcher_foreground), null, modifier = Modifier
                 .size(296.dp)
                 .constrainAs(titleRef) {
                     centerTo(parent)

                 }, contentScale = ContentScale.FillBounds
         )
      Surface(modifier = Modifier.constrainAs(otherContentRef) {
            top.linkTo(titleRef.bottom)
            start.linkTo(titleRef.start)
            end.linkTo(titleRef.end)
         }, content = { otherContent() }
         )

      })
      it
   }

}



@ExperimentalGetImage
class OCR(
   val callback: (String) -> Unit
) : ImageAnalysis.Analyzer {
   override fun analyze(imageProxy: ImageProxy) {


      val scanner = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
      val mediaImage = imageProxy.image!!
      mediaImage.let {
         val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

         scanner.process(image)
            .addOnSuccessListener { text ->
               if (text.text.isNotEmpty()) {
                  callback(text.text)
               }
            }
            .addOnFailureListener {

            }
      }
      imageProxy.close()
   }
}

@Composable
@ExperimentalGetImage

fun PreviewViewComposable(result: (String) -> Unit) {
   AndroidView(modifier = Modifier.fillMaxSize(), factory = { context: Context ->

      val cameraExecutor = Executors.newSingleThreadExecutor()
      val previewView = PreviewView(context).also {
         it.scaleType = PreviewView.ScaleType.FILL_CENTER
      }
      val cameraProviderFuture = ProcessCameraProvider.getInstance(context)


      cameraProviderFuture.addListener({
         val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

         val preview = androidx.camera.core.Preview.Builder()
            .build()
            .also {
               it.setSurfaceProvider(previewView.surfaceProvider)
            }

         val imageCapture = ImageCapture.Builder().build()

         @ExperimentalGetImage

         val imageAnalyzer = ImageAnalysis.Builder()
            .build()
            .also {
               it.setAnalyzer(cameraExecutor, OCR { str ->
                  result(str)

               })
            }

         val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA


         cameraProvider.unbindAll()

         // Bind use cases to camera

         cameraProvider.bindToLifecycle(
            context as ComponentActivity, cameraSelector, preview, imageCapture, imageAnalyzer
         )


      }, ContextCompat.getMainExecutor(context))
      previewView

   })
}


sealed class Screen(val route: String, val icon: ImageVector) {
   object Vision : Screen("Vision", Icons.Default.Person)
   object Thought : Screen("Thought", Icons.Default.List)

}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)

@Composable
@androidx.annotation.OptIn(ExperimentalGetImage::class)
fun Home(viewModel: MainViewModel) {
   val coroutineScope = rememberCoroutineScope()

   var query by remember { mutableStateOf(" ") }
   val navController = rememberNavController()
   val backdropScaffoldState = rememberBackdropScaffoldState(initialValue = BackdropValue.Revealed)
   var answer by remember { mutableStateOf(" ") }
   var openDialog by remember { mutableStateOf(false) }
   var error by remember { mutableStateOf(" ") }
   var isLoading by remember { mutableStateOf(false) }
   var selectedItem by remember { mutableStateOf<Int>(0) }

   BackdropScaffold(
      modifier = Modifier.background(MaterialTheme.colorScheme.background),
      scaffoldState = backdropScaffoldState,
      appBar = {
         Column() {
            TopAppBar({
               Row {
                  Text(text = "Zeayn", style = MaterialTheme.typography.titleLarge)
                  Text(
                     " - The unique Vision",
                     fontWeight = FontWeight.Normal,
                     fontSize = 14.sp
                  )


               }


            })

         }

      },
      backLayerContent = {

         val pv = PaddingValues(0.dp)


         val items = listOf(
            Screen.Thought,
            Screen.Vision,


            )



         TabRow(selectedItem, contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            indicator = {


               TabRowDefaults.PrimaryIndicator(
                  Modifier.tabIndicatorOffset(it[selectedItem]),
                  width = (it[selectedItem].contentWidth - 8.dp),
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  shape = RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp)
               )
            }

         ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()

            val currentRoute = navBackStackEntry?.destination?.route
            items.forEachIndexed { index, item ->
             Tab(selected = currentRoute == item.route, onClick = {

                  navController.navigate(item.route) {


                     popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                     }

                     launchSingleTop = true

                     restoreState = true
                  }
                  selectedItem = index
               }, modifier = Modifier
                   .padding(2.dp)
                   .wrapContentWidth()
//                        .background(Color.Red)
                  , text = {
                     Text(
                        item.route, modifier = Modifier
                           .wrapContentWidth()//
                     )
                  })

            }
         }

         NavHost(navController, startDestination = Screen.Thought.route, Modifier.padding(pv)) {
            composable(Screen.Thought.route) {
               com.reqeique.zeayn.Thought(viewModel) {
                  query = it
               }
            }
            composable(Screen.Vision.route) {

               LaunchedEffect(backdropScaffoldState) {
//
                  backdropScaffoldState.reveal()

               }
               Vision(pv) {
                  query = it
//

               }
            }

         }
      },
      frontLayerBackgroundColor = MaterialTheme.colorScheme.onPrimaryContainer,
      frontLayerScrimColor = Color.Transparent,
      frontLayerContent = {

         Column {

            fun load() {
               coroutineScope.launch {
//                        viewModel.addThought(Thought(0,"hey"))

                  isLoading = true

                  when (val a = generateAnswer(query)) {
                     is Error -> {
                        openDialog = true
                        error = a.error.message ?: " "

                     }

                     is Success -> {
                        answer = a.data
                        viewModel.addThought(
                           com.reqeique.zeayn.util.Thought(
                              id = 0,
                              request = query
                           )
                        )
                     }
                  }

                  isLoading = false

               }
            }
            OutlinedTextField(modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .align(Alignment.CenterHorizontally)

                .padding(horizontal = 16.dp, 16.dp), value = query, onValueChange = {
               query = it

            }, maxLines = 10, colors = colors(
               focusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
               unfocusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
               unfocusedTextColor = MaterialTheme.colorScheme.surface,
               focusedTextColor = MaterialTheme.colorScheme.surface
            ), trailingIcon = {
               IconButton(content = {
                  Icon(
                     Icons.Default.Search,
                     contentDescription = null,
                     tint = MaterialTheme.colorScheme.primaryContainer
                  )
               }, onClick = {

                  load()
               })
            }


            )
            if (error.isNotBlank() && openDialog) {


               AlertDialog(properties = DialogProperties(),
                  onDismissRequest = {

                  },

                  text = {
                     Text(error)
                  },
                  confirmButton = {
                     Text("Retry", modifier = Modifier.clickable { load() })
                  },
                  dismissButton = {
//

                     Text("Dismiss", modifier = Modifier.clickable { openDialog = false })

                  }
               )

            }

            Card(
               colors = CardDefaults.cardColors(containerColor = Color.Transparent),
               modifier = Modifier
                   .fillMaxWidth()

                   .height(1000.dp)
            ) {

               val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.View)

               if (isLoading) {

                  Shimmer(shimmerInstance)
               } else {
                  BackdropFrontText(
                     text = answer, modifier = Modifier
                  )
               }


            }


         }


      }) {



   }
}


@Composable
fun Shimmer(shimmer: Shimmer) {


   LazyColumn(modifier = Modifier.fillMaxSize(), content = {
      val rand = (10..16)
      this.items(rand.random()) {
         Card(
            shape = RectangleShape,
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.background),
            modifier = Modifier
                .padding(
                    bottom = 4.dp,
                    top = 4.dp,
                    start = 16.dp,
                    end = (16 + ((rand
                        .shuffled()
                        .random() - 10) * 6)).dp
                )
                .fillMaxWidth()
                .height(24.dp)
                .shimmer()
         ) {}

      }

   })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@androidx.annotation.OptIn(ExperimentalGetImage::class)
fun Vision(pv: PaddingValues, setResult: (String) -> Unit) {
   val scope = rememberCoroutineScope()
   var caption by remember { mutableStateOf("") }
   val scaffoldState = rememberBottomSheetScaffoldState()

   Card(
      modifier = Modifier.padding(
         start = 4.dp,
         end = 4.dp,
         bottom = pv.calculateBottomPadding()
      )
   ) {
      PreviewViewComposable {
         caption = it;
         scope.launch { scaffoldState.bottomSheetState.expand() }
      }


   }

   BottomSheetScaffold(scaffoldState = scaffoldState, modifier = Modifier.graphicsLayer {
      clip = true
      alpha = 0.1f
      // Change the blur radius to adjust the strength of the blur effect
      val blurRadius = 20f
      val blurSigma = blurRadius / 6f

      shape = RectangleShape
      this.ambientShadowColor
      ambientShadowColor = Color.White



      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

         val renderEffectFactory =
            RenderEffect.createBlurEffect(blurSigma, blurSigma, Shader.TileMode.CLAMP)
         this.renderEffect = renderEffectFactory.asComposeRenderEffect()
      }
   }, sheetContent = {
      Column(
          Modifier
              .fillMaxWidth()
//                .background(Color.White.copy(alpha = 0.3f))

              .padding(top = 2.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
         horizontalAlignment = Alignment.Start
      ) {
         var typeWriterResult by remember { mutableStateOf("") }
         var isChecked by remember { mutableStateOf(false) }
         var isClosed by remember { mutableStateOf(false) }
         Row(modifier = Modifier.align(Alignment.End)) {
            //Enabled if theres something selected in the typewriter
            IconButton(
               onClick = { isChecked = true },
               enabled = (typeWriterResult.isNotBlank())
            ) {
               Icon(Icons.Default.Check, contentDescription = null)

            }
            //Enabled if the caption isn't blank
            IconButton(onClick = { isClosed = true }, enabled = caption.isNotBlank()) {
               Icon(Icons.Default.Close, contentDescription = null)
            }
         }

         VisionTypewriterText(caption, 1000, onChange = {

            typeWriterResult = it

         }, onDone = {

         })
         if (isChecked) {

//

            setResult(typeWriterResult)

            isChecked = false
         }
         if (isClosed) caption = ""; isClosed = false

      }
   }) {

   }
}

//@Preview
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

@Composable
fun Thought(
   viewModel: MainViewModel,
   pv: PaddingValues = PaddingValues(0.dp, 0.dp, 0.dp, 0.dp),
   cardOnClick: (String) -> Unit = {}
) {
   var prompt by remember { mutableStateOf("") }
   val coroutineScope = rememberCoroutineScope()
   Column(
       Modifier
           .padding(top = pv.calculateTopPadding())
           .verticalScroll(rememberScrollState())
           .fillMaxWidth()
   ) {
      val hintPrompt = listOf(
         "What is the formula for calculating the speed of light?",
         "What is the theory of relativity?",
         "What is the difference between a hypothesis and a theory?",
         "What is the structure of an atom?",
         "What is the process of photosynthesis?",
         "What is the capital of France?",
         "What is the Pythagorean theorem?",
         "What is the difference between a metaphor and a simile?",
         "What is the definition of a noun?",
         "What is the significance of the Magna Carta?"
      )
      prompt = hintPrompt.random()

      Text(
         text = "Popular Thought \uD83C\uDF10",
         modifier = Modifier.padding(8.dp, 16.dp, 8.dp, 8.dp),
         style = MaterialTheme.typography.titleMedium
      )
      LazyRow(modifier = Modifier, content = {

         this.items(hintPrompt.size) {
            Card(colors = CardDefaults.cardColors(
               containerColor = MaterialTheme.colorScheme.primaryContainer
//                    containerColor = Color(0xFF8F43EE),

            ),
               shape = (MaterialTheme.shapes.small),
               onClick = { cardOnClick(hintPrompt[it]) },
               modifier = Modifier
                   .padding(start = 8.dp, bottom = 8.dp, top = 8.dp)
                   .requiredHeight(170.dp),
               content = {
                  Text(
                     """"${hintPrompt[it]}"""",
                      Modifier
                          .width(150.dp)
                          .padding(8.dp), maxLines = 10, softWrap = true
                  )
               })
         }
      })
      Text(
         text = "My Thought \uD83D\uDCC3",
         Modifier.padding(8.dp),
         style = MaterialTheme.typography.titleMedium
      )


      Box(modifier = Modifier.height(200.dp)) {
         val myThought by viewModel.getAllThought()
         var _idx by remember { mutableIntStateOf(-1) }
         LazyColumn(modifier = Modifier, content = {

            this.itemsIndexed(myThought) { idx, thought ->
               Card(colors = CardDefaults.cardColors(
                  containerColor = MaterialTheme.colorScheme.primaryContainer

               ), shape = (MaterialTheme.shapes.small), modifier = Modifier
                   .combinedClickable(onLongClick = {
                       _idx = idx
                   }, onClick = {


                       cardOnClick(thought.request)
                   })
                   .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                   .heightIn(max = 75.dp), content = {
                  ConstraintLayout(modifier = Modifier.fillMaxSize()) {

                     val (text, dateTime) = createRefs()
                     if (idx == _idx) {
                        DropdownMenu(
                           expanded = true,
                           onDismissRequest = { _idx = -1 }) {
                           DropdownMenuItem(text = { Text("Delete") }, onClick = {
                              coroutineScope.launch {
                                 viewModel.deleteThought(thought)
                                 _idx = -1
                              }

                           })
                        }
                     }



                     Text(
                        """"${thought.request}"""",
                         Modifier
                             .constrainAs(text) {
                                 this.centerTo(parent)
                             }
                             .fillMaxWidth()
                             .padding(8.dp), maxLines = 100, softWrap = true
                     )


                     Text(
                        modifier = Modifier
                            .padding(0.dp, 0.dp, 4.dp, 4.dp)
                            .constrainAs(dateTime) {
                                this.end.linkTo(parent.end)

                                this.bottom.linkTo(parent.bottom)
                            },
                        text = thought.dateTime,
                        style = MaterialTheme.typography.bodySmall
                     )


                  }
               })
            }


         })

      }


   }
}

@Composable
fun VisionTypewriterText(
   text: String,
   durationMillis: Int,
   textStyle: TextStyle = LocalTextStyle.current,
   onDone: () -> Unit = {},
   onChange: (String) -> Unit
) {
   val transition = updateTransition(targetState = text.length, label = "textLength")
   val textLength by transition.animateInt(
      transitionSpec = { tween(durationMillis) },
      label = "textLength"
   ) { targetTextLength ->
      targetTextLength
   }

   if (textLength == text.length) {
      onDone()
   }
   val t = AnnotatedString(text.take(textLength))
   var textState by remember {
      mutableStateOf(TextFieldValue(t))
   }

   textState = textState.copy(t)





   SelectionContainer {
      BasicTextField(
         value = textState,
         onValueChange = {
            textState =
               textState.copy(selection = it.selection);onChange(it.getSelectedText().text)
         },
         textStyle = textStyle.copy(MaterialTheme.colorScheme.onSurface),
         modifier = Modifier.onFocusChanged {
            it
         },
         onTextLayout = {
            textState = textState.copy(selection = TextRange(0, textState.text.length))
            onChange(textState.getSelectedText().text)
         })

   }

}

@Composable
fun ThoughtTypeWriter(
   text: String, modifier: Modifier = Modifier, style: TextStyle, durationMillis: Int,
) {
   val transition = updateTransition(targetState = text.length, label = "textLength")
   val textLength by transition.animateInt(
      transitionSpec = { tween(durationMillis) },
      label = "textLength"
   ) { targetTextLength ->
      targetTextLength
   }


   val t = (text.take(textLength))
   Text(
      text = t,
      modifier = modifier,
      style = style,

      )


}

@Composable
@Preview
fun BackdropFrontText(text: String = "hey", modifier: Modifier = Modifier) {
   ThoughtTypeWriter(
      text,
      modifier = modifier.padding(horizontal = 16.dp),
      style = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.surface),
      1000,
   )
}

suspend fun generateAnswer(question: String): Result {
   return LLMpi(ktorHttpClient).textCompletion(question)
}