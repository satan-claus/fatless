package com.niked.fatless.domain.model

sealed class WorkoutState {
    object READY : WorkoutState()
    object RUNNING : WorkoutState()
    object PAUSED : WorkoutState()
    object COMPLETED : WorkoutState()
}