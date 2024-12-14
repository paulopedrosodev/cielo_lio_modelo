package cielo.sample.uriapp.domain

class PrintRequest(val operation: PrintOperation, val value: Array<String>, val styles: List<Map<String, Int>>)