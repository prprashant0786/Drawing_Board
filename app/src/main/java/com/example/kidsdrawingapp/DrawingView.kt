package com.example.kidsdrawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Toast


class DrawingView(context : Context,attrs : AttributeSet) : View(context,attrs) {
    // we want to draw on this view so we add color,Bitmap , paint,thickness, path

    private var mDrawPath : CustomPath? = null
    private var mCanvasBitmap : Bitmap? = null
    private var mDrawpaint : Paint? = null
    private var mCanvasPaint : Paint? = null
    private var canvas : Canvas? = null

    private val mPaths = ArrayList<CustomPath>()

    private val mundoPaths = ArrayList<CustomPath>()
    private val mredoPaths = ArrayList<CustomPath>()

    private var mBrushSize : Float = 0f
    private var color : Int = Color.BLACK

    init { // Contructor
        setupdrawing()
    }

    fun onclickundo(){
        if(mPaths.size>0){
         mundoPaths.add(mPaths.removeAt(mPaths.size - 1));
         invalidate()
        }
    }
    fun oneraseeverything(){
        if (mPaths.size>0) {
            mPaths.clear()
        }
        else{
            Toast.makeText(context, "ALL READY CLEANED", Toast.LENGTH_SHORT).show()
        }
        invalidate()
    }

    private fun setupdrawing(){
        mDrawpaint = Paint()
        mDrawPath = CustomPath(color,mBrushSize)
        mDrawpaint!!.color = color
        mDrawpaint!!.style = Paint.Style.STROKE
        mDrawpaint!!.strokeJoin = Paint.Join.ROUND
        mDrawpaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
//        mBrushSize = 20f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(h,w, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!,0f,0f,mCanvasPaint)

        for(path in mPaths){
            mDrawpaint!!.strokeWidth = path.brushThickness
            mDrawpaint!!.color= path.color
            canvas.drawPath(path,mDrawpaint!!)

        }


        if(!mDrawPath!!.isEmpty){
            mDrawpaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawpaint!!.color= mDrawPath!!.color
            canvas.drawPath(mDrawPath!!,mDrawpaint!!)

        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        val touchX = event?.x
        val touchY = event?.y

        when (event?.action) {

            MotionEvent.ACTION_DOWN -> { //Press on screen
                mDrawPath?.color = color
                mDrawPath?.brushThickness = mBrushSize

                mDrawPath?.reset() // Clear any lines and curves from the path, making it empty.
                mDrawPath?.moveTo(
                   touchX!!,
                   touchY!!
                ) // Set the beginning of the next contour to the point (x,y).
            }

            MotionEvent.ACTION_MOVE -> {
                mDrawPath?.lineTo(
                    touchX!!,
                    touchY!!
                ) // Add a line from the last point to the specified point (x,y).
            }

            MotionEvent.ACTION_UP -> {
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(color, mBrushSize)
            }

            else -> return false
        }


        invalidate()
        return true
    }

     fun setsizeforbrush(newsize : Float){
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            newsize,resources.displayMetrics
        )

        mDrawpaint!!.strokeWidth = mBrushSize
    }
    fun setColor(newColor: String) {
        color = Color.parseColor(newColor)
        mDrawpaint?.color = color
    }
    internal inner class CustomPath(var color :Int,var brushThickness : Float) : Path() {

    }
}
