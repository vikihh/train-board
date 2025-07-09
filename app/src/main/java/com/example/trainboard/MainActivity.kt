package com.example.trainboard
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri


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
@Composable
fun Page()
{



    val selectedOriginStation = remember { mutableStateOf("")}
    val selectedDestinationStation = remember { mutableStateOf("")}
    val client = ApiClient()
    MyScreen(client)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Red),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text("LNER", color = Color.White, fontSize = 25.sp, modifier = Modifier.padding(top = 0.dp))
        Row (){
            SimpleExposedDropdown("From", selectedOriginStation, selectedDestinationStation)
            SimpleExposedDropdown("To", selectedDestinationStation, selectedOriginStation)

        }
        ButtonToLNER(selectedOriginStation, selectedDestinationStation)
    }

}


fun generateUrl(selectedOriginStation: MutableState<String>, selectedDestinationStation: MutableState<String>): String{
    val stationCodes = mutableMapOf("London" to "KGX", "Edinburgh" to "EDB", "Oxford" to "OXF", "Bristol" to "BRI", "Liverpool" to "LVC")
    return "https://www.lner.co.uk/travel-information/travelling-now/live-train-times/depart/${stationCodes[selectedOriginStation.value]}/${stationCodes[selectedDestinationStation.value]}/#tab_livedepartures"
}


@Composable
fun MyScreen(apiClient: ApiClient) {
    LaunchedEffect(Unit) {
        val result = try {
            apiClient.get("/v1/silverSeek/cheapestTickets?originCrs=KGX&destinationCrs=LDS&ticketType=return&totalDays=2&searchFirstClassOnly=false") 
        } catch (e: Exception) {
            "Error: ${e.message}"
        }

        println("Response: $result")
    }
}
@Composable
fun ButtonToLNER (selectedOriginStation: MutableState<String>, selectedDestinationStation: MutableState<String>)
{   val context = LocalContext.current
    val buttonText = remember { mutableStateOf("Find route") }
    val url = generateUrl(selectedOriginStation, selectedDestinationStation)
    Button(

        onClick = {

            if (selectedOriginStation.value != "" && selectedDestinationStation.value != "")
            {
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())

                context.startActivity(intent)
            }
            else {

                buttonText.value = "Stations not chosen"
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


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleExposedDropdown(name: String, selectedStation: MutableState<String>,selectedOtherStation: MutableState<String>) {
    val options = listOf("London", "Edinburgh", "Oxford", "Liverpool", "Bristol")
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedStation.value,
            onValueChange = {},
            readOnly = true,
            label = { Text(name) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier.menuAnchor().clickable { expanded = !expanded }.padding(5.dp).width(150.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                if (selectionOption != selectedOtherStation.value){
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            selectedStation.value = selectionOption
                            expanded = false

                        }
                    )
                }
            }
        }
    }
}


