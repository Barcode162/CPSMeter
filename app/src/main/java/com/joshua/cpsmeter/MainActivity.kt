package com.joshua.cpsmeter

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.joshua.cpsmeter.databinding.ActivityMainBinding

/**
 * Measures real taps on the hardware: every finger-down is a touch event with a
 * monotonic timestamp. Live CPS = taps within the last 1000 ms; peak = highest
 * live value seen; average = total / elapsed.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val ui = Handler(Looper.getMainLooper())

    private var durationMs = 0L          // 0 = free run
    private var running = false
    private var done = false
    private var startTime = 0L
    private var totalTaps = 0
    private var peakCps = 0
    private val window = ArrayDeque<Long>()  // tap timestamps, last 1s

    private val ticker = object : Runnable {
        override fun run() {
            refresh()
            ui.postDelayed(this, TICK_MS)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tapZone.setOnTouchListener { _, ev ->
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> onTap()
            }
            true
        }

        binding.modeFree.setOnClickListener { selectMode(0L) }
        binding.mode5.setOnClickListener { selectMode(5000L) }
        binding.mode10.setOnClickListener { selectMode(10000L) }
        binding.btnReset.setOnClickListener { reset() }

        selectMode(0L)
    }

    override fun onResume() {
        super.onResume()
        ui.post(ticker)
    }

    override fun onPause() {
        super.onPause()
        ui.removeCallbacks(ticker)
    }

    private fun onTap() {
        if (done) return
        val now = SystemClock.elapsedRealtime()
        if (!running) {
            running = true
            startTime = now
        }
        totalTaps++
        window.addLast(now)
        binding.tapHint.text = ""
        refresh()
    }

    private fun selectMode(duration: Long) {
        durationMs = duration
        highlight(duration)
        reset()
    }

    private fun highlight(duration: Long) {
        fun set(tv: TextView, on: Boolean) =
            tv.setTextColor(getColor(if (on) R.color.accent else R.color.onSurfaceDim))
        set(binding.modeFree, duration == 0L)
        set(binding.mode5, duration == 5000L)
        set(binding.mode10, duration == 10000L)
    }

    private fun reset() {
        running = false
        done = false
        startTime = 0L
        totalTaps = 0
        peakCps = 0
        window.clear()
        binding.tapHint.text = getString(R.string.tap_here)
        refresh()
    }

    private fun refresh() {
        val now = SystemClock.elapsedRealtime()
        while (window.isNotEmpty() && now - window.first() > 1000L) window.removeFirst()
        val current = window.size
        if (running && current > peakCps) peakCps = current

        var elapsedMs = if (running || done) now - startTime else 0L
        if (durationMs > 0 && running && elapsedMs >= durationMs) {
            finish(durationMs)
            elapsedMs = durationMs
        }
        if (done && durationMs > 0) elapsedMs = durationMs

        val elapsedSec = elapsedMs / 1000.0
        val avg = if (elapsedSec >= 0.5) totalTaps / elapsedSec else 0.0

        binding.currentValue.text = (if (done) 0 else current).toString()
        binding.peakValue.text = peakCps.toString()
        binding.tapsValue.text = totalTaps.toString()
        binding.timeValue.text = String.format("%.1f", elapsedSec)
        binding.avgValue.text = String.format("%.1f", avg)
    }

    private fun finish(duration: Long) {
        running = false
        done = true
        val avg = totalTaps / (duration / 1000.0)
        binding.tapHint.text = "Done!  ${"%.1f".format(avg)} CPS avg\nTap Reset to retry"
    }

    private companion object {
        const val TICK_MS = 50L
    }
}
