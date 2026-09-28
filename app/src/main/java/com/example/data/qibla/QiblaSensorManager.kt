package com.example.data.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

data class QiblaSensorState(
    val azimuth: Float = 0f, // 0..360 degrees True/Magnetic North heading
    val pitch: Float = 0f, // Device tilt front-to-back in degrees
    val roll: Float = 0f, // Device tilt side-to-side in degrees
    val accuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
    val isSupported: Boolean = true,
    val isFlatEnough: Boolean = true, // Whether device is held relatively flat (< 30° tilt)
    val hasMagneticSensor: Boolean = true
)

class QiblaSensorManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val rotationVectorSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometerSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magneticFieldSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _sensorState = MutableStateFlow(
        QiblaSensorState(
            isSupported = (rotationVectorSensor != null) || (accelerometerSensor != null && magneticFieldSensor != null),
            hasMagneticSensor = (rotationVectorSensor != null) || (magneticFieldSensor != null)
        )
    )
    val sensorState: StateFlow<QiblaSensorState> = _sensorState.asStateFlow()

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var smoothedAzimuth = 0f
    private var isListening = false

    fun startListening() {
        if (isListening || sensorManager == null) return

        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            accelerometerSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
            magneticFieldSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        }
        isListening = true
    }

    fun stopListening() {
        if (!isListening || sensorManager == null) return
        sensorManager.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        var rawAzimuth = 0f
        var rawPitch = 0f
        var rawRoll = 0f

        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)

                // Azimuth in radians -> convert to degrees 0..360
                val azimuthRad = orientationAngles[0]
                rawAzimuth = ((Math.toDegrees(azimuthRad.toDouble()) + 360.0) % 360.0).toFloat()
                rawPitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
                rawRoll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()
            }

            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, gravity, 0, event.values.size)
                computeOrientationFromGravityAndGeomagnetic()?.let { (az, p, r) ->
                    rawAzimuth = az
                    rawPitch = p
                    rawRoll = r
                } ?: return
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
                computeOrientationFromGravityAndGeomagnetic()?.let { (az, p, r) ->
                    rawAzimuth = az
                    rawPitch = p
                    rawRoll = r
                } ?: return
            }
        }

        // Smooth azimuth transitions across the 0/360 boundary
        smoothedAzimuth = smoothAngle(smoothedAzimuth, rawAzimuth, alpha = 0.25f)
        val isFlat = abs(rawPitch) < 35f && abs(rawRoll) < 35f

        _sensorState.value = _sensorState.value.copy(
            azimuth = smoothedAzimuth,
            pitch = rawPitch,
            roll = rawRoll,
            accuracy = event.accuracy,
            isFlatEnough = isFlat
        )
    }

    private fun computeOrientationFromGravityAndGeomagnetic(): Triple<Float, Float, Float>? {
        val success = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)
        if (success) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val azimuthRad = orientationAngles[0]
            val az = ((Math.toDegrees(azimuthRad.toDouble()) + 360.0) % 360.0).toFloat()
            val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
            val roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()
            return Triple(az, pitch, roll)
        }
        return null
    }

    /**
     * Circular angle smoothing avoiding erratic 359° <-> 0° jumping
     */
    private fun smoothAngle(current: Float, target: Float, alpha: Float): Float {
        var diff = target - current
        while (diff < -180f) diff += 360f
        while (diff > 180f) diff -= 360f
        val result = current + alpha * diff
        return (result + 360f) % 360f
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _sensorState.value = _sensorState.value.copy(accuracy = accuracy)
    }
}
