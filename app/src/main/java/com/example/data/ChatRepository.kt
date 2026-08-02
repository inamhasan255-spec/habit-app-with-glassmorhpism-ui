package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini REST API Data Classes with Moshi ---

interface CommandExecutor {
    fun execute(commandName: String, args: Map<String, Any>): String
}

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null,
    @Json(name = "tools") val tools: List<Tool>? = null
)

@JsonClass(generateAdapter = true)
data class Tool(
    @Json(name = "functionDeclarations") val functionDeclarations: List<FunctionDeclaration>? = null,
    @Json(name = "googleSearch") val googleSearch: Map<String, Any>? = null
)

@JsonClass(generateAdapter = true)
data class FunctionDeclaration(
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String,
    @Json(name = "parameters") val parameters: Parameters
)

@JsonClass(generateAdapter = true)
data class Parameters(
    @Json(name = "type") val type: String = "OBJECT",
    @Json(name = "properties") val properties: Map<String, Property>,
    @Json(name = "required") val required: List<String>
)

@JsonClass(generateAdapter = true)
data class Property(
    @Json(name = "type") val type: String,
    @Json(name = "description") val description: String,
    @Json(name = "enum") val enumValues: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>,
    @Json(name = "role") val role: String? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: InlineData? = null,
    @Json(name = "functionCall") val functionCall: FunctionCall? = null
)

@JsonClass(generateAdapter = true)
data class FunctionCall(
    @Json(name = "name") val name: String,
    @Json(name = "args") val args: Map<String, Any>? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "topP") val topP: Float? = null,
    @Json(name = "topK") val topK: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content? = null,
    @Json(name = "groundingMetadata") val groundingMetadata: GroundingMetadata? = null
)

@JsonClass(generateAdapter = true)
data class GroundingMetadata(
    @Json(name = "webSearchQueries") val webSearchQueries: List<String>? = null,
    @Json(name = "groundingChunks") val groundingChunks: List<GroundingChunk>? = null
)

@JsonClass(generateAdapter = true)
data class GroundingChunk(
    @Json(name = "web") val web: WebSource? = null
)

@JsonClass(generateAdapter = true)
data class WebSource(
    @Json(name = "uri") val uri: String,
    @Json(name = "title") val title: String
)

// --- Retrofit Setup ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse

    @POST("v1beta/models/gemini-3.1-pro-preview:generateContent")
    suspend fun generateContentPro(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse

    @POST("v1beta/models/gemini-3.1-flash-lite:generateContent")
    suspend fun generateContentLite(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContentForModel(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

class ChatRepository {

    private fun isNetworkAvailable(): Boolean {
        return try {
            val address = java.net.InetAddress.getByName("generativelanguage.googleapis.com")
            !address.hostAddress.isNullOrEmpty()
        } catch (e: Exception) {
            false
        }
    }

    private fun buildToolsList(): List<Tool> {
        val standardTools = Tool(
            functionDeclarations = listOf(
                FunctionDeclaration(
                    name = "startFocusSession",
                    description = "Start a new focus session with a label, duration in minutes, and mode",
                    parameters = Parameters(
                        properties = mapOf(
                            "label" to Property(type = "STRING", description = "The focus session label, e.g., Calculus Homework"),
                            "durationMinutes" to Property(type = "INTEGER", description = "The duration of focus session in minutes"),
                            "mode" to Property(type = "STRING", description = "The mode for the focus block: Study, Work, Code, Read, General")
                        ),
                        required = listOf("label", "durationMinutes")
                    )
                ),
                FunctionDeclaration(
                    name = "addTask",
                    description = "Add a new academic task to your planner dashboard",
                    parameters = Parameters(
                        properties = mapOf(
                            "title" to Property(type = "STRING", description = "Title of the task"),
                            "subject" to Property(type = "STRING", description = "Academic subject name"),
                            "dueInDays" to Property(type = "INTEGER", description = "Number of days from now until it is due"),
                            "priority" to Property(type = "STRING", description = "Priority of the task: High, Medium, Low")
                        ),
                        required = listOf("title", "subject")
                    )
                ),
                FunctionDeclaration(
                    name = "addHabit",
                    description = "Create a new positive habit to track on your dashboard",
                    parameters = Parameters(
                        properties = mapOf(
                            "name" to Property(type = "STRING", description = "The name of the habit"),
                            "category" to Property(type = "STRING", description = "The category: Health, Work, Study, Mindfulness, Routine"),
                            "colorHex" to Property(type = "STRING", description = "An optional hex color code for the habit card, e.g., #FF5722"),
                            "targetValue" to Property(type = "INTEGER", description = "Optional target daily count"),
                            "unit" to Property(type = "STRING", description = "Optional target count units, e.g., times, cups")
                        ),
                        required = listOf("name", "category")
                    )
                ),
                FunctionDeclaration(
                    name = "switchTab",
                    description = "Switch the active navigation tab or screen in the dashboard",
                    parameters = Parameters(
                        properties = mapOf(
                            "tabName" to Property(type = "STRING", description = "The tab/screen name to switch to: HOME, HABITS, SHIELD, STATS, ACADEMIC, SETTINGS")
                        ),
                        required = listOf("tabName")
                    )
                ),
                FunctionDeclaration(
                    name = "toggleDarkMode",
                    description = "Toggle the application's visual interface mode between dark and light themes",
                    parameters = Parameters(
                        properties = emptyMap(),
                        required = emptyList()
                    )
                ),
                FunctionDeclaration(
                    name = "addTimetableEntry",
                    description = "Add an academic class/lecture/tutorial to your timetable schedule",
                    parameters = Parameters(
                        properties = mapOf(
                            "subjectName" to Property(type = "STRING", description = "The name of the class subject"),
                            "dayOfWeek" to Property(type = "INTEGER", description = "Day of the week (1=Monday, 2=Tuesday, 3=Wednesday, 4=Thursday, 5=Friday, 6=Saturday, 7=Sunday)"),
                            "weekRotation" to Property(type = "STRING", description = "Week frequency: All Weeks, Even Weeks, Odd Weeks"),
                            "startTime" to Property(type = "STRING", description = "Start time in HH:mm format, e.g., 09:30"),
                            "endTime" to Property(type = "STRING", description = "End time in HH:mm format, e.g., 11:00"),
                            "instructor" to Property(type = "STRING", description = "Optional instructor name"),
                            "room" to Property(type = "STRING", description = "Optional room code or lecture hall name"),
                            "colorHex" to Property(type = "STRING", description = "Optional hex color code for the timetable block")
                        ),
                        required = listOf("subjectName", "dayOfWeek", "startTime", "endTime")
                    )
                )
            )
        )

        val list = mutableListOf<Tool>()
        list.add(standardTools)
        if (isNetworkAvailable()) {
            list.add(Tool(googleSearch = emptyMap()))
        }
        return list
    }

    suspend fun generateCoachResponse(
        userMessage: String,
        chatHistory: List<ChatMessage>,
        streak: Int,
        lastSessionMinutes: Int,
        blockedAppsCount: Int,
        commandExecutor: CommandExecutor? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty()) {
            return@withContext generateOfflineCoachResponse(userMessage, streak, lastSessionMinutes, blockedAppsCount)
        }

        // Construct conversation contents
        val contentsList = mutableListOf<Content>()
        
        // Add chat history
        chatHistory.forEach { msg ->
            contentsList.add(
                Content(
                    parts = listOf(Part(text = msg.message)),
                    role = if (msg.role == "user") "user" else "model"
                )
            )
        }

        // Add the current user prompt
        contentsList.add(
            Content(
                parts = listOf(Part(text = userMessage)),
                role = "user"
            )
        )

        val systemPrompt = """
            You are Cube Coach, a concise productivity expert. 
            You have access to the user's focus stats [streak: $streak days, last session: $lastSessionMinutes minutes, blocked apps: $blockedAppsCount]. 
            Give actionable advice, not fluff. Max 2 sentences per response. Keep tone encouraging but professional.
            You can call tools to start focus sessions or add tasks if the user requests it.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = contentsList,
            generationConfig = GenerationConfig(
                temperature = 0.7f,
                topP = 0.95f,
                topK = 40
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = systemPrompt))
            ),
            tools = buildToolsList()
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val part = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()
            
            if (part?.functionCall != null && commandExecutor != null) {
                val call = part.functionCall
                val argsMap = call.args ?: emptyMap()
                commandExecutor.execute(call.name, argsMap)
            } else {
                var textResult = part?.text ?: ""
                val chunks = response.candidates?.firstOrNull()?.groundingMetadata?.groundingChunks
                if (!chunks.isNullOrEmpty()) {
                    val citationBuilder = java.lang.StringBuilder()
                    chunks.forEach { chunk ->
                        chunk.web?.let { web ->
                            citationBuilder.append("\n[CITATION: title=\"${web.title}\", url=\"${web.uri}\"]")
                        }
                    }
                    textResult += citationBuilder.toString()
                }
                textResult.ifEmpty { generateOfflineCoachResponse(userMessage, streak, lastSessionMinutes, blockedAppsCount) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            generateOfflineCoachResponse(userMessage, streak, lastSessionMinutes, blockedAppsCount)
        }
    }

    suspend fun generateAcademicResponse(
        userMessage: String,
        chatHistory: List<AcademicChatMessage>,
        systemPrompt: String,
        imageBytesBase64: String? = null,
        commandExecutor: CommandExecutor? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty()) {
            return@withContext generateOfflineAcademicResponse(userMessage, systemPrompt)
        }

        val contentsList = mutableListOf<Content>()

        // Add history
        chatHistory.forEach { msg ->
            contentsList.add(
                Content(
                    parts = listOf(Part(text = msg.message)),
                    role = if (msg.role == "user") "user" else "model"
                )
            )
        }

        // Add the current user message (multimodal if image is supplied)
        val userParts = mutableListOf<Part>()
        userParts.add(Part(text = userMessage))
        if (!imageBytesBase64.isNullOrEmpty()) {
            userParts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = imageBytesBase64)))
        }
        contentsList.add(
            Content(
                parts = userParts,
                role = "user"
            )
        )

        val request = GenerateContentRequest(
            contents = contentsList,
            generationConfig = GenerationConfig(
                temperature = 0.5f,
                topP = 0.9f,
                topK = 40
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = systemPrompt))
            ),
            tools = buildToolsList()
        )

        try {
            val response = if (!imageBytesBase64.isNullOrEmpty()) {
                RetrofitClient.service.generateContentPro(apiKey, request)
            } else {
                RetrofitClient.service.generateContent(apiKey, request)
            }
            val part = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()

            if (part?.functionCall != null && commandExecutor != null) {
                val call = part.functionCall
                val argsMap = call.args ?: emptyMap()
                commandExecutor.execute(call.name, argsMap)
            } else {
                var textResult = part?.text ?: ""
                val chunks = response.candidates?.firstOrNull()?.groundingMetadata?.groundingChunks
                if (!chunks.isNullOrEmpty()) {
                    val citationBuilder = java.lang.StringBuilder()
                    chunks.forEach { chunk ->
                        chunk.web?.let { web ->
                            citationBuilder.append("\n[CITATION: title=\"${web.title}\", url=\"${web.uri}\"]")
                        }
                    }
                    textResult += citationBuilder.toString()
                }
                textResult.ifEmpty { generateOfflineAcademicResponse(userMessage, systemPrompt) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            generateOfflineAcademicResponse(userMessage, systemPrompt)
        }
    }

    suspend fun analyzeImageWithGemini(
        prompt: String,
        imageBytesBase64: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty()) {
            return@withContext "ERROR: Gemini API Key is missing. Configure GEMINI_API_KEY in secrets."
        }

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt.ifBlank { "Analyze this image in detail and describe what you see, including key insights, diagram analysis, text extraction, and actionable steps." }),
                        Part(inlineData = InlineData(mimeType = "image/jpeg", data = imageBytesBase64))
                    )
                )
            ),
            generationConfig = GenerationConfig(temperature = 0.4f),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are an advanced AI vision and image understanding system powered by gemini-3.1-pro-preview. Provide comprehensive image analysis with deep reasoning."))
            )
        )

        try {
            val response = RetrofitClient.service.generateContentPro(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No analysis generated."
        } catch (e: Exception) {
            e.printStackTrace()
            "Error analyzing image: ${e.localizedMessage}"
        }
    }

    suspend fun summarizeTextFast(text: String): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isEmpty()) return@withContext "API Key missing."

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = "Please provide a highly concise summary of the following text:\n\n$text")))),
            generationConfig = GenerationConfig(temperature = 0.2f),
            systemInstruction = Content(parts = listOf(Part(text = "You are a fast, efficient AI summarizer powered by gemini-3.1-flash-lite.")))
        )
        try {
            val response = RetrofitClient.service.generateContentLite(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No summary generated."
        } catch (e: Exception) {
            "Error summarizing: ${e.localizedMessage}"
        }
    }

    suspend fun analyzeContentGeneral(content: String, prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isEmpty()) return@withContext "API Key missing."

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = "$prompt\n\nContent:\n$content")))),
            generationConfig = GenerationConfig(temperature = 0.4f),
            systemInstruction = Content(parts = listOf(Part(text = "You are a helpful AI assistant powered by gemini-3.5-flash, specializing in general text tasks, editing, and content analysis.")))
        )
        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response generated."
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }

    suspend fun parseScheduleImageWithGemini(
        imageBytesBase64: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty()) {
            return@withContext "ERROR: Gemini API Key is not configured yet. Set GEMINI_API_KEY in your env secrets."
        }

        val systemPrompt = """
            You are a highly accurate academic timetable and schedule digitizer powered by gemini-3.1-pro-preview. 
            Analyze the provided image of a physical class schedule, syllabus, spreadsheet, or whiteboard drawing.
            Extract all class sessions and map them to structured JSON.
            
            You MUST return a JSON array containing objects with the following EXACT fields:
            - subjectName (string, e.g. "Calculus III", "General Physics")
            - dayOfWeek (integer, 1 = Monday, 2 = Tuesday, 3 = Wednesday, 4 = Thursday, 5 = Friday, 6 = Saturday, 7 = Sunday)
            - weekRotation (string, default "All", or "A"/"B" if specified)
            - startTime (string, 24-hour HH:mm, e.g. "09:00", "13:30", "08:15")
            - endTime (string, 24-hour HH:mm, e.g. "10:30", "15:00", "09:45")
            - instructor (string, optional, name of professor/teacher)
            - room (string, optional, classroom or lab name, e.g. "Room 302", "Lab B")
            - colorHex (string, a beautiful Material-3-style theme color hex code, e.g. "#EF4444", "#3B82F6", "#10B981", "#F59E0B", "#8B5CF6")
            
            Strict requirements:
            1. Ensure start time and end time are in HH:mm 24-hour format.
            2. Match days of the week carefully to dayOfWeek (1 for Monday through 7 for Sunday).
            3. Do NOT include markdown code blocks like ```json or any other text wrappers around the JSON.
            4. If no schedule is visible or parseable, return an empty JSON array: []
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = "Please parse this physical schedule image into a JSON array."),
                        Part(inlineData = InlineData(mimeType = "image/jpeg", data = imageBytesBase64))
                    )
                )
            ),
            generationConfig = GenerationConfig(
                temperature = 0.2f
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = systemPrompt))
            )
        )

        try {
            val response = RetrofitClient.service.generateContentPro(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: "[]"
        } catch (e: Exception) {
            e.printStackTrace()
            "ERROR: ${e.localizedMessage}"
        }
    }

    private fun generateOfflineCoachResponse(
        userMessage: String,
        streak: Int,
        lastSessionMinutes: Int,
        blockedAppsCount: Int
    ): String {
        val msg = userMessage.lowercase()
        return when {
            msg.contains("distract") || msg.contains("focus") || msg.contains("why am i") || msg.contains("stuck") -> {
                "Distractions occur when cognitive load is high. Try using the Pomodoro technique: focus intensely for 25 minutes, then take a 5-minute break. You have blocked $blockedAppsCount apps to guard your focus!"
            }
            msg.contains("streak") || msg.contains("boost") || msg.contains("keep up") -> {
                "Your streak is at $streak days! Maintain this momentum by scheduling a short 15-minute session today. Consistency beats intensity every single time."
            }
            msg.contains("tomorrow") || msg.contains("plan") || msg.contains("schedule") -> {
                "Planning tomorrow tonight reduces decision fatigue. Set exactly three 'Most Important Tasks' for tomorrow and block dedicated hours on your calendar."
            }
            msg.contains("technique") || msg.contains("method") || msg.contains("study") || msg.contains("pomodoro") -> {
                "Try the Feynman Technique: explain a concept to yourself in simple terms as if teaching a child. This quickly exposes gaps in your comprehension."
            }
            else -> {
                "Keep up the great work! Your focus streak is at $streak days, and your last session was $lastSessionMinutes minutes. Let's block distractions and stay productive!"
            }
        }
    }

    private fun generateOfflineAcademicResponse(
        userMessage: String,
        systemPrompt: String
    ): String {
        val lines = systemPrompt.lines()
        val classes = lines.filter { it.trim().startsWith("- ") && it.contains(" at ") && it.contains("Instructor:") }
        val tasks = lines.filter { it.trim().startsWith("- Task:") }

        val msg = userMessage.lowercase()
        
        // 1. Handle requests for homework or tasks
        if (msg.contains("homework") || msg.contains("task") || msg.contains("due") || msg.contains("todo")) {
            if (tasks.isEmpty()) {
                return "You have no outstanding homework or tasks in your list right now. Great job keeping it clear! Would you like to create one?"
            }
            val taskSummary = tasks.joinToString("\n") { line ->
                val cleanLine = line.trim().substringAfter("- Task:").trim()
                "- $cleanLine"
            }
            return "Here are your current academic tasks (Offline Mode):\n$taskSummary\n\nFocus on completing high-priority items first."
        }

        // 2. Handle requests for classes or timetable
        if (msg.contains("class") || msg.contains("schedule") || msg.contains("timetable") || msg.contains("today") || msg.contains("calendar")) {
            if (classes.isEmpty()) {
                return "Your class schedule is currently empty. You can upload a photo of your schedule or add classes manually."
            }
            val classSummary = classes.joinToString("\n") { line ->
                val cleanLine = line.trim().substringAfter("- ").trim()
                "- $cleanLine"
            }
            return "Here is your current class timetable (Offline Mode):\n$classSummary\n\nBe sure to check your room locations before heading out."
        }

        // 3. Handle proposals to add task
        if (msg.contains("add task") || msg.contains("create task") || msg.contains("add homework") || msg.contains("suggest task")) {
            // Extract subject if possible
            var subject = "General"
            if (msg.contains("chemistry")) subject = "Chemistry"
            if (msg.contains("physics")) subject = "Physics"
            if (msg.contains("calculus") || msg.contains("math")) subject = "Math"
            if (msg.contains("computer") || msg.contains("coding")) subject = "Computer Science"
            
            return "I can help you add that task offline. Click 'Approve' on the proposal below to add it directly to your dashboard!\n\n[PROPOSAL_ADD_TASK: title=\"Review chapter notes\", subject=\"$subject\", dueInDays=2, priority=\"High\", notes=\"Added offline via Academic Assistant\"]"
        }

        // 4. Handle proposals to add class
        if (msg.contains("add class") || msg.contains("create class") || msg.contains("add schedule")) {
            var subject = "New Class"
            if (msg.contains("chemistry")) subject = "Organic Chemistry"
            if (msg.contains("physics")) subject = "Advanced Physics"
            if (msg.contains("math") || msg.contains("calculus")) subject = "Calculus III"
            
            return "I can assist in adding this class schedule offline. Click 'Approve' below to update your timetable!\n\n[PROPOSAL_ADD_CLASS: subject=\"$subject\", day=1, start=\"10:00\", end=\"11:30\", instructor=\"Dr. Smith\", room=\"Room 301\"]"
        }

        // 5. Default study advice
        return "Offline Assistant Mode: I am currently operating offline to save battery and work without connection.\n\n" +
               "• Current schedule includes ${classes.size} classes.\n" +
               "• You have ${tasks.size} active homework tasks.\n\n" +
               "Tip: Try practicing space-repetition or pomodoro techniques. Reconnect to the internet for advanced, open-ended conceptual explanations!"
    }
}
