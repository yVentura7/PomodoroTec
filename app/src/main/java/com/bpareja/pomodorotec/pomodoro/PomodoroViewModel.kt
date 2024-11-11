package com.bpareja.pomodorotec.pomodoro

import android.Manifest
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.CountDownTimer
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bpareja.pomodorotec.MainActivity
import com.bpareja.pomodorotec.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


enum class Phase {

    FOCUS, BREAK

}

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext


    private val _timeLeft = MutableLiveData("25:00")
    val timeLeft: LiveData<String> = _timeLeft
    private val _isRunning = MutableLiveData(false)
    val isRunning: LiveData<Boolean> = _isRunning
    private val _currentPhase = MutableLiveData(Phase.FOCUS)
    val currentPhase: LiveData<Phase> = _currentPhase
    private var countDownTimer: CountDownTimer? = null
    fun startTimer() {
        startFocusSession()
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
                    }
                    Phase.BREAK -> {
                        _currentPhase.value = Phase.FOCUS
                        _timeLeft.value = "25:00"
                    }

                    else -> {}
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



    fun startFocusSession() {
        viewModelScope.launch {
            showNotification("Inicio de Concentración", "La sesión de 25 minutos ha comenzado.")
            delay(25 * 60 * 1000L) // 25 minutos
            showNotification("Fin de Concentración", "La sesión de concentración ha finalizado.")
            startBreakSession()
        }
    }

    private fun startBreakSession() {
        viewModelScope.launch {
            showNotification("Inicio de Descanso", "La sesión de 5 minutos ha comenzado.")
            delay(5 * 60 * 1000L) // 5 minutos
            showNotification("Fin de Descanso", "La sesión de descanso ha finalizado.")
        }
    }

    private fun showNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.pomodoro)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(MainActivity.NOTIFICATION_ID, builder.build())
        }
    }

}
