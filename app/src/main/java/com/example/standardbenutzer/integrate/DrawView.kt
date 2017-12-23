package com.example.standardbenutzer.integrate

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Created by Standardbenutzer on 22.12.2017.
 */
class DrawView : View{

    private val paint = Paint()
    private var prevX = 0
    private var prevY = 0
    private var lowerBound = 0
    private var upperBound = 0
    private val divFac = 100;
    private var listener: UpdatedBoundsListener? = null
    constructor(context:Context) : super(context) {
        paint.color = Color.BLACK
        paint.isAntiAlias = true
        paint.strokeWidth = 2f
    }

    constructor(context:Context,attr: AttributeSet) : super(context,attr) {
        paint.color = Color.BLACK
        paint.isAntiAlias = true
        paint.strokeWidth = 2f
    }

    override fun onDraw(canvas: Canvas?) {
        paint.color = Color.parseColor("#FAFAFA")
        canvas?.drawRect(0f,0f,canvas?.width!!.toFloat(),canvas?.height!!.toFloat(),paint)
        val xPoints : MutableList<Int> = mutableListOf()
        val yPoints : MutableList<Int> = mutableListOf()
        for(i in 0..canvas?.width!!){
            xPoints.add(i)
            yPoints.add(Math.exp(i.toDouble().div(divFac)).toInt())
        }
        paint.color = Color.BLACK
        for(i in 0 until xPoints.size-2){
            val startX = xPoints[i].toFloat()
            val startY = canvas?.height - yPoints[i].toFloat()
            val endX = xPoints[i+1].toFloat()
            val endY = canvas?.height - yPoints[i+1].toFloat()
            paint.color = Color.BLACK
            canvas?.drawLine(startX,startY,endX,endY, paint)
            if(startX in upperBound..lowerBound && endX in upperBound..lowerBound){
                paint.color = Color.RED
                canvas?.drawRect(startX,startY,endX,canvas?.height.toFloat(),paint)
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
        listener?.onBoundsUpdated(mutableListOf(lowerBound.toDouble().div(divFac),upperBound.toDouble().div(divFac)))
        this.invalidate()
        return true
    }

    interface UpdatedBoundsListener {
        fun onBoundsUpdated(bounds : List<Double>)
    }

    fun setUpdatedBoundsListener(listener : UpdatedBoundsListener){
        this.listener = listener
    }

    private fun drawBounds(canvas : Canvas?){
        paint.color = Color.BLUE
        canvas?.drawLine(lowerBound.toFloat(),canvas?.height.toFloat(),lowerBound.toFloat(),0.toFloat(),paint)
        canvas?.drawText(lowerBound.toDouble().div(divFac).toString(),lowerBound.toFloat(),canvas?.height.div(2).toFloat(),paint)

        canvas?.drawLine(upperBound.toFloat(),canvas?.height.toFloat(),upperBound.toFloat(),0.toFloat(),paint)
        canvas?.drawText(upperBound.toDouble().div(divFac).toString(),upperBound.toFloat(),canvas?.height.div(2).toFloat(),paint)
    }

    fun getText() : String {
        return "LowerBound: $lowerBound | UpperBound: $upperBound"
    }
}