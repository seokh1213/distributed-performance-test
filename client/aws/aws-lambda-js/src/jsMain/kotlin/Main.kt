import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.LoadContext
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import kotlin.js.Promise

external fun fetch(input: dynamic, init: RequestInit = definedExternally): Promise<Response>


// lambda entry point
@JsExport
@OptIn(ExperimentalJsExport::class, DelicateCoroutinesApi::class)
fun handler(event: dynamic): Promise<dynamic> = GlobalScope.promise {
    try {
        val loadContext = Json.decodeFromString<LoadContext>(JSON.stringify(event))
        console.log("LoadContext.httpRequest.url: ${loadContext.httpRequest.url}")
    } catch (e: Exception) {
        console.log(e)
    }

    JSON.stringify(event)
}

// node entry point
suspend fun main() {
    console.log("Hello, World!")

    val response = fetch("https://naver.com").await()
    val html = response.text().await()

    console.log("HTML: ${html.length}")
}
