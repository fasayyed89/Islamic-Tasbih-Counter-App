package com.example.tasbihcounter.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// Keep the original DataRepository to avoid breaking the template's test file.
interface DataRepository {
    val data: Flow<List<String>>
}

class DefaultDataRepository : DataRepository {
    override val data: Flow<List<String>> = flow { emit(listOf("Android")) }
}
