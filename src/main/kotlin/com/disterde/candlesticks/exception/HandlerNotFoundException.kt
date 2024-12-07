package com.disterde.candlesticks.exception

class HandlerNotFoundException(isin: String) : ApiException("Handler not found by ISIN: $isin")
