package cielo.sample.uriapp

data class  CancelRequest(val id: String, val clientID: String, val accessToken: String, val cieloCode: String, val authCode: String, val merchantCode: String, val value: Long)