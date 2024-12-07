package com.disterde.candlesticks.exception

class HandlerExistsException(isin: String) : ApiException("Handler already exists. ISIN: $isin")
