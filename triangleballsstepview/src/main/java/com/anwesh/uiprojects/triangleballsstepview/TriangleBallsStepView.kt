package com.anwesh.uiprojects.triangleballsstepview

/**
 * Created by anweshmishra on 28/12/18.
 */

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.app.Activity

val nodes : Int = 5
val rows : Int = 4
val movements : Int = 2
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val sizeFactor : Float = 2.6f
val color : Int = Color.parseColor("#1abc9c")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n)
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.mirrorValue(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.inverse() + scaleFactor() * b.inverse()
fun Float.updateScale(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap

fun Canvas.drawTBSNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val size : Float = gap / sizeFactor
    val ballGap : Float = (2 * size) / (rows + 1)
    val r = ballGap/4
    paint.color = color
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    save()
    translate(gap * (i + 1), h/2)
    rotate(90f * sc2)
    translate(-size, -size)
    var currY : Float = 0f
    for (j in 0..(rows - 1)) {
        val sc : Float = sc1.divideScale(j, rows)
        if (sc == 0f) {
            break
        }
        val sck1 : Float = sc.divideScale(0, 2)
        val sck2 : Float = sc.divideScale(1, 2)
        var currX : Float = 0f
        for (t in 0..j) {
            if (t == j) {
                currX += ballGap * sck2
            } else {
                currX += ballGap
            }
            drawCircle(currX, currY, r, paint)
        }
        currY += ballGap * sck1
    }
    restore()
}

class TriangleBallsStepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateScale(dir, rows * movements, 1)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class TBSNode(var i : Int, val state : State = State()) {

        private var prev : TBSNode? = null

        private var next : TBSNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = TBSNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawTBSNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : TBSNode {
            var curr : TBSNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class TriangleBallStep(var i : Int) {

        private val root : TBSNode = TBSNode(0)
        private var curr : TBSNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : TriangleBallsStepView) {

        private val animator : Animator = Animator(view)
        private var tbs : TriangleBallStep = TriangleBallStep(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            tbs.draw(canvas, paint)
            animator.animate {
                tbs.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            tbs.startUpdating {
                animator.start()
            }
        }
    }
}
