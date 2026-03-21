package com.taigatkd.karamemo.domain.model

const val MIN_KARAOKE_MACHINE_SETTING = 0
const val MAX_KARAOKE_MACHINE_SETTING = 50
const val DEFAULT_KARAOKE_MACHINE_SETTING = 25

data class KaraokeMachineSettings(
    val bgm: Int = DEFAULT_KARAOKE_MACHINE_SETTING,
    val mic: Int = DEFAULT_KARAOKE_MACHINE_SETTING,
    val echo: Int = DEFAULT_KARAOKE_MACHINE_SETTING,
    val music: Int = DEFAULT_KARAOKE_MACHINE_SETTING,
)

fun defaultMachineSettings(): Map<KaraokeMachine, KaraokeMachineSettings> =
    KaraokeMachine.entries.associateWith { KaraokeMachineSettings() }
