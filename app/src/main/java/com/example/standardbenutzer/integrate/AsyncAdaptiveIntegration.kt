package com.example.standardbenutzer.integrate

import android.os.AsyncTask

/**
 * Created by Standardbenutzer on 24.12.2017.
 */
class AsyncAdaptiveIntegration : AsyncTask<Any,Int,Double> {
    constructor() : super()

    private var exp : String? = null
    private var a = 0.0
    private var b = 0.0
    private var err = 0.0
    private var listener : OnAdaptiveIntegrationCompleted? = null
    private var sumList : MutableList<Double>? = null
    override fun doInBackground(vararg p0: Any?): Double {
        exp = p0[0] as String
        a = p0[1] as Double
        b = p0[2] as Double
        err = p0[3] as Double
        listener = p0[4] as OnAdaptiveIntegrationCompleted
        sumList = p0[5] as MutableList<Double>


        return adativeIntegration(a,b,err)
    }

    override fun onPostExecute(result: Double?) {
        super.onPostExecute(result)
        System.out.println("Did Adaptive Integration from [%.2f..%.2f]= %.2f".format(a,b,result))
        sumList!!.add(result!!)
        listener?.onAdaptiveIntegrationCompleted(result,sumList!!)
    }

    private fun f(x : Double) : Double{
        var math = MathEval()
        math.setVariable("x",x)
        try {
            return math.evaluate(exp)
        } catch (e : Exception){
            return 0.0
        }
    }

    private fun adativeIntegration(a : Double, b : Double, err : Double) : Double{
        if(!isCancelled) {
            var h = b.minus(a)
            val s0 = h * ((1.0 / 6.0) * f(a) + (4.0 / 6.0) * f((a + b) / 2) + (1.0 / 6.0) * f(b))
            h = (b - a).div(2)
            var s1 = 0.0
            for (i in 0..1) {
                if(!isCancelled) {
                    var a_1 = a + i.times(h)
                    var b_1 = b + (i + 1).times(h)
                    s1 += h * ((1.0 / 6.0) * f(a_1) + (4.0 / 6.0) * f((a_1 + b_1) / 2) + (1.0 / 6.0) * f(b_1))
                } else
                    break
            }
            var e = Math.abs(s1 - s0)

            if (e <= err)
                return s1
            else {
                return adativeIntegration(a, (a + b).div(2), err) + adativeIntegration((a + b).div(2), b, err)
            }
        } else
            return -1.0
    }
}