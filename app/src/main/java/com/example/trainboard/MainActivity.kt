package com.example.trainboard
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainboard.ui.theme.TrainBoardTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrainBoardTheme {
                MainScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun getDateFromUTC (utcTime: String):String
{
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMM")
    val zonedTime = ZonedDateTime.parse(utcTime)
    val formatedDate = zonedTime.format(dateFormatter)
    return formatedDate
}

@RequiresApi(Build.VERSION_CODES.O)
fun getTimeFromUTC (utcTime: String):String
{
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val zonedTime = ZonedDateTime.parse(utcTime)
    val formatedTime = zonedTime.format(timeFormatter)
    return formatedTime
}
@RequiresApi(Build.VERSION_CODES.O)
fun enforceValidTime(currentTime: Instant): Instant{
    val utcTime: Instant = Instant.now()
    return if (currentTime.isBefore(utcTime)) utcTime
    else currentTime

}
@RequiresApi(Build.VERSION_CODES.O)
fun getTrainFaresApiUrl(originCode: String, destinationCode: String, numberOfAdults: Int, numberOfChildren: Int, currentTime: Instant): String{
    return "/v1/fares?originStation=${originCode}&destinationStation=${destinationCode}&noChanges=false&avoidLondon=false&outboundDateTime=${enforceValidTime(currentTime)}&outboundIsArriveBy=false&inboundIsArriveBy=false&numberOfChildren=${numberOfChildren}&numberOfAdults=${numberOfAdults}&doSplitTicketing=false&includeSpecialMenus=false"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(modifier: Modifier) {
    val navController = rememberNavController()
    val trainInfoViewModel: TrainInfoViewModel = viewModel()
    NavHost(navController = navController, startDestination = "findTicketPage", modifier = modifier) {
        composable("findTicketPage") { FindTicketPage(navController, trainInfoViewModel) }
        composable("ticketResultsPage") { TicketResultsPage(trainInfoViewModel) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FindTicketPage(navController: NavController, viewModel: TrainInfoViewModel)
{
    var selectedOriginStation by remember { mutableStateOf(Station("", ""))}
    var selectedDestinationStation by remember { mutableStateOf(Station("", ""))}
    var numberOfAdults by remember { mutableIntStateOf(1) }
    var numberOfChildren by remember { mutableIntStateOf(0) }
    var currentTime by remember { mutableStateOf(Instant.now()) }
    var showTimePicker by remember { mutableStateOf(false) }

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
                        Text("${currentTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy - HH:mm"))}")
                    }


                    Button(onClick = {showTimePicker = true}, modifier = Modifier.fillMaxWidth().height(35.dp) , border = BorderStroke(1.dp, Color.Black),colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray,
                            contentColor = Color.Black
                        ))
                        {Text("CLICK TO CHOOSE TIME") }



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
        if (showTimePicker) {
           TimePickerAlert(currentTime = currentTime, onTimeChange = {currentTime = it}, onConfirm = { showTimePicker = false })
        }
        SearchButton(navController, selectedOriginStation, selectedDestinationStation,numberOfAdults, numberOfChildren, Modifier.fillMaxWidth().padding(20.dp), currentTime, viewModel)
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
                        Text("${viewModel.trainInfo?.outboundJourneys!![0].originStation.displayName} -> ${viewModel.trainInfo?.outboundJourneys!![0].destinationStation.displayName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp)


                        Text("${getDateFromUTC(viewModel.trainInfo?.outboundJourneys!![0].departureTime)}                       Adults: ${viewModel.trainInfo?.numberOfAdults}, Children: ${viewModel.trainInfo?.numberOfChildren}",

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

                    Row{
                        Text("${getTimeFromUTC(journey.departureTime)} → ${getTimeFromUTC( journey.arrivalTime)} " ,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp)
                        Text("           ${journey.journeyDurationInMinutes.div(60) }h ${journey.journeyDurationInMinutes%60}mins")
                    }

                    var totalPrice = 0
                    try{
                        totalPrice = journey.tickets.minOf{it.priceInPennies}/100
                    }
                    catch(_: Exception){

                    }
                    if (totalPrice != 0) Text("£${totalPrice}")
                    else Text("Price Not available")

                    }
                }
            }




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
            shape = RoundedCornerShape(15.dp)
        )
        .clip(RoundedCornerShape(15.dp))
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerAlert(
    currentTime: Instant,
    onTimeChange: (Instant) -> Unit,
    onConfirm: () -> Unit
) {
    val zonedTime = remember(currentTime) {
        ZonedDateTime.ofInstant(currentTime, ZoneId.systemDefault())
    }

    val timePickerState = rememberTimePickerState(
        initialHour = zonedTime.hour,
        initialMinute = zonedTime.minute,
        is24Hour = true
    )

    BasicAlertDialog(
        {},
        content =  {
            Column (verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally){
            TimePicker(state = timePickerState)
            Button(
                onClick = {
                    val updated = zonedTime
                        .withHour(timePickerState.hour)
                        .withMinute(timePickerState.minute)
                    onTimeChange(updated.toInstant())
                    onConfirm()
                }
        )   {
            Text("Click to Confirm")
            }
        }
    })

}



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SearchButton (navController: NavController, selectedOriginStation: Station, selectedDestinationStation: Station, numberOfAdults: Int, numberOfChildren: Int, modifier: Modifier, currentTime: Instant, viewModel: TrainInfoViewModel)
{
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
                    val apiTags = getTrainFaresApiUrl(selectedOriginStation.code, selectedDestinationStation.code, numberOfAdults, numberOfChildren, currentTime)
                    showLoading = true
                    coroutineScope.launch {
                        var json = ""
                        try {
                            json = client.get(apiTags)
                            println(json)
                        }catch(_: Exception){
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
    LoadingIndicator(showLoading)
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




