package com.example.trainboard
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
/*
* Departure time , arrival time, cost, ticket type, status,
* */


@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class TrainInfo(
    val numberOfAdults: Int,
    val numberOfChildren: Int,
    val outboundJourneys: List<OutboundJourney>,

)
@OptIn(kotlinx.serialization.InternalSerializationApi::class)

@Serializable
data class OutboundJourney(
    val originStation: StationInfo,
    val destinationStation: StationInfo,
    val departureTime: String,
    val arrivalTime: String,
    val status: String,
    val tickets: List<Ticket>
    // etc.
)
@OptIn(kotlinx.serialization.InternalSerializationApi::class)

@Serializable
data class StationInfo(val displayName: String, val nlc: String, val crs: String)
@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class Ticket(
    val fareId: String,
    val ticketOptionToken: String,
    val priceInPennies: Int

)
