import com.sap.gateway.ip.core.customdev.util.Message
import groovy.xml.MarkupBuilder

def Message processData(Message message) {
    def messageBody = message.getBody(String)
    def root = new XmlParser().parseText(messageBody)

    // Fields used for grouping rows into records
    def groupFields = [
        'Vendor.Supplying_Plant',
        'Company_Code',
        'Purch_Group',
        'Purch_Org',
        'Dest.Shp_Method',
        'Your_Ref.Vendor',
        'Our_Ref.Prod_Cat',
        'Frequency',
        'Plant'
    ]

    // Header fields taken from the first row of each record/chunk
    def headerFields = [
        'Document_Category',
        'Document_Type',
        'Vendor.Supplying_Plant',
        'Company_Code',
        'Purch_Group',
        'Purch_Org',
        'Dest.Shp_Method',
        'Your_Ref.Vendor',
        'Our_Ref.Prod_Cat',
        'Validity_Start',
        'Frequency',
        'Header_Text',
        'Transmission_Control'
    ]

    // Line item fields output inside each LineItem
    def lineItemFields = [
        'Account_Assignment_Category',
        'Item_Category',
        'Material',
        'PO_Qty',
        'Deliv.Date',
        'Plant',
        'GL_Account',
        'Cost_Centre',
        'Customer'
    ]

    // Helper method to get text safely from a child node, return empty string if missing
    def getNodeText = { node, field ->
        def child = node."${field}"
        return (child && child.text()) ? child.text().trim() : ''
    }

    // Group rows by composite key of groupFields
    def grouped = root.row.groupBy { row ->
        groupFields.collect { getNodeText(row, it) }.join('|')
    }

    def writer = new StringWriter()
    def xml = new MarkupBuilder(writer)

    // Normalize element names (replace '.', '/', spaces with '_')
    def normalizeName = { name ->
        name.replaceAll(/[ .\/]/, '_')
    }

    xml.root {
        grouped.each { key, rows ->
            // Sort rows by Material ascending
            def sortedRows = rows.sort { a, b ->
                getNodeText(a, 'Material') <=> getNodeText(b, 'Material')
            }
            
            // Split rows into chunks of max 99 line items
            def chunkSize = 2
            def chunks = sortedRows.collate(chunkSize)

            chunks.each { chunkRows ->
                def firstRow = chunkRows[0]
                record {
                    // Write header fields from the first row of this chunk
                    headerFields.each { field ->
                        def normField = normalizeName(field)
                        "${normField}"(getNodeText(firstRow, field))
                    }
                    // Write line items of this chunk
                    LineItems {
                        chunkRows.each { row ->
                            LineItem {
                                lineItemFields.each { field ->
                                    def normField = normalizeName(field)
                                    "${normField}"(getNodeText(row, field))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Set the new XML as message body
    message.setBody(writer.toString())
    return message
}
