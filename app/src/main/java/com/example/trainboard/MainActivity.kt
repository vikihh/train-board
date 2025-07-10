package com.example.trainboard
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
    val selectedStartStation = remember { mutableStateOf("")}
    val selectedEndStation = remember { mutableStateOf("")}
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
                ExposedDropdown("From", selectedStartStation, selectedEndStation, Modifier.padding(10.dp, 10.dp, 10.dp, 5.dp))
                ExposedDropdown("To", selectedEndStation, selectedStartStation, Modifier.padding(10.dp, 5.dp, 10.dp, 10.dp))

            }

        }

        ButtonToLNER(selectedStartStation, selectedEndStation)
    }

}


fun generateUrl(selectedStartStation: MutableState<String>, selectedEndStation: MutableState<String>): String{
    val codeDictionary = mutableMapOf("London" to "KGX", "Edinburgh" to "EDB", "Oxford" to "OXF", "Bristol" to "BRI", "Liverpool" to "LVC")
    return "https://www.lner.co.uk/travel-information/travelling-now/live-train-times/depart/${codeDictionary[selectedStartStation.value]}/${codeDictionary[selectedEndStation.value]}/#tab_livedepartures"
}


@Composable
fun MyScreen(apiClient: ApiClient) {
    LaunchedEffect(Unit) {
        val result = try {
            apiClient.get("/v1/silverSeek/cheapestTickets?originCrs=KGX&destinationCrs=LDS&ticketType=return&totalDays=2&searchFirstClassOnly=false") // suspending call
        } catch (e: Exception) {
            "Error: ${e.message}"
        }

        println("Response: $result")
    }
}
@Composable
fun ButtonToLNER (selectedStartStation: MutableState<String>, selectedEndStation: MutableState<String>)
{   val context = LocalContext.current
    val buttonText = remember { mutableStateOf("Find route") }
    val url = generateUrl(selectedStartStation, selectedEndStation)
    Button(

        onClick = {

            if (selectedStartStation.value != "" && selectedEndStation.value != "")
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
fun ExposedDropdown(name: String, selectedOptionText: MutableState<String>,selectedOtherOptionText: MutableState<String>, position: Modifier) {
    val options = listOf("London", "Edinburgh", "Oxford", "Liverpool", "Bristol")
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }

    ) {
        TextField(
            value = selectedOptionText.value,
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
            options.forEach { selectionOption ->
                if (selectionOption != selectedOtherOptionText.value){
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            selectedOptionText.value = selectionOption
                            expanded = false

                        }
                    )
                }
            }
        }
    }
}


