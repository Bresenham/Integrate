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
    constructor(context:Context) : super(context)
    constructor(context:Context,attr: AttributeSet) : super(context,attr)
    override fun onDraw(canvas: Canvas?) {
        canvas?.drawRect(0f,0f,canvas?.width!!.toFloat(),canvas?.height!!.toFloat(),Paint(Color.BLACK))
        for(i in -50..50){
            canvas?.drawPoint((i + 50).toFloat(),(canvas?.height - Math.pow(i.toDouble(),2.toDouble())).toFloat(),Paint(Color.GREEN))
        }
    }
}