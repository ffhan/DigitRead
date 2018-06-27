package com.prosocial.digitread


import android.content.Context
import android.graphics.*
import android.graphics.Paint.Style
import android.util.AttributeSet
import android.view.*
import android.R.attr.keySet
import android.R.attr.path
import android.app.Activity
import android.util.Log
import android.widget.TextView
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import java.util.*
import android.graphics.Bitmap
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Math.abs


class MyThing @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val radius: Float = 80f
    private var path: Path = Path()
    private var currentActivity : Activity = context as MainActivity
    private var img : Bitmap? = null
    private var tensorflow = TensorFlowInferenceInterface(currentActivity.assets, "file:///android_asset/new_model.pb")
    private var privateCanvas : Canvas = Canvas()

    init {
        mPaint.style = Style.STROKE
        mPaint.color = Color.WHITE
        mPaint.strokeWidth = radius
        mPaint.isAntiAlias = true
        mPaint.isFilterBitmap = true
        mPaint.isDither = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //Redraw old lines with stored stroke width
        canvas.drawPath(path, mPaint)
        privateCanvas.drawPath(path, mPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if(img == null){
            img = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }
        privateCanvas.setBitmap(img)
        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(event.x, event.y)
            }
            MotionEvent.ACTION_UP -> {
                predict()
            }
            else -> {
                if(path.isEmpty) path.moveTo(event.x, event.y)
                else path.lineTo(event.x, event.y)
                invalidate()
            }
        }
        val new_img = Bitmap.createScaledBitmap(img, 28, 28, true)
        currentActivity.myImage.setImageBitmap(new_img)
        return true
    }

    fun reset(){
        path = Path()
        img = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        privateCanvas.setBitmap(img)
        invalidate()
    }

    fun predict(){
        val new_img = Bitmap.createScaledBitmap(img, 28, 28, true)

        var intInput : IntArray = IntArray(new_img.width * new_img.height)
//        var input2d : Array<FloatArray> = Array(new_img.width, { FloatArray(new_img.height) })
        new_img.getPixels(intInput, 0, new_img.width, 0, 0, new_img.width, new_img.height)
        var input : FloatArray = intInput.map { t -> abs((t and 8).toFloat() / 8) }.toFloatArray()
        Log.d("WOOOAH", Arrays.toString(input))
//        for (row in 0 until new_img.height){
//            for (column in 0 until new_img.width){
//                input2d[row][column] = input[row * new_img.width + column].toFloat() / 255
//            }
//        }
        tensorflow.feed("input", input, 1, 28, 28, 1)
        // INPUT_SHAPE is an long[] of expected shape, input is a float[] with the input data
        Log.d("PASSED", "Fantastic!")
        val output = FloatArray(10)

// running inference for given input and reading output
        val outputNode = "output_node0"
        val outputNodes = arrayOf(outputNode)
        val enableStats = false
        tensorflow.run(outputNodes, enableStats)
        Log.d("PASSED", "PASSED RUN")
        tensorflow.fetch(outputNode, output) // output is a preallocated float[] in the size of the expected output vector

        var maksPerc : Float = Float.MIN_VALUE
        var maks : Int = -1
        for(index in 0 until output.size){
            if(output[index] > maksPerc){
                maksPerc = output[index]
                maks = index
            }
        }
        Log.d("RESULTS", Arrays.toString(output))
        val label : TextView = currentActivity.predictionText
        if(maksPerc > 0.5) label.text = "${maks} with certainty ${maksPerc * 100}%"
        else label.text = "No prediction"
    }
}