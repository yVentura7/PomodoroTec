package com.bpareja.pomodorotec.pomodoro

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.CountDownTimer
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bpareja.pomodorotec.MainActivity
import com.bpareja.pomodorotec.R

enum class Phase {
    FOCUS, BREAK
}

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {

    private val _timeLeft = MutableLiveData("25:00")

    val timeLeft: LiveData<String> = _timeLeft

    private val _isRunning = MutableLiveData(false)

    val isRunning: LiveData<Boolean> = _isRunning

    private val _currentPhase = MutableLiveData(Phase.FOCUS)

    val currentPhase: LiveData<Phase> = _currentPhase

    private var countDownTimer: CountDownTimer? = null

    fun startTimer() {

        if (_isRunning.value == true) return

        val duration = when (_currentPhase.value) {

            Phase.FOCUS -> 25 * 60 * 1000L

                    Phase.BREAK -> 5 * 60 * 1000L

            else -> 25 * 60 * 1000L

        }

        _isRunning.value = true

        countDownTimer = object : CountDownTimer(duration, 1000) {

            override fun onTick(millisUntilFinished: Long) {

                val minutes = (millisUntilFinished / 1000) / 60

                val seconds = (millisUntilFinished / 1000) % 60

                _timeLeft.value = String.format("%02d:%02d", minutes, seconds)

            }

            override fun onFinish() {

                _isRunning.value = false

                when (_currentPhase.value) {

                    Phase.FOCUS -> {

                        _currentPhase.value = Phase.BREAK

                        _timeLeft.value = "05:00"

                        sendNotification("Tiempo de descanso", "Es hora de un descanso de 5 minutos.")

                        startTimer()

                    }

                    Phase.BREAK -> {

                        _currentPhase.value = Phase.FOCUS

                        _timeLeft.value = "25:00"

                        sendNotification("Tiempo de concentración", "Es hora de concentrarse por 25 minutos.")

                    }

                }

            }

        }.start()

    }

    fun pauseTimer() {

        countDownTimer?.cancel()

        _isRunning.value = false

    }

    fun resetTimer() {

        countDownTimer?.cancel()

        _isRunning.value = false

        _currentPhase.value = Phase.FOCUS

        _timeLeft.value = "25:00"

    }

    private fun sendNotification(title: String, message: String) {

        val context = getApplication<Application>().applicationContext

        val intent = Intent(context, MainActivity::class.java).apply {

            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)

            .setSmallIcon(R.drawable.ic_launcher_background)

            .setContentTitle(title)

            .setContentText(message)

            .setPriority(NotificationCompat.PRIORITY_HIGH)

            .setContentIntent(pendingIntent)

            .setAutoCancel(true)

            .build()

        with(NotificationManagerCompat.from(context)) {

            notify(NOTIFICATION_ID, notification)

        }

    }

    companion object {

        private const val CHANNEL_ID = "pomodoro_channel"

        private const val NOTIFICATION_ID = 1

    }

}