package kr.goldenmine.inuminecraftlauncher.ui

import lombok.AllArgsConstructor
import lombok.Getter

@AllArgsConstructor
@Getter
class ClientInfo(
    val clientId: String,
    val clientSecret: String
) {
}