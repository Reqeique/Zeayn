
# Zean - The unique Vision

Zean is a Jetpack Compose project that provides both Textual and OCR-based Question Answering capabilities. It leverages a custom implemented Language Model (LLM) to provide accurate and efficient answers to your queries.

![alt text](/assets/img_1.png "1") ![alt text](/assets/img.png "2")

## Features

- **Textual Question Answering**: Pose your questions in text format and get accurate answers instantly. This feature is powered by a custom implemented Language Model (LLM).
- **OCR-based Question Answering**: Have a question in an image or a document? No problem! Zean can extract the text from your images or documents and provide answers. This feature uses the CameraX library for capturing images and performing OCR (Optical Character Recognition).
- **Offline History Caching**: Zean also offers offline history caching, allowing you to access your past queries and answers without an internet connection. This feature uses the Room database for efficient data caching.
## Setup

1. Clone the repository to your local machine(`git clone https:\\github.com\Reqeique\Zeayn`).
2. Open the project in your preferred IDE (preferably Android Studio BumbleBee Canary or later) implement `LLMApi`.
3. `gradle build` and run the app.
## Usage

1. Run the app on your emulator or physical device.
2. Use the Textual Question Answering feature by typing your question into the provided text field.
3. Use the OCR-based Question Answering feature by uploading an image or document with your question.

## Custom LLM API

To enable the question answering capabilities, you must set up the custom LLM API. Please follow these steps:

1. Navigate to the `LLMApi` class.
2. Add Custom LLM Api as per below.
```kotlin
@Serializable
data class GenerateRequest(
    val prompt: String,
    val max_tokens: Int
)

class LLMApi(private val client: HttpClient) {

    @OptIn(InternalAPI::class)
    suspend fun textCompletion(prompt: String): Result {

        val apiKey = "YOUR_API_KEY"
        val url = "YOUR_ENDPOINT"
        val maxTokens = 50

        return try {
            val response = client.post<HttpResponse>(url) {
                headers {
                    append(HttpHeaders.Authorization, "BEARER $apiKey")
                    append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    append("API-Version", "2021-11-08")
                }

                body = GenerateRequest(prompt, maxTokens)
            }

            if (response.status == HttpStatusCode.OK){ Success(
                response.content.readUTF8Line()?.let { extractGenerations(it)?.get(0)?.text } ?: "") }
            else { Error(Throwable(response.status.description))}
        } catch (cause: Throwable) {
            Error(Throwable(cause.rootCause?.localizedMessage))
        }
    }
}

@Serializable
data class Generation(val id: String, val text: String)

@Serializable
data class GenerationResponse(val id: String, val generations: List<Generation>, val prompt: String, @Contextual val meta: JsonElement)

@OptIn(ExperimentalSerializationApi::class)
fun extractGenerations(jsonString: String): List<Generation>? {
    return try {
        val json = Json {
            ignoreUnknownKeys = true
        }

        val response = json.decodeFromString<GenerationResponse>(jsonString)
        response.generations
    } catch (e: Exception) {
        null
    }
}
```



## License

This project is licensed under  [MIT License](LICENSE). Please see the `LICENSE` file for more information.


