package com.example.standardbenutzer.integrate

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ScaleGestureDetector
/**
 * Created by Standardbenutzer on 22.12.2017.
 */
class DrawView : View {

    private val paint = Paint()
    private var prevX = 0
    private var lowerBound = 0.0
    private var upperBound = 0.0
    private var divFac = 1.0
    private var listener: UpdatedBoundsListener? = null
    private var yPoints : IntArray? = null
    private var scaleDetector : ScaleGestureDetector? = null
    private var gestureDetector : GestureDetectorCompat? = null
    private var leftXBorder = 0

    constructor(context:Context) : super(context) {
        paint.color = Color.BLACK
        paint.isAntiAlias = true
        paint.strokeWidth = 2f
        scaleDetector = ScaleGestureDetector(context, ScaleListener())
        gestureDetector = GestureDetectorCompat(this.context,GestureListener())
    }

    constructor(context:Context,attr: AttributeSet) : super(context,attr) {
        paint.color = Color.BLACK
        paint.isAntiAlias = true
        paint.strokeWidth = 2f
        scaleDetector = ScaleGestureDetector(context, ScaleListener())
        gestureDetector = GestureDetectorCompat(this.context,GestureListener())
    }

    override fun onDraw(canvas: Canvas?) {
        paint.color = Color.parseColor("#FAFAFA")
        canvas?.drawRect(0f,0f,canvas.width.toFloat(),canvas.height.toFloat(),paint)
        paint.color = Color.BLACK
        if(yPoints == null)
            return
        for(i in 0 until this.width-2){
            val startY = canvas?.height!!.div(2) - yPoints!![i]
            val endX = i+1
            val endY = canvas.height.div(2) - yPoints!![i+1]
            paint.color = Color.BLACK
            canvas.drawLine(i.toFloat(),startY.toFloat(),endX.toFloat(),endY.toFloat(), paint)
            if(i in upperBound..lowerBound && endX in upperBound..lowerBound){
                paint.color = Color.parseColor("#ff4949")
                canvas.drawRect(i.toFloat(),startY.toFloat(),endX.toFloat(),canvas.height.div(2).toFloat(),paint)
            }
        }
        drawBounds(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x = event?.x

        val dX = x?.minus(prevX)

        if(dX!! > 0){
            lowerBound = x.toDouble()
        } else{
            upperBound = x.toDouble()
        }

        prevX = x.toInt()
        scaleDetector?.onTouchEvent(event)
        gestureDetector?.onTouchEvent(event)
        listener?.onBoundsUpdated(mutableListOf((lowerBound - leftXBorder - (this.width / 2.0)) / divFac,(upperBound - leftXBorder - (this.width / 2)) / divFac))
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
                val diffY = e2.getY() - e1.getY()
                val diffX = e2.getX() - e1.getX()
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            Log.d("SWIPE", "Detected Swipe Right.")
                            leftXBorder += 25
                            //onSwipeRight()
                        } else {
                            Log.d("SWIPE", "Detected Swipe Left.")
                            leftXBorder -= 25
                            //onSwipeLeft()
                        }
                        result = true
                    }
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        //onSwipeBottom()
                    } else {
                        //onSwipeTop()
                    }
                    result = true
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            invalidate()
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
        canvas?.drawLine(lowerBound.toFloat(),canvas.height.toFloat(),lowerBound.toFloat(),0.toFloat(),paint)
        canvas?.drawText("%.2f".format((lowerBound - leftXBorder - (this.width / 2.0)) / divFac),lowerBound.toFloat(),canvas.height.div(2).toFloat(),paint)

        canvas?.drawLine(upperBound.toFloat(),canvas.height.toFloat(),upperBound.toFloat(),0.toFloat(),paint)
        canvas?.drawText("%.2f".format((upperBound - leftXBorder - (this.width / 2)) / divFac),upperBound.toFloat(),canvas.height.div(2).toFloat(),paint)
    }

    interface UpdatedBoundsListener {
        fun onBoundsUpdated(bounds : List<Double>)
    }

    fun setUpdatedBoundsListener(listener : UpdatedBoundsListener){
        this.listener = listener
    }
}