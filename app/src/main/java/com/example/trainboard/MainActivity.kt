package com.example.trainboard
import android.R
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Display.Mode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.collection.emptyLongSet
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainboard.ui.theme.TrainBoardTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Popup
import androidx.core.net.toUri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrainBoardTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Page()
                }
            }
        }
    }
}


fun getUrl(originCode: String, destinationCode: String): String{
    return "https://www.lner.co.uk/travel-information/travelling-now/live-train-times/depart/${originCode}/${destinationCode}/#tab_livedepartures"
}
fun openUrl(url: String, context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    context.startActivity(intent)
}
@Composable
fun Page()
{
    var selectedOriginStation by remember { mutableStateOf<Station>(Station("", ""))}
    var selectedDestinationStation by remember { mutableStateOf<Station>(Station("", ""))}
    val client = ApiClient()
    MyScreen(client)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFF731522)),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text("LNER", color = Color.White, fontSize = 35.sp, modifier = Modifier.padding(top = 30.dp, bottom = 20.dp))
        WhiteContainer {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    "Where",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(7.dp, 4.dp, 0.dp, 0.dp)
                )
                StationDropdown(
                    "From",
                    selectedOriginStation,
                    onStationChange = { selectedOriginStation = it },
                    Modifier.padding(10.dp, 10.dp, 10.dp, 5.dp)
                )
                StationDropdown(
                    "To",
                    selectedDestinationStation,
                    onStationChange = { selectedDestinationStation = it },
                    Modifier.padding(10.dp, 5.dp, 10.dp, 10.dp)
                )
            }

        }
        WhiteContainer {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("When", fontWeight = FontWeight.Bold, modifier =  Modifier.padding(7.dp, 4.dp, 0.dp, 0.dp))
                Column(modifier = Modifier.padding(10.dp)){
                    RoundedOutlinedBox {
                        Text("SINGLE | RETURN | OPEN RETURN")
                    }
                    RoundedOutlinedBox {
                        Text("TODAY     NOW")
                    }

                }
            }

        }
        WhiteContainer {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    "Who",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(7.dp, 4.dp, 0.dp, 0.dp)
                )

            }
        }


        SearchButton(selectedOriginStation, selectedDestinationStation)
    }

}
@Composable
fun RoundedOutlinedBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
){
    Box(contentAlignment = Alignment.Center, modifier = modifier
        .fillMaxWidth()
        .padding(5.dp)
        .background(color = Color.White, shape = RoundedCornerShape(10.dp))
        .height(30.dp)
        .border(
            width = 1.dp,
            color = Color.Black,
            shape = RoundedCornerShape(10.dp)
        )
        .clip(RoundedCornerShape(10.dp))
    ) {
        content()
    }

}

@Composable
fun WhiteContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp, 10.dp,20.dp, 10.dp )
            .height(200.dp)
            .background(color = Color.White, shape = RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
    ) {

            content()

    }
}

@Composable
fun MyScreen(apiClient: ApiClient) {
    LaunchedEffect(Unit) {
        val result = try {
            apiClient.get("/v1/silverSeek/cheapestTickets?originCrs=KGX&destinationCrs=LDS&ticketType=return&totalDays=2&searchFirstClassOnly=false") // suspDestinationing call
        } catch (e: Exception) {
            "Error: ${e.message}"
        }

        println("Response: $result")
    }
}
@Composable
fun ErrorAlert(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        },
        title = { Text("Error:") },
        text = { Text(message) }
    )
}


@Composable
fun SearchButton (selectedOriginStation: Station, selectedDestinationStation: Station)
{   val context = LocalContext.current
    val buttonText = remember { mutableStateOf("Find route") }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    Button(

        onClick = {
            when {


                (selectedOriginStation.name.isEmpty() || selectedDestinationStation.name.isEmpty()) -> {
                    errorMessage = "Please select both origin and destination stations."
                    showErrorDialog = true
                }
                (selectedOriginStation == selectedDestinationStation) -> {
                    errorMessage = "Please Select Different Origin And Destination"
                    showErrorDialog = true
                }
                else -> openUrl(getUrl(selectedOriginStation.code, selectedDestinationStation.code), context)
            }

        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        )
    )
    {
        Text(buttonText.value)
    }
    if (showErrorDialog) {
        ErrorAlert(
            message = errorMessage,
            onDismiss = { showErrorDialog = false }
        )
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationDropdown(headerText: String, selectedStation: Station, onStationChange: (Station) -> (Unit), modifier: Modifier) {

    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }

    ) {
        TextField(
            value = selectedStation.name,
            onValueChange = {},
            readOnly = true,
            label = { Text(headerText) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = modifier.menuAnchor()
                .clickable { expanded = !expanded }
                .fillMaxWidth()
                .clip( shape = RoundedCornerShape(10.dp)),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            )

        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }

        ) {
            stations.forEach { station ->
                    DropdownMenuItem(
                        text = { Text(station.name) },
                        onClick = {
                            onStationChange(station)
                            expanded = false

                        }
                    )
                }
            }
        }
    }





