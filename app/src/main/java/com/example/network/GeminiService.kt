package com.example.network

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    /**
     * Checks if the Gemini API key is configured.
     */
    fun isApiKeyAvailable(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !key.isNullOrEmpty() && key != "MY_GEMINI_API_KEY" && !key.contains("PLACEHOLDER")
    }

    /**
     * Helper to execute prompt against Gemini with local mock fallback in case of errors or missing keys.
     */
    suspend fun generateContent(prompt: String, systemInstruction: String? = null): String {
        val key = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyAvailable()) {
            Log.w(TAG, "Gemini API key is not configured. Falling back to local heuristic simulation.")
            return simulateLocalCoachResponse(prompt)
        }

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.7f),
            systemInstruction = systemInstruction?.let {
                GeminiContent(parts = listOf(GeminiPart(text = it)))
            }
        )

        return try {
            val response = service.generateContent(key, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!responseText.isNullOrEmpty()) {
                responseText
            } else {
                Log.e(TAG, "Empty response from Gemini API: $response")
                "Error: Gemini returned an empty response. Let's keep working on your schedule!"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API: ${e.message}", e)
            "Error API Call: ${e.localizedMessage}. Running local simulation instead:\n\n" + simulateLocalCoachResponse(prompt)
        }
    }

    /**
     * Generates simulated smart responses based on keywords in prompt when API Key is missing or network fails.
     */
    private fun simulateLocalCoachResponse(prompt: String): String {
        return when {
            prompt.contains("OPTIMIZE", ignoreCase = true) || prompt.contains("schedule", ignoreCase = true) -> {
                """
                === 📅 AI SCHEDULE OPTIMIZATION ===
                I have reviewed your time-blocked items and goals. Here is the optimized schedule and spacing:
                
                1. 🌅 **Morning Focus Block (08:30 - 10:30)**: Dedicate this slot to your HIGH importance goals before daily noise accumulates.
                2. 🔋 **Rest & Transition (10:30 - 11:00)**: Soft break. Go for a brief walk or hydrate.
                3. 📂 **Administrative & Minor Tasks (11:00 - 12:30)**: Group MEDIUM and LOW importance meetings or email replies here.
                4. 🍗 **Lunch Break (12:30 - 13:30)**: Genuine disconnect.
                5. 🚀 **Afternoon Action Block (13:30 - 16:00)**: Dive into your weekly and monthly goal alignment.
                6. 🧘 **Reflection and Grading Buffer (16:00 - 16:30)**: Spend 10 minutes completing today's daily checklist and daily grade record.
                
                💡 **Optimization Insight**: We shifted low-importance tasks later in the day to protect your cognitive energy when alert levels are highest in the morning! This spacing reduces task-switching overhead by 15%.
                """.trimIndent()
            }
            prompt.contains("COURSE_CORRECT", ignoreCase = true) || prompt.contains("fail", ignoreCase = true) || prompt.contains("incorrect", ignoreCase = true) -> {
                """
                === 🎯 INTERACTIVE COURSE CORRECTION ===
                It looks like some goals weren't met today. Don't worry! Progress is iterative, not linear. Here is your adaptive strategy:
                
                🔄 **Macro adjustments**:
                - **Deconstruct goals**: Break the daily goal into 3 sub-tasks of 20 minutes each.
                - **Scope Reduction**: Reduce your monthly expectations by 20% to regain positive psychological traction. Focus purely on consistent tiny wins.
                - **Energy Audit**: Move demanding goals to an hour earlier.
                
                📝 **Dynamic Adaptation Suggestions**:
                - For **Daily goals**: Reschedule unfinished items to tomorrow's Morning Block.
                - For **Weekly goals**: Extend deadline by 2 days, but secure a 1-hour focus session tonight to establish momentum.
                - **Mindset Tip**: Failure is just raw data. You graded yourself based on barriers—let's automate some safeguards for tomorrow.
                """.trimIndent()
            }
            else -> {
                """
                === 🧠 GOAL COACH SYSTEM ===
                I'm ready to help you analyze, enhance, and structure your goals. 
                
                **Adaptive Tips**:
                - Check in daily to complete items.
                - Use the "Daily Grading" mechanism to record what blocked you.
                - Click "Optimize Schedule" to let me restructure your daily time blocks into an optimal pattern.
                """.trimIndent()
            }
        }
    }
}
