package com.niked.fatless.domain.model

enum class RpsChoice(val emoji: String) {
    ROCK("🪨"),
    PAPER("📄"),
    SCISSORS("✂️")
}

enum class GameResult {
    WIN, LOSS, DRAW
}

data class RpsGameState(
    val playerChoice: RpsChoice? = null,
    val computerChoice: RpsChoice? = null,
    val result: GameResult? = null,
    val playerScore: Int = 0,
    val computerScore: Int = 0,
    val logMessage: String = "Сделай свой ход, Чувак!"
)
