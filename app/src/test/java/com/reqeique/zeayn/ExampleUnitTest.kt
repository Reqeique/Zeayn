package com.reqeique.zeayn

import android.util.Log
import com.reqeique.zeayn.util.Generation
import com.reqeique.zeayn.util.GenerationResponse
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
private const val dot = "{\"id\":\"b770bbdb-24a4-4e97-aa6a-78c2a20ae941\",\"generations\":[{\"id\":\"1365427a-44f1-4034-a4ad-857b9ebb0a62\",\"text\":\"\\nDid you mean to say \\\"Did you do this?\\\"\"}],\"prompt\":\" ydydydts\",\"meta\":{\"api_version\":{\"version\":\"2021-11-08\"}}}"
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
class ExampleUnitTest {
    @Test
    fun test_generation() {
//        assertEquals(4, 2 + 2)
        print(extractGenerations(dot))
    }
}

fun extractGenerations(jsonString: String): List<Generation>? {
    return try {
        val json = Json {
            ignoreUnknownKeys = true
        }


        val response = json.decodeFromString<GenerationResponse>(jsonString)
        response.generations
    } catch (e: Exception) {
        Log.d("API-1", "EX $e")
        null
    }
}