import com.sap.it.api.mapping.*

def String padLeftZeros(String input, MappingContext context) {
    if (input == null || input.trim().isEmpty()) {
        return ""
    }
    return input.trim().padLeft(6, '0')
}