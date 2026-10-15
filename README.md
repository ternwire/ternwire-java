# Ternwire Java SDK

Java client for the Ternwire transactional email API. Requires Java 17+ and Gson.
It can also be called from server-side Kotlin.

## Install

With Maven:

```xml
<dependency>
  <groupId>com.ternwire</groupId>
  <artifactId>ternwire</artifactId>
  <version>0.1.0</version>
</dependency>
```

With Gradle, add `implementation("com.ternwire:ternwire:0.1.0")`.

## Usage

```java
import com.google.gson.JsonParser;
import com.ternwire.TernwireClient;

var client = new TernwireClient(
    System.getenv("TERNWIRE_API_KEY"), System.getenv("TERNWIRE_API_ORIGIN"));
var input = JsonParser.parseString("""
    {"from":"notifications@your-verified-domain.com","to":["recipient@example.com"],
     "subject":"Your receipt","text":"Thanks for your order."}
    """).getAsJsonObject();
var message = client.submitEmail(input, "order-123-receipt");
System.out.println(client.getMessage(message.get("id").getAsString()));
```

All 17 operations use the API's camelCase operation names. Input and output are
Gson `JsonObject` values. Template deletion returns `null`. Calls are synchronous
and declare `IOException` and `InterruptedException`. Let cancellation interrupts
propagate, or restore the thread's interrupted flag when handling them.
`listEmailTemplates(String cursor, Integer limit)` returns one page of summaries,
and either argument may be `null`; `listEmailTemplates()` fetches the first page.
Pass the previous `page.nextCursor` as `cursor` to fetch the next page.
The optional third constructor argument is a `Duration`, default 30 seconds.

`TernwireApiException` exposes `status()`, `body()`, `rawBody()`, and `headers()`.
The response headers include any `Retry-After` value.

Verify webhooks with
`Webhooks.verify(signatureHeader, rawBodyBytes, signingSecret)` from
`com.ternwire.Webhooks`. The optional fourth argument is tolerance seconds,
default 300. The body must be the exact received bytes.

`examples/Send.java` is a complete main class; run it with this package and Gson
on the classpath after setting `TERNWIRE_API_KEY`, `TERNWIRE_API_ORIGIN`,
`TERNWIRE_FROM`, and `TERNWIRE_TO`. It submits an email.

Use `https://ternwire.com` as the API origin. Read the full documentation at
[ternwire.com/docs](https://ternwire.com/docs), or contact support@ternwire.com.
Released under the MIT License.
