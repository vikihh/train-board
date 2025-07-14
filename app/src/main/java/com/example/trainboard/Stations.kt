package com.example.trainboard

data class Station(val name:String, val code: String)

val stations = listOf(Station("London", "KGX"),
                      Station("Edinburgh", "EDB"),
                      Station("Oxford", "OXF"),
                      Station("Bristol", "BRI"),
                      Station("Liverpool" ,"LVC"))
