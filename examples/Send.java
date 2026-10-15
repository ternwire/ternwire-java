import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ternwire.TernwireClient;

public class Send {
    public static void main(String[] args) throws Exception {
        var client = new TernwireClient(System.getenv("TERNWIRE_API_KEY"), System.getenv("TERNWIRE_API_ORIGIN"));
        var input = new JsonObject();
        input.addProperty("from", System.getenv("TERNWIRE_FROM"));
        var recipients = new JsonArray();
        recipients.add(System.getenv("TERNWIRE_TO"));
        input.add("to", recipients);
        input.addProperty("subject", "Your receipt");
        input.addProperty("text", "Thanks for your order.");
        var accepted = client.submitEmail(input, "order-123-receipt");
        System.out.println(accepted);
        System.out.println(client.getMessage(accepted.get("id").getAsString()));
    }
}
