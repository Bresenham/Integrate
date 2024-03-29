package com.example.standardbenutzer.integrate

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ScaleGestureDetector
import java.math.BigDecimal

/**
 * Created by Standardbenutzer on 22.12.2017.
 */
class DrawView : View {

    private val paint = Paint()
    private var prevX = 0
    private var lowerBound = 0.0
    private var upperBound = 0.0
    private var divFac = 1.0
    private var updatedBoundsListener: UpdatedBoundsListener? = null
    private var updatedScreenListener : UpdatedScreenListener? = null
    private var yPoints : IntArray? = null
    private var scaleDetector : ScaleGestureDetector? = null
    private var gestureDetector : GestureDetectorCompat? = null
    private var leftXBorder = 0
    private var upperYBorder = 0

    constructor(context:Context) : super(context) {
        init()
    }
    constructor(context:Context,attr: AttributeSet) : super(context,attr) {
        init()
    }

    override fun onDraw(canvas: Canvas?) {
        paint.color = Color.parseColor("#FAFAFA")
        canvas?.drawRect(0f,0f,canvas.width.toFloat(),canvas.height.toFloat(),paint)
        paint.color = Color.BLACK
        if(yPoints == null)
            return
        for(i in 0 until yPoints!!.size-1){
            val startY = canvas?.height!!.div(2) - yPoints!![i] - upperYBorder
            val endX = i+1
            val endY = canvas.height.div(2) - yPoints!![i+1] - upperYBorder
            paint.color = Color.BLACK
            canvas.drawLine(i.toFloat(),startY.toFloat(),endX.toFloat(),endY.toFloat(), paint)
            if(i in lowerBound..upperBound && endX in lowerBound..upperBound){
                paint.color = Color.parseColor("#FF4081")
                canvas.drawRect(i.toFloat(),startY.toFloat(),endX.toFloat(),canvas.height.div(2).minus(upperYBorder).toFloat(),paint)
            }
        }
        drawBounds(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x = event?.x!!

        val dXLower = x - lowerBound
        val dXUpper = x - upperBound

        if(Math.abs(dXLower) < Math.abs(dXUpper))
            lowerBound = x.toDouble()
        else
            upperBound = x.toDouble()

        scaleDetector?.onTouchEvent(event)
        gestureDetector?.onTouchEvent(event)

        prevX = x.toInt()

        updatedBoundsListener?.onBoundsUpdated(doubleArrayOf((lowerBound - leftXBorder - (this.width / 2.0)) / divFac, (upperBound - leftXBorder - (this.width / 2)) / divFac))
        this.invalidate()

        return true
    }

    internal inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onDown(event: MotionEvent): Boolean {
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            var result = false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        leftXBorder += (velocityX / 100).toInt()
                        updatedScreenListener?.onScreenUpdated()
                        result = true
                        invalidate()
                    }
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    upperYBorder += (velocityY / 100).toInt()
                    result = true
                    invalidate()
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return result
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var scale = detector.scaleFactor
            if(scale > 1) {
                divFac += scale * 5
            }
            else {
                divFac -= scale * 5
            }
            divFac = Math.max(1.0, Math.min(divFac, 1000.0))

            updatedScreenListener?.onScreenUpdated()
            invalidate()
            return true
        }
    }

    fun updateFunction(dataPoints : IntArray){
        yPoints = dataPoints
        this.invalidate()
    }

    fun getScaleFactor() : Double {
        return this.divFac
    }

    fun getXStart() : Int {
        return this.leftXBorder
    }

    private fun drawBounds(canvas : Canvas?){
        paint.color = Color.BLUE
        val lowerRounded = BigDecimal((lowerBound - leftXBorder - (this.width / 2.0)) / divFac).setScale(2, BigDecimal.ROUND_HALF_UP)
        var upperRounded = BigDecimal((upperBound - leftXBorder - (this.width / 2.0)) / divFac).setScale(2, BigDecimal.ROUND_HALF_UP)

        canvas?.drawLine(lowerBound.toFloat(),canvas.height.toFloat(),lowerBound.toFloat(),0.toFloat(),paint)
        canvas?.drawText("$lowerRounded",lowerBound.toFloat(),canvas.height.div(2).toFloat(),paint)

        canvas?.drawLine(upperBound.toFloat(),canvas.height.toFloat(),upperBound.toFloat(),0.toFloat(),paint)
        canvas?.drawText("$upperRounded",upperBound.toFloat(),canvas.height.div(2).toFloat(),paint)
    }

    private fun init(){
        paint.color = Color.BLACK
        paint.isAntiAlias = true
        paint.strokeWidth = 3f
        scaleDetector = ScaleGestureDetector(context, ScaleListener())
        gestureDetector = GestureDetectorCompat(this.context,GestureListener())
        lowerBound = width.toDouble().div(2.0) + 100
        upperBound = width.toDouble().div(2.0) + 200
    }

    interface UpdatedBoundsListener {
        fun onBoundsUpdated(bounds : DoubleArray)
    }

    fun setUpdatedBoundsListener(listener : UpdatedBoundsListener){
        this.updatedBoundsListener = listener
    }

    interface UpdatedScreenListener {
        fun onScreenUpdated()
    }

    fun setUpdatedScreenListener(listener : UpdatedScreenListener) {
        this.updatedScreenListener = listener
    }
}