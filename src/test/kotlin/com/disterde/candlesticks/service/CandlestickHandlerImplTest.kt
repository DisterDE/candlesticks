import com.disterde.candlesticks.model.TimedPrice
import com.disterde.candlesticks.service.CandlestickHandlerImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.time.temporal.ChronoUnit.MINUTES
import java.time.temporal.ChronoUnit.SECONDS
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class CandlestickHandlerImplTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var handler: CandlestickHandlerImpl

    @BeforeTest
    fun setUp() {
        handler = CandlestickHandlerImpl(ISIN, MAX_CANDLES, dispatcher)
    }

    @Test
    fun `should create a new candle for each minute`() = runTest(dispatcher) {
        val startTime = LocalDateTime.now().truncatedTo(MINUTES).toInstant(UTC)

        handler.addPrice(TimedPrice(100.0, startTime))
        advanceTimeBy(60.seconds)
        handler.addPrice(TimedPrice(110.0, startTime.plus(1, MINUTES)))
        advanceTimeBy(60.seconds)
        handler.addPrice(TimedPrice(120.0, startTime.plus(2, MINUTES)))
        advanceTimeBy(100)
        handler.stop()

        val candles = handler.getCandlesticks()
        assertThat(candles).hasSize(3)
        assertThat(candles[0].closingPrice).isEqualTo(100.0)
        assertThat(candles[1].closingPrice).isEqualTo(110.0)
        assertThat(candles[2].closingPrice).isEqualTo(120.0)
    }

    @Test
    fun `should reuse last candle values if no new data arrives`() = runTest(dispatcher) {
        val startTime = LocalDateTime.now().truncatedTo(MINUTES).toInstant(UTC)

        handler.addPrice(TimedPrice(100.0, startTime))
        advanceTimeBy(180.seconds)
        handler.stop()

        val candles = handler.getCandlesticks()
        assertThat(candles).hasSize(3)
        candles.forEach {
            assertThat(it.openPrice).isEqualTo(100.0)
            assertThat(it.closingPrice).isEqualTo(100.0)
        }
    }

    @Test
    fun `should update the current candle with new prices`() = runTest(dispatcher) {
        val startTime = LocalDateTime.now().truncatedTo(MINUTES).toInstant(UTC)

        handler.addPrice(TimedPrice(100.0, startTime))
        handler.addPrice(TimedPrice(105.0, startTime.plus(15, SECONDS)))
        handler.addPrice(TimedPrice(95.0, startTime.plus(30, SECONDS)))
        advanceTimeBy(100)
        handler.stop()

        val candles = handler.getCandlesticks()
        assertThat(candles).hasSize(1)
        val candle = candles.first()
        assertThat(candle.openPrice).isEqualTo(100.0)
        assertThat(candle.highPrice).isEqualTo(105.0)
        assertThat(candle.lowPrice).isEqualTo(95.0)
        assertThat(candle.closingPrice).isEqualTo(95.0)
    }

    @Test
    fun `should create a new candle when a price from a new interval arrives`() = runTest(dispatcher) {
        val startTime = LocalDateTime.now().truncatedTo(MINUTES).toInstant(UTC)

        handler.addPrice(TimedPrice(100.0, startTime))
        advanceTimeBy(60.seconds)
        handler.addPrice(TimedPrice(110.0, startTime.plus(1, MINUTES)))
        advanceTimeBy(1.seconds)
        handler.stop()

        val candles = handler.getCandlesticks()
        assertThat(candles).hasSize(2)
        assertThat(candles.first().closingPrice).isEqualTo(100.0)
        assertThat(candles.last().closingPrice).isEqualTo(110.0)
    }

    @Test
    fun `should limit the number of stored candlesticks to maxCandles`() = runTest(dispatcher) {
        val startTime = LocalDateTime.now().truncatedTo(MINUTES).toInstant(UTC)

        repeat(10) { i ->
            launch { handler.addPrice(TimedPrice(100.0 + i, startTime.plus(i.toLong(), MINUTES))) }
        }
        advanceTimeBy(10.minutes)
        handler.stop()

        val candles = handler.getCandlesticks()
        assertThat(candles).hasSize(5) // maxCandles is set to 5
        assertThat(candles.first().closingPrice).isEqualTo(105.0)
        assertThat(candles.last().closingPrice).isEqualTo(109.0)
    }

    @Test
    fun `should modify closed candlesticks when a late price arrives`() = runTest(dispatcher) {
        val startTime = LocalDateTime.now().truncatedTo(MINUTES).toInstant(UTC)

        handler.addPrice(TimedPrice(100.0, startTime))
        handler.addPrice(TimedPrice(110.0, startTime.plus(1, MINUTES)))
        handler.addPrice(TimedPrice(90.0, startTime))
        advanceTimeBy(60.seconds)
        handler.stop()

        val candles = handler.getCandlesticks()
        assertThat(candles).hasSize(2)
        assertThat(candles[0].closingPrice).isEqualTo(90.0)
        assertThat(candles[1].closingPrice).isEqualTo(110.0)
    }

    @Test
    fun `should handle concurrent price updates correctly`() = runTest(dispatcher) {
        val startTime = LocalDateTime.now().truncatedTo(MINUTES).toInstant(UTC)

        repeat(100) { i ->
            launch {
                handler.addPrice(TimedPrice(100.0 + i, startTime.plusSeconds(i.toLong())))
            }
        }

        advanceTimeBy(60.seconds)
        handler.stop()

        val candles = handler.getCandlesticks()
        assertThat(candles).hasSize(2)
        val firstCandle = candles.first()
        assertThat(firstCandle.lowPrice).isEqualTo(100.0)
        assertThat(firstCandle.highPrice).isEqualTo(159.0)
        assertThat(firstCandle.closingPrice).isEqualTo(159.0)
        val lastCandle = candles.last()
        assertThat(lastCandle.openPrice).isEqualTo(160.0)
        assertThat(lastCandle.lowPrice).isEqualTo(160.0)
        assertThat(lastCandle.highPrice).isEqualTo(199.0)
        assertThat(lastCandle.closingPrice).isEqualTo(199.0)
    }

    @Test
    fun `should handle price exactly on the boundary of intervals`() = runTest(dispatcher) {
        val startTime = LocalDateTime.now().truncatedTo(MINUTES).toInstant(UTC)

        handler.addPrice(TimedPrice(100.0, startTime))
        handler.addPrice(TimedPrice(110.0, startTime.plus(59, SECONDS)))
        handler.addPrice(TimedPrice(111.0, startTime.plus(1, MINUTES)))
        advanceTimeBy(60.seconds)
        handler.stop()

        val candles = handler.getCandlesticks()
        assertThat(candles).hasSize(2)
        val candle = candles.first()
        assertThat(candle.openPrice).isEqualTo(100.0)
        assertThat(candle.closingPrice).isEqualTo(110.0)
    }

    @Test
    fun `should handle zero prices`() = runTest(dispatcher) {
        val startTime = LocalDateTime.now().truncatedTo(MINUTES).toInstant(UTC)

        handler.addPrice(TimedPrice(0.0, startTime))
        advanceTimeBy(1.seconds)
        handler.stop()

        val candles = handler.getCandlesticks()
        assertThat(candles).hasSize(1)
        val candle = candles.first()
        assertThat(candle.openPrice).isEqualTo(0.0)
        assertThat(candle.highPrice).isEqualTo(0.0)
        assertThat(candle.lowPrice).isEqualTo(0.0)
        assertThat(candle.closingPrice).isEqualTo(0.0)
    }

    @Test
    fun `should ignore negative prices`() = runTest(dispatcher) {
        val startTime = LocalDateTime.now().truncatedTo(MINUTES).toInstant(UTC)

        handler.addPrice(TimedPrice(0.0, startTime))
        handler.addPrice(TimedPrice(-10.0, startTime.plus(30, SECONDS)))
        advanceTimeBy(1.seconds)
        handler.stop()

        val candles = handler.getCandlesticks()
        assertThat(candles).hasSize(1)
        val candle = candles.first()
        assertThat(candle.openPrice).isEqualTo(0.0)
        assertThat(candle.highPrice).isEqualTo(0.0)
        assertThat(candle.lowPrice).isEqualTo(0.0)
        assertThat(candle.closingPrice).isEqualTo(0.0)
    }

    @Test
    fun `should handle high-frequency updates with expected accuracy`() = runTest(dispatcher) {
        val startTime = LocalDateTime.now().truncatedTo(MINUTES).toInstant(UTC)

        repeat(200) { i ->
            launch {
                handler.addPrice(TimedPrice(100.0 + i, startTime.plusSeconds(i.toLong())))
            }
        }

        advanceTimeBy(200.seconds)
        handler.stop()

        val candles = handler.getCandlesticks()
        assertThat(candles).hasSize(4) // Assumes periods overlap into third minute
        val lastCandle = candles.last()
        assertThat(lastCandle.openPrice).isEqualTo(280.0)
        assertThat(lastCandle.lowPrice).isEqualTo(280.0)
        assertThat(lastCandle.highPrice).isEqualTo(299.0)
        assertThat(lastCandle.closingPrice).isEqualTo(299.0)
    }

    companion object {
        private const val ISIN = "TEST_ISIN"
        private const val MAX_CANDLES = 5
    }
}