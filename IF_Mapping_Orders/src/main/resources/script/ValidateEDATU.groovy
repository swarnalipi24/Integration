import com.sap.it.api.mapping.*

def String validateEdatu(String edatu, MappingContext context) {
    if (edatu == null || edatu.trim().isEmpty() || edatu.trim() == "00000000") {
        throw new RuntimeException("Mapping Error: EDATU (Schedule Line Date) is initial or missing.")
    }
    
    String cleanDate = edatu.trim().replaceAll("-", "")
    if (cleanDate.length() != 8) {
        throw new RuntimeException("Mapping Error: Invalid EDATU date format '${edatu}'. Expected YYYYMMDD.")
    }
    
    return cleanDate
}
