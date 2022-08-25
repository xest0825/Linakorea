package kr.co.lina.ga.webcore

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import androidx.core.view.NestedScrollingChild2
import androidx.core.view.NestedScrollingChildHelper
import kr.co.lina.ga.utils.WaLog
import kr.co.lina.ga.utils.WaUtils

/**
 * 메인 웹뷰
 * @property tag Log 태그
 * @property Log Log
 * @property lastMotionY 마지막 스크롤 포지션
 * @property nestedYOffset
 * @property scrollOffset
 * @property scrollConsumed
 * @property childHelper
 * @property location
 * @property currentY1
 * @property SCROLL_UP_THRESHOLD
 * @property mScrollY
 * @property gestureDetector
 * @property maxFling Fling 범위 지정
 * @property mActivity Activity
 */
class WcNsWebView constructor(
    context: Context,
    attrs: AttributeSet
) : WebView(context, attrs), NestedScrollingChild2 {

    private var lastMotionY = 0
    private var nestedYOffset = 0
    private val scrollOffset = IntArray(2)
    private val scrollConsumed = IntArray(2)
    private val childHelper = NestedScrollingChildHelper(this)

    init {
        isNestedScrollingEnabled = true
    }

    private val tag = "NestedScrollWebView"
    private val Log = WaLog

    private var location: Float = 0f
    private var currentY1: Float = 0f
    private val SCROLL_UP_THRESHOLD = WaUtils.dpToPx(10f)
    private var mScrollY: Int = 0
    //private var mFragment: WaUiFragment? = null
    private var gestureDetector: GestureDetector? = null

    private var maxFling = 50

    // static 정의
    //companion object { }

    /*
    fun setWaUiFragment(wauifragment: WaUiFragment?) {
        Log.i(tag, "setWaUiFragment:")
        this.mFragment = wauifragment
        gestureDetector = GestureDetector(mFragment?.pActivity, CustomGestureListener())
    }
     */

    private var mActivity: Activity? = null

    fun setMainActivity(activity: Activity) {
        mActivity = activity
    }

    /**
     * 터치 이벤트 설정
     * @param event
     * @return Boolean
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false
        //Log.i(tag, "NestedScrollWebView: event_ACTION_${event.action}  event.y ${event.y}")
        val motionEvent = MotionEvent.obtain(event)
        val currentY = event.y.toInt()
        currentY1 = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                location = currentY1
                // 원본
                nestedYOffset = 0
                lastMotionY = currentY
                startNestedScroll(View.SCROLL_AXIS_VERTICAL)
            }

            MotionEvent.ACTION_MOVE -> {
                var deltaY = lastMotionY - currentY
                if (dispatchNestedPreScroll(0, deltaY, scrollConsumed, scrollOffset)) {
                    deltaY -= scrollConsumed[1]
                    motionEvent.offsetLocation(0f, scrollOffset[1].toFloat())
                    nestedYOffset += scrollOffset[1]
                }
                lastMotionY = currentY - scrollOffset[1]

                val oldY = scrollY
                val newScrollY = Math.max(0, oldY + deltaY)
                val dyConsumed = newScrollY - oldY
                val dyUnconsumed = deltaY - dyConsumed

                if (dispatchNestedScroll(0, dyConsumed, 0, dyUnconsumed, scrollOffset)) {
                    lastMotionY -= scrollOffset[1]
                    motionEvent.offsetLocation(0f, scrollOffset[1].toFloat())
                    nestedYOffset += scrollOffset[1]
                }
                motionEvent.recycle()
            }
            MotionEvent.ACTION_UP -> {
                val distance = currentY1 - location
                if (distance > SCROLL_UP_THRESHOLD && mScrollY < SCROLL_UP_THRESHOLD) {
                    // pActivity.showTopTitle()
                } else if (distance < -SCROLL_UP_THRESHOLD) {
                    // pActivity.hideTopTitle()
                }
                location = 0f
            }
            MotionEvent.ACTION_POINTER_DOWN,
            MotionEvent.ACTION_CANCEL -> stopNestedScroll()
            else -> {
            }
        }

        // 제스처 설정
        gestureDetector?.onTouchEvent(event)

        return super.onTouchEvent(event)
    }

    /**
     * 스크롤이벤트 설정
     * @param scrollX
     * @param scrollY
     * @param clampedX
     * @param clampedY
     */
    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        //Log.i(tag,"NestedScrollWebView: onOverScrolled: scrollX:$scrollX scrollY:$scrollY clampedX:$clampedX, clampedY:$clampedY")
        mScrollY = scrollY
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
    }

    /**
     * 스크롤 변경 이벤트
     * @param l
     * @param t
     * @param oldl
     * @param oldt
     */
    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        //Log.i(tag, "NestedScrollWebView: onScrollChanged: l:$l t:$t oldl:$oldl, oldt:$oldl")
        super.onScrollChanged(l, t, oldl, oldt)
    }

    /**
     * 스크롤 시작 이벤트
     * @param axes
     * @param type
     */
    override fun startNestedScroll(axes: Int, type: Int) = childHelper.startNestedScroll(axes, type)

    /**
     * 스크롤 스탑 이벤트
     * @param type
     */
    override fun stopNestedScroll(type: Int) = childHelper.stopNestedScroll(type)

    override fun hasNestedScrollingParent(type: Int) = childHelper.hasNestedScrollingParent(type)

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?,
        type: Int
    ) =
        childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int
    ) =
        childHelper.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow,
            type
        )

    /**
     * 제스쳐 이벤트 설정
     * @return GestureDetector.SimpleOnGestureListener()
     */
    private inner class CustomGestureListener : GestureDetector.SimpleOnGestureListener() {

        private var canTriggerLongPress = true

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val power = (velocityY * 100 / maxFling).toInt()
            if (power < -10) {
                // (mActivity as MainActivity).hideTopTitle()
            } else if (power > 15) {
                // pActivity?.showTopTitle()
            }
            return super.onFling(e1, e2, velocityX, velocityY)
        }

        /**
         * 롱터치 이벤트
         * @param e MotionEvent
         */
        override fun onLongPress(e: MotionEvent) {
            //if (canTriggerLongPress) { }
        }

        /**
         * 더블탭 이벤트
         * @param e MotionEvent
         */
        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            canTriggerLongPress = false
            return false
        }

        /**
         * 일반 터치보다 조금 긴 터치 이벤트
         * @param e MotionEvent
         */
        override fun onShowPress(e: MotionEvent) {
            canTriggerLongPress = true
        }
    }

}