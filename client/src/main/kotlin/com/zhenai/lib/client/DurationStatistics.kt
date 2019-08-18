package com.zhenai.lib.client


import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import java.util.function.BiConsumer
import java.util.function.Supplier
import org.sonar.api.config.Configuration
import org.sonar.api.utils.log.Loggers

class DurationStatistics(config: Configuration) {

    private val stats = ConcurrentHashMap<String, AtomicLong>()

    private val recordStat: Boolean

    init {
        recordStat = config.getBoolean(PROPERTY_KEY).orElse(false)
    }

    fun <C, T> time(id: String, consumer: BiConsumer<C, T>): BiConsumer<C, T> {
        if (recordStat) {
            return BiConsumer{ t, u -> time(id, Supplier{ consumer.accept(t, u) }) }
        } else {
            return consumer
        }
    }

    fun time(id: String, runnable: Runnable) {
        if (recordStat) {
            time(id, Supplier{
                runnable.run()
                null
            })
        } else {
            runnable.run()
        }
    }

    fun <T> time(id: String, supplier: Supplier<T>): T {
        if (recordStat) {
            val startTime = System.nanoTime()
            val result = supplier.get()
            record(id, System.nanoTime() - startTime)
            return result
        } else {
            return supplier.get()
        }
    }

    fun record(id: String, elapsedTime: Long) {
        (stats as java.util.Map<String, AtomicLong>).computeIfAbsent(id) { key -> AtomicLong(0) }.addAndGet(elapsedTime)
    }

    fun log() {
        if (recordStat) {
            val out = StringBuilder()
            val symbols = DecimalFormatSymbols(Locale.ROOT)
            symbols.groupingSeparator = '\''
            val format = DecimalFormat("#,###", symbols)
            out.append("Duration Statistics")
            stats.entries.stream()
                .sorted { a, b -> java.lang.Long.compare(b.value.get(), a.value.get()) }
                .forEach { e ->
                    out.append(", ")
                        .append(e.key)
                        .append(" ")
                        .append(format.format(e.value.get() / 1_000_000L))
                        .append(" ms")
                }
            LOG.info(out.toString())
        }
    }

    companion object {

        private val LOG = Loggers.get(DurationStatistics::class.java)

        private val PROPERTY_KEY = "sonar.slang.duration.statistics"
    }

}