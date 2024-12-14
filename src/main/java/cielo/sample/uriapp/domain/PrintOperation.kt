package cielo.sample.uriapp.domain

enum class PrintOperation(val operation: Int) {
    PRINT_TEXT(0),
    PRINT_MULTI_COLUMN_TEXT(1),
    PRINT_IMAGE(2)
}