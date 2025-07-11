package com.example.trainboard
import android.R
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.controls.Control
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Popup
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrainBoardTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen()
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
@RequiresApi(Build.VERSION_CODES.O)
fun getDatefromUTC (utcTime: String):String
{
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMM") // e.g. "Friday, 11 Jul 2025"
               // e.g. "09:30"
    val time = ZonedDateTime.parse(utcTime)
    val niceDate = time.format(dateFormatter)

    return niceDate
}

@RequiresApi(Build.VERSION_CODES.O)
fun getTimefromUTC (utcTime: String):String
{
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val time = ZonedDateTime.parse(utcTime)
    val niceTime = time.format(timeFormatter)

    return niceTime
}
@RequiresApi(Build.VERSION_CODES.O)
fun getTrainFaresApiUrl(originCode: String, destinationCode: String, numberOfAdults: Int, numberOfChildren: Int): String{
    val utcTime: Instant = Instant.now()

    val tags =  "/v1/fares?originStation=${originCode}&destinationStation=${destinationCode}&noChanges=false&avoidLondon=false&outboundDateTime=${utcTime}&outboundIsArriveBy=false&inboundIsArriveBy=false&numberOfChildren=${numberOfChildren}&numberOfAdults=${numberOfAdults}&doSplitTicketing=false&includeSpecialMenus=false"
    return tags
}
@RequiresApi(Build.VERSION_CODES.O)
fun getDurationMin (utcTimeDepart: String, utcTimeArrive: String):String
{
    val timeDepart = getTimefromUTC(utcTimeDepart)
    val timeArrive = getTimefromUTC(utcTimeArrive)
    var minutesDepart = (timeDepart[0]-'0')*600 + (timeDepart[1]-'0')*60 + (timeDepart[3]-'0')*10 + (timeDepart[4]-'0')
    var minutesArrive = (timeArrive[0]-'0')*600 + (timeArrive[1]-'0')*60 + (timeArrive[3]-'0')*10 + (timeArrive[4]-'0')
    return (minutesArrive - minutesDepart).toString()
}
//fun getTrainInfoFromJSON()


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val trainInfoViewModel: TrainInfoViewModel = viewModel()
    NavHost(navController = navController, startDestination = "findTicketPage") {
        composable("findTicketPage") { FindTicketPage(navController, trainInfoViewModel) }
        composable("ticketResultsPage") { TicketResultsPage(trainInfoViewModel) }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FindTicketPage(navController: NavController, viewModel: TrainInfoViewModel)
{
    var selectedOriginStation by remember { mutableStateOf<Station>(Station("", ""))}
    var selectedDestinationStation by remember { mutableStateOf<Station>(Station("", ""))}
    var numberOfAdults by remember { mutableStateOf<Int>(1) }
    var numberOfChildren by remember { mutableStateOf<Int>(0) }

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
        WhiteContainer(modifier = Modifier.height(170.dp)) {
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
        WhiteContainer(modifier = Modifier.height(200.dp)) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    "Who",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(7.dp, 4.dp, 0.dp, 0.dp)
                )
                PassengerDropdown("Adults (16+) :", numberOfAdults, onNumberChange = {numberOfAdults = it }, modifier = Modifier.padding(0.dp) )
                PassengerDropdown("Children (1-15):", numberOfChildren, onNumberChange = {numberOfChildren = it }, modifier = Modifier.padding(0.dp) )

            }
        }



        SearchButton(navController, selectedOriginStation, selectedDestinationStation,numberOfAdults, numberOfChildren, Modifier.fillMaxWidth().padding(20.dp), viewModel)
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TicketResultsPage(viewModel: TrainInfoViewModel){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFF731522)),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text("LNER", color = Color.White, fontSize = 35.sp, modifier = Modifier.padding(top = 30.dp, bottom = 20.dp))
        WhiteContainer(modifier = Modifier.height(130.dp)){
                Column(
                    modifier = Modifier.padding(15.dp).fillMaxWidth(),


                    )
                {
                        Text("${viewModel.trainInfo?.outboundJourneys!![0].originStation.displayName} → ${viewModel.trainInfo?.outboundJourneys!![0].destinationStation.displayName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp)


                        Text("${getDatefromUTC(viewModel.trainInfo?.outboundJourneys!![0].departureTime)}                       Adults: ${viewModel.trainInfo?.numberOfAdults}, Children: ${viewModel.trainInfo?.numberOfChildren}",

                            fontSize = 15.sp)

                }
        }

        for (journey in viewModel.trainInfo?.outboundJourneys!!)
        {
            WhiteContainer (
                modifier = Modifier.height(100.dp)

            ){
                Column (
                    modifier = Modifier.padding(15.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                        )
                {
                   // Text("${getDatefromUTC(journey.departureTime)} - ${getDatefromUTC( journey.arrivalTime)}")
                    Row{
                        Text("${getTimefromUTC(journey.departureTime)} → ${getTimefromUTC( journey.arrivalTime)} " ,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp)
                        Text("           ${getDurationMin(journey.departureTime, journey.arrivalTime)}min")
                    }
                   // Text("Duration: ${getDurationMin(journey.departureTime, journey.arrivalTime)}min")
                    val totalPrice = journey.tickets.minOf{it.priceInPennies}/100
                    Text("£${totalPrice}")
                }
            }




        }

    }
    if (viewModel.trainInfo != null)
        Text(viewModel.trainInfo!!.numberOfChildren.toString(), color = Color.Red)


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

    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 26.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp, 10.dp,20.dp, 10.dp )
            .background(color = Color.White, shape = RoundedCornerShape(16.dp))
    )

    {

            content()

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


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SearchButton (navController: NavController, selectedOriginStation: Station, selectedDestinationStation: Station, numberOfAdults: Int, numberOfChildren: Int,modifier: Modifier, viewModel: TrainInfoViewModel)
{   val context = LocalContext.current
    val buttonText = "Find route"
    var trainInfo by remember { mutableStateOf<TrainInfo?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showLoading by remember { mutableStateOf(false)}
    val coroutineScope = rememberCoroutineScope()
    val client = ApiClient()
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
                else -> {
                    val apiTags = getTrainFaresApiUrl(selectedOriginStation.code, selectedDestinationStation.code, numberOfAdults, numberOfChildren)
                    showLoading = true
                    coroutineScope.launch {
                        var json = ""
                        try {
                            json = client.get(apiTags)
                        }catch(e: Exception){
                            errorMessage = "failed to fetch from API, please try again!"
                            showErrorDialog = true
                            showLoading = false

                        }
                        val jsonParser = Json{ ignoreUnknownKeys = true}
                        trainInfo = jsonParser.decodeFromString<TrainInfo>(json)
                        showLoading = false

                    }


                }
            }

        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        modifier = modifier
    )
    {
        Text(buttonText)
    }
    if (!showLoading && !showErrorDialog && trainInfo != null){
        navController.navigate("ticketResultsPage")
        viewModel.trainInfo = trainInfo
    }
    if (showLoading) LoadingIndicator(showLoading)
    if (showErrorDialog) {
        ErrorAlert(
            message = errorMessage,
            onDismiss = { showErrorDialog = false }
        )
    }


}
@Composable
fun LoadingIndicator(loading: Boolean) {
    if (!loading) return

    CircularProgressIndicator(
        modifier = Modifier.width(64.dp),
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerDropdown(headerText: String, selectedNumber: Int, onNumberChange: (Int) -> (Unit), modifier: Modifier) {

    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }

    ) {
        TextField(
            value = selectedNumber.toString(),
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
            (1..3).forEach { numberOfPassengers ->
                DropdownMenuItem(
                    text = { Text(numberOfPassengers.toString()) },
                    onClick = {
                        onNumberChange(numberOfPassengers)
                        expanded = false

                    }
                )
            }
        }
    }
}




