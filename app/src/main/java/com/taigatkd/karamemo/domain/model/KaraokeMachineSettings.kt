package com.taigatkd.karamemo.domain.model

data class KaraokeMachineSettings(
    val bgm: Int = 25,
    val mic: Int = 25,
    val echo: Int = 25,
    val music: Int = 25,
)

fun defaultMachineSettings(): Map<KaraokeMachine, KaraokeMachineSettings> =
    KaraokeMachine.entries.associateWith { KaraokeMachineSettings() }

