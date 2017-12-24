package com.example.standardbenutzer.integrate

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ScaleGestureDetector



/**
 * Created by Standardbenutzer on 22.12.2017.
 */
class DrawView : View{

    private val paint = Paint()
    private var prevX = 0
    private var prevY = 0
    private var lowerBound = 0
    private var upperBound = 0
    private var divFac = 1f
    private var listener: UpdatedBoundsListener? = null
    private var xPoints = listOf<Int>()
    private var yPoints = listOf<Int>()
    private var scaleDetector : ScaleGestureDetector? = null

    constructor(context:Context) : super(context) {
        paint.color = Color.BLACK
        paint.isAntiAlias = true
        paint.strokeWidth = 2f
        scaleDetector = ScaleGestureDetector(context, ScaleListener())
    }

    constructor(context:Context,attr: AttributeSet) : super(context,attr) {
        paint.color = Color.BLACK
        paint.isAntiAlias = true
        paint.strokeWidth = 2f
        scaleDetector = ScaleGestureDetector(context, ScaleListener())
    }

    override fun onDraw(canvas: Canvas?) {
        paint.color = Color.parseColor("#FAFAFA")
        canvas?.drawRect(0f,0f,canvas?.width!!.toFloat(),canvas?.height!!.toFloat(),paint)
        paint.color = Color.BLACK
        for(i in 0 until xPoints.size-2){
            val startX = xPoints[i].toFloat()
            val startY = canvas?.height!!.div(2) - yPoints[i]
            val endX = xPoints[i+1].toFloat()
            val endY = canvas?.height!!.div(2) - yPoints[i+1]
            paint.color = Color.BLACK
            canvas?.drawLine(startX,startY.toFloat(),endX,endY.toFloat(), paint)
            if(startX in upperBound..lowerBound && endX in upperBound..lowerBound){
                paint.color = Color.parseColor("#ff4949")
                canvas?.drawRect(startX,startY.toFloat(),endX,canvas?.height!!.div(2).toFloat(),paint)
            }
        }
        drawBounds(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x = event?.x
        val y = event?.y

        val dX = x?.minus(prevX)
        val dY = y?.minus(prevY)
        if(dX!! > 0){
            lowerBound = x.toInt()
        } else{
            upperBound = x.toInt()
        }

        prevX = x!!.toInt()
        prevY = y!!.toInt()
        scaleDetector?.onTouchEvent(event)
        listener?.onBoundsUpdated(mutableListOf(lowerBound.toDouble().minus(this.width.div(2)).div(divFac),upperBound.toDouble().minus(this.width.div(2)).div(divFac)))
        this.invalidate()
        return true
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
            divFac = Math.max(1.0f, Math.min(divFac, 1000.0f))

            System.out.println("SCALE FACTOR: $divFac")
            invalidate()
            return true
        }
    }

    fun updateFunction(dataPoints : Array<List<Int>>){
        xPoints = dataPoints[0]
        yPoints = dataPoints[1]
        this.invalidate()
    }

    fun getScaleFactor() : Float {
        return this.divFac
    }

    private fun drawBounds(canvas : Canvas?){
        paint.color = Color.BLUE
        canvas?.drawLine(lowerBound.toFloat(),canvas?.height.toFloat(),lowerBound.toFloat(),0.toFloat(),paint)
        canvas?.drawText("%.2f".format(lowerBound.toDouble().minus(this.width.div(2)).div(divFac)),lowerBound.toFloat(),canvas?.height.div(2).toFloat(),paint)

        canvas?.drawLine(upperBound.toFloat(),canvas?.height.toFloat(),upperBound.toFloat(),0.toFloat(),paint)
        canvas?.drawText("%.2f".format(upperBound.toDouble().minus(this.width.div(2)).div(divFac)),upperBound.toFloat(),canvas?.height.div(2).toFloat(),paint)
    }

    interface UpdatedBoundsListener {
        fun onBoundsUpdated(bounds : List<Double>)
    }

    fun setUpdatedBoundsListener(listener : UpdatedBoundsListener){
        this.listener = listener
    }
}