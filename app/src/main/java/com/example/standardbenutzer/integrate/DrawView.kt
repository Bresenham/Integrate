package com.example.standardbenutzer.integrate

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Created by Standardbenutzer on 22.12.2017.
 */
class DrawView : View{
    private val paint = Paint()
    constructor(context:Context) : super(context) {
        paint.color = Color.BLACK
    }
    constructor(context:Context,attr: AttributeSet) : super(context,attr) {
        paint.color = Color.BLACK
    }
    override fun onDraw(canvas: Canvas?) {
        paint.color = Color.BLACK
        canvas?.drawRect(0f,0f,canvas?.width!!.toFloat(),canvas?.height!!.toFloat(),paint)
        val xPoints : MutableList<Int> = mutableListOf()
        val yPoints : MutableList<Int> = mutableListOf()
        for(i in 0..50){
            xPoints.add(i+canvas?.width!!.div(2))
            yPoints.add(Math.exp(i.toDouble()).toInt())
        }
        paint.color = Color.GREEN
        for(i in 0 until xPoints.size-2){
            canvas?.drawLine(xPoints[i].toFloat(),canvas?.height - yPoints[i].toFloat(),xPoints[i+1].toFloat(),canvas?.height - yPoints[i+1].toFloat(),paint)
        }
    }
}