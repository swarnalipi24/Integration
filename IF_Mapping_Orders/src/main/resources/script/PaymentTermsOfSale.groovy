import com.sap.it.api.mapping.*

def void getPaymentTerms(String[] qualfs, String[] ztermTxts, String[] tages, String[] ztermCodes, Output result, MappingContext context) {
    // Lookup table for E1EDK01/ZTERM fallback (add all your SAP payment term codes here)
    def ztermLookup = [
        "N73": "73 days net after date of invoice",
        "N45": "45 days net after date of invoice"
    ]

    def isValid = { val -> val != null && !val.equals(Output.SUPPRESS) && val.trim().length() > 0 }

    // 1. Check all E1EDK18 segments for QUALF == '001'
    if (qualfs != null) {
        for (int i = 0; i < qualfs.length; i++) {
            if (isValid(qualfs[i]) && qualfs[i].trim() == "001") {
                String txt = (ztermTxts != null && i < ztermTxts.length) ? ztermTxts[i] : ""
                String days = (tages != null && i < tages.length) ? tages[i] : ""

                // Priority 1: Use ZTERM_TXT if present and not blank
                if (isValid(txt)) {
                    result.addValue(txt.trim())
                    return
                }
                // Priority 2: If blank, use '<TAGE> days net after date of invoice'
                if (isValid(days)) {
                    result.addValue("${days.trim()} days net after date of invoice")
                    return
                }
            }
        }
    }

    // Priority 3: If E1EDK18 (QUALF=001) is absent, send E1EDK01/ZTERM text via lookup
    if (ztermCodes != null && ztermCodes.length > 0 && isValid(ztermCodes[0])) {
        String code = ztermCodes[0].trim().toUpperCase()
        result.addValue(ztermLookup.get(code, code))
        return
    }

    result.addSuppress()
}
