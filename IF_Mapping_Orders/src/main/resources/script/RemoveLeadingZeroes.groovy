import com.sap.it.api.mapping.*

def String removeLeadingZeros(String input, MappingContext context) {
    if (input == null || input.trim().isEmpty()) {
        return ""
    }
    return input.trim().replaceFirst("^0+(?!\$)", "")
}