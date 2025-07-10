package com.example.trainboard
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Display.Mode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    val selectedOriginStation = remember { mutableStateOf<Station?>(null)}
    val selectedDestinationStation = remember { mutableStateOf<Station?>(null)}
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .height(200.dp)
                .background(color = Color.White, shape = RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))

        )
        {
            Column(
                modifier = Modifier
                    .padding(10.dp),
            )
            {
                Text("Where", fontWeight = FontWeight.Bold, modifier =  Modifier.padding(7.dp, 4.dp, 0.dp, 0.dp))
                ExposedDropdown("From", selectedOriginStation, Modifier.padding(10.dp, 10.dp, 10.dp, 5.dp))
                ExposedDropdown("To", selectedDestinationStation, Modifier.padding(10.dp, 5.dp, 10.dp, 10.dp))

            }

        }
        SearchButton(selectedOriginStation, selectedDestinationStation)
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
fun SearchButton (selectedOriginStation: Station, selectedDestinationStation: MutableState<Station>)
{   val context = LocalContext.current
    val buttonText = remember { mutableStateOf("Find route") }
    val url = getUrl(selectedOriginStation.value.code, selectedDestinationStation.value.code)
    Button(

        onClick = {

            if (selectedOriginStation.value.name != "" && selectedDestinationStation.value.name != "")
                openUrl(url, context)
            else
                if (selectedOriginStation.value == selectedDestinationStation.value)
                    buttonText.value = "Choose different staions"
                    buttonText.value = "Stations not chosen"


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
fun ExposedDropdown(name: String, selectedStation: MutableState<String>, position: Modifier) {

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
            modifier = position.menuAnchor()
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
            stations.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            // onStationChange(station)
                            selectedStation.value = selectionOption
                            expanded = false

                        }
                    )
                }
            }
        }
    }
}


