package com.niked.fatless.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.niked.fatless.domain.repository.IWorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WorkoutCreateViewModel @Inject constructor(
    private val repository: IWorkoutRepository
) : ViewModel() {
    // Тут будет логика добавления интервалов в список перед сохранением
}