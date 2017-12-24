package com.example.standardbenutzer.integrate

import android.os.AsyncTask
import android.util.Log

/**
 * Created by Standardbenutzer on 24.12.2017.
 */
class AsyncFunctionValues : AsyncTask<Any,Int,Array<List<Int>>> {
    private var drawViewWidth = 0
    private var divFac = 0.0
    private var input : String? = null
    private var listener : OnFunctionCalculationCompleted? = null
    constructor() : super()

    override fun doInBackground(vararg p0: Any?): Array<List<Int>> {
        drawViewWidth = p0[0] as Int
        divFac = p0[1] as Double
        input = p0[2] as String
        listener = p0[3] as OnFunctionCalculationCompleted

        val xPoints : MutableList<Int> = mutableListOf()
        val yPoints : MutableList<Int> = mutableListOf()
        try {
            for (i in -drawViewWidth.div(2)..drawViewWidth.div(2)){
                if(!isCancelled) {
                    var math = MathEval()
                    math.setVariable("x", i.div(divFac))
                    xPoints.add(i + drawViewWidth.div(2))
                    yPoints.add(math.evaluate(input).toInt())
                } else
                    break
            }
        } catch(e : Exception){
            Log.d("ERROR",e.message)
        }
        return arrayOf(xPoints,yPoints)
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
    }

    override fun onPostExecute(result: Array<List<Int>>?) {
        super.onPostExecute(result)
        System.out.println("Did Function Value Calculation.")
        listener?.onFunctionCalcCompleted(result)
    }
}