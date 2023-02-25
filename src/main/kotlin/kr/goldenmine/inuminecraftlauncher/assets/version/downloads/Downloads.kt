package kr.goldenmine.inuminecraftlauncher.assets.version.downloads

import com.google.gson.annotations.SerializedName

data class Downloads(
    @SerializedName("client") val client: Client,
    @SerializedName("client_mappings") val clientMappings: Client,
    @SerializedName("server") val server: Server,
    @SerializedName("server_mappings") val serverMappings: Server,
)