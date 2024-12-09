package com.disterde.candlesticks.service

import com.disterde.candlesticks.exception.HandlerExistsException
import com.disterde.candlesticks.exception.HandlerNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import kotlin.test.BeforeTest
import kotlin.test.Test

class HandlerManagerImplTest {

    private lateinit var manager: HandlerManagerImpl

    @BeforeTest
    fun setUp() {
        manager = HandlerManagerImpl()
    }

    @Test
    fun `should create a new handler successfully`() {
        manager.createHandler(ISIN)

        val handler = manager.getHandler(ISIN)
        assertThat(handler).isNotNull
        assertThat(handler.getCandlesticks()).isEmpty()
    }

    @Test
    fun `should throw exception when creating duplicate handler`() {
        manager.createHandler(ISIN)

        assertThatThrownBy { manager.createHandler(ISIN) }
            .isInstanceOf(HandlerExistsException::class.java)
            .hasMessage("Handler already exists for the specified ISIN: $ISIN")
    }

    @Test
    fun `should retrieve an existing handler`() {
        manager.createHandler(ISIN)

        val handler = manager.getHandler(ISIN)

        assertThat(handler).isNotNull
    }

    @Test
    fun `should throw exception when retrieving non-existent handler`() {
        assertThatThrownBy { manager.getHandler(ISIN) }
            .isInstanceOf(HandlerNotFoundException::class.java)
            .hasMessage("Handler not found for the specified ISIN: $ISIN")
    }

    @Test
    fun `should delete an existing handler`() {
        manager.createHandler(ISIN)

        manager.deleteHandler(ISIN)

        assertThatThrownBy { manager.getHandler(ISIN) }
            .isInstanceOf(HandlerNotFoundException::class.java)
            .hasMessage("Handler not found for the specified ISIN: $ISIN")
    }

    @Test
    fun `should throw exception when deleting non-existent handler`() {
        assertThatThrownBy { manager.deleteHandler(ISIN) }
            .isInstanceOf(HandlerNotFoundException::class.java)
            .hasMessage("Handler not found for the specified ISIN: $ISIN")
    }

    @Test
    fun `should handle multiple handlers`() {
        val isins = listOf("US1234567890", "US0987654321", "US1111111111")

        isins.forEach { manager.createHandler(it) }

        isins.forEach { isin ->
            val handler = manager.getHandler(isin)
            assertThat(handler).isNotNull
        }

        isins.forEach { manager.deleteHandler(it) }

        isins.forEach { isin ->
            assertThatThrownBy { manager.getHandler(isin) }
                .isInstanceOf(HandlerNotFoundException::class.java)
                .hasMessage("Handler not found for the specified ISIN: $isin")
        }
    }

    @Test
    fun `should allow concurrent creation and deletion`() {
        val isins = (1..100).map { "ISIN-$it" }

        val creationThreads = isins.map { isin ->
            Thread {
                try {
                    manager.createHandler(isin)
                } catch (e: HandlerExistsException) {
                    // Ignore duplicates
                }
            }
        }

        val deletionThreads = isins.map { isin ->
            Thread {
                try {
                    manager.deleteHandler(isin)
                } catch (e: HandlerNotFoundException) {
                    // Ignore not found
                }
            }
        }

        (creationThreads + deletionThreads).forEach { it.start() }
        (creationThreads + deletionThreads).forEach { it.join() }

        assertThat(manager).isNotNull
    }

    companion object {
        private const val ISIN = "TEST_ISIN"
    }
}