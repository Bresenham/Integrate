package com.example.standardbenutzer.integrate

import android.os.AsyncTask
import android.util.Log

/**
 * Created by Standardbenutzer on 24.12.2017.
 */
class AsyncFunctionValues : AsyncTask<Any,Int,IntArray> {
    private var drawViewWidth = 0
    private var divFac = 0.0
    private var input : String? = null
    private var funcStart = 0
    private var funcEnd = 0
    private var listener : OnFunctionCalculationCompleted? = null
    private var calc : IntArray? = null
    constructor() : super()

    override fun doInBackground(vararg p0: Any?): IntArray {
        drawViewWidth = p0[0] as Int
        divFac = p0[1] as Double
        input = p0[2] as String
        funcStart = p0[3] as Int
        funcEnd = p0[4] as Int
        listener = p0[5] as OnFunctionCalculationCompleted

        calc = IntArray(funcEnd-funcStart,{_->0})
        for (i in 0 until calc!!.size){//-drawViewWidth.div(2)-leftXBorder..drawViewWidth.div(2)-leftXBorder)
            if(!isCancelled) {
                var math = MathEval()
                math.setVariable("x", (i + funcStart).div(divFac))
                try {
                    calc!![i] = math.evaluate(input).toInt()
                } catch (e : Exception){
                    calc!![i] = -1000
                    Log.d("ERROR", e.message)
                }
            } else
                break
        }

        return calc!!
    }

    override fun onPostExecute(result: IntArray) {
        super.onPostExecute(result)
        listener?.onFunctionCalcCompleted(result, funcStart)
    }
}