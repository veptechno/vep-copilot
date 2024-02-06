import com.mlp.sdk.utils.JSON
import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatCompletionResult
import com.theokanning.openai.completion.chat.ChatMessage
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpStatement
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking

object OpenAiClient {

    private val client = HttpClient(CIO) {
        engine {
            requestTimeout = Long.MAX_VALUE
        }
    }

    fun chatCompletionResult(system: String, user: String): String = runBlocking {
        val request = ChatCompletionRequest()
        request.model = "gpt-4-1106-preview"
        request.messages = listOf(
            ChatMessage("system", system),
            ChatMessage("user", user)
        )
        request.maxTokens = 4096

        val rawResponse = client.post<HttpStatement>("${PROXY_URL}v1/chat/completions") {
            contentType(ContentType.Application.Json)
            body = JSON.stringify(request)
            header("Authorization", "Bearer $AUTH_TOKEN")
        }

        val result = JSON.parse(rawResponse.receive<String>(), ChatCompletionResult::class.java)

        println("PROMPT: ${result.usage.promptTokens} TOKENS")
        println("COMPLETION: ${result.usage.completionTokens} TOKENS")
        result.choices.first().message.content
    }

    const val PROXY_URL = "<url>"
    const val AUTH_TOKEN = "<token>"
}
