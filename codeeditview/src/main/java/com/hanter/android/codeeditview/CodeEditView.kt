package com.hanter.android.codeeditview

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.text.Editable
import android.text.InputType
import android.text.TextPaint
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager


/**
 * 短信验证码输入控件
 */
class CodeEditView : View {

    companion object {
        @JvmStatic
        val BLINK: Int = 500

        @JvmStatic
        var DEBUG = false

        @JvmStatic
        val TAG = "CodeEditView"
    }

    private var _codeText: String? = null // 字符串
    private var _codeTextColor: Int = Color.RED // 颜色
    private var _codeTextSize: Float = 0f // 大小

    private var _codeWidth: Int = 100 // 验证码字符宽度
    private var _dividerWidth: Int = 20 // 验证码之间的间隔宽度

    private var editable: Editable = Editable.Factory.getInstance().newEditable("")

    private var textBounds = Rect()

    private lateinit var textPaint: TextPaint
    private var textWidth: Float = 0f
    private var textHeight: Float = 0f

    private lateinit var cursorPaint: TextPaint
    private var cursorDrawable: Drawable? = null
    private var mBlink: Blink? = null
    private var mShowCursor: Long = SystemClock.uptimeMillis()

    private val count = 4

    private val listener = DigitsKeyListener(false, true)

    private var inputMethodManager: InputMethodManager? = null

    var onCodeCompleteListener: OnCodeCompleteListener? = null

    /**
     * The text to draw
     */
    var codeText: String?
        get() = _codeText
        set(value) {
            _codeText = value?.substring(0, Math.min(value.length, count))
            editable.clear()
            editable.append(_codeText)
            invalidateTextPaintAndMeasurements()
        }

    /**
     * The font color
     */
    var codeTextColor: Int
        get() = _codeTextColor
        set(value) {
            _codeTextColor = value
            invalidateTextPaintAndMeasurements()
        }

    var codeTextSize: Float
        get() = _codeTextSize
        set(value) {
            _codeTextSize = value
            invalidateTextPaintAndMeasurements()
        }

    /**
     * In the example view, this drawable is drawn above the text.
     */
    var codeDrawable: Drawable? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo?): InputConnection {
        outAttrs?.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

//        val ic = inputConnection(this)
//        outAttrs.initialSelStart = getSelectionStart()
//        outAttrs.initialSelEnd = getSelectionEnd()
//        outAttrs.initialCapsMode = ic.getCursorCapsMode(getInputType())
//        return ic

//        val ic = BaseInputConnection(this@CodeEditView, false)

        return BaseInputConnection(this@CodeEditView, false)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (DEBUG) {
            Log.d(TAG, "onKeyDown: $keyCode")
        }

        // 接收按键事件，67是删除键(backspace)，7-16就是0-9
        if (keyCode == KeyEvent.KEYCODE_DEL && editable.isNotEmpty()) {
            val charLength = editable.length
            editable.delete(editable.length - 1, editable.length)
            invalidate()
            if (charLength == count) {
                makeBlink()
            }
        } else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9
                && editable.length < count) {
            listener.onKeyDown(this@CodeEditView, editable, keyCode, event)
            if (editable.length == count) {
                onCodeCompleteListener?.onCodeComplete(editable.toString())
            }
            invalidate()
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(attrs, R.styleable.CodeEditView, defStyle, 0)

        _codeText = a.getString(R.styleable.CodeEditView_cev_code_text)
        _codeTextColor = a.getColor(R.styleable.CodeEditView_cev_code_textColor, codeTextColor)

        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        _codeTextSize = a.getDimension(R.styleable.CodeEditView_cev_code_textSize, codeTextSize)

        if (a.hasValue(R.styleable.CodeEditView_cev_code_drawable)) {
            codeDrawable = a.getDrawable(R.styleable.CodeEditView_cev_code_drawable)
            codeDrawable?.callback = this
        }

        if (a.hasValue(R.styleable.CodeEditView_cev_code_cursorDrawable)) {
            cursorDrawable = a.getDrawable(R.styleable.CodeEditView_cev_code_drawable)
            cursorDrawable?.callback = this
        } else { // TODO 获取系统默认光标
            cursorDrawable = context.resources.getDrawable(R.drawable.code_input_cursor)
        }

        a.recycle()

        // Set up a default TextPaint object
        textPaint = TextPaint()
        textPaint.flags = Paint.ANTI_ALIAS_FLAG
        textPaint.textAlign = Paint.Align.LEFT

        cursorPaint = TextPaint()
        cursorPaint.flags = Paint.ANTI_ALIAS_FLAG
        cursorPaint.textAlign = Paint.Align.LEFT

        codeText = _codeText

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements()

        inputMethodManager = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
    }

    private fun invalidateTextPaintAndMeasurements() {
        textPaint.textSize = codeTextSize
        textPaint.color = codeTextColor
        textWidth = textPaint.measureText(codeText)
        textHeight = textPaint.fontMetrics.bottom
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measureWidth = count * _codeWidth + (count - 1) * _dividerWidth +
                paddingLeft + paddingRight

        val measureHeight = paddingTop + paddingBottom + _codeWidth
        //        val measureHeight = textPaint.getTextBounds("0", 0, 1, textBounds)

        setMeasuredDimension(resolveSize(measureWidth, widthMeasureSpec),
                resolveSize(measureHeight, heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val paddingRight = paddingRight
        val paddingBottom = paddingBottom

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom

        for (i in 0 until count) {
            val itemStart = paddingLeft + i * (_dividerWidth + _codeWidth)

            // 绘制每一项背景框
            codeDrawable?.let {
                it.setBounds(itemStart, paddingTop,
                        itemStart + _codeWidth, paddingTop + contentHeight - paddingBottom)
                it.draw(canvas)
            }

            // 绘制文本
            if (i < editable.length) {
                val width = textPaint.measureText(editable, i, i + 1)
                canvas.drawText(editable,
                        i,
                        i + 1,
                        itemStart + (_codeWidth - width) / 2,
                        paddingTop + (contentHeight + textHeight) / 2,
                        textPaint)
            }
        }

        // 如果未达到
        if (editable.length < count && (mBlink?.isCancelled()?.not() == true)) {
            cursorDrawable?.let {
                val cursorStart = paddingLeft + editable.length * (_dividerWidth + _codeWidth) +
                        _codeWidth / 2 - it.intrinsicWidth / 2

                val cursorTop = paddingTop + (contentHeight - it.intrinsicHeight) / 2
                it.setBounds(cursorStart, cursorTop, cursorStart + it.intrinsicWidth,
                        cursorTop + it.intrinsicHeight)
                drawCursor(canvas)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val actionMasked = event?.actionMasked
        val action = event?.action

        when (actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                super.onTouchEvent(event)
                return true
            }

            MotionEvent.ACTION_UP -> {
                val touchIsFinished = (action == MotionEvent.ACTION_UP) and isFocused
                if (touchIsFinished) {
                    inputMethodManager?.viewClicked(this)
                    inputMethodManager?.showSoftInput(this, 0)
                    super.onTouchEvent(event)
                    return true
                }

                return super.onTouchEvent(event)
            }

            else -> {
                super.onTouchEvent(event)
                return true
            }

        }
    }

    override fun onScreenStateChanged(screenState: Int) {
        super.onScreenStateChanged(screenState)

        when (screenState) {
            View.SCREEN_STATE_ON -> resumeBlink()
            View.SCREEN_STATE_OFF -> suspendBlink()
        }
    }

    private fun drawCursor(canvas: Canvas) {
//        cursorPaint

//        val translate = cursorOffsetVertical != 0.0f
//        if (translate) canvas.translate(0f, cursorOffsetVertical)

        cursorDrawable?.draw(canvas)

        if (DEBUG)
            Log.d(TAG, "alpha: $")

//        if (translate) canvas.translate(0f, (-cursorOffsetVertical))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        resumeBlink()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        suspendBlink()
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)

        mShowCursor = SystemClock.uptimeMillis()
        if (gainFocus) {
            resumeBlink()
            showInputMethod()
        } else {
            suspendBlink()
            invalidate()
        }
    }

    fun showInputMethod() {
        postDelayed(showImRunnable, 100)
    }

    /**
     * 关闭输入法
     */
    fun closeInputMethod() {
        post(closeImRunnable)
    }

    private val showImRunnable = Runnable {
        inputMethodManager?.viewClicked(this@CodeEditView)
        inputMethodManager?.showSoftInput(this@CodeEditView, InputMethodManager.SHOW_FORCED)
    }

    private val closeImRunnable = Runnable {
        if (inputMethodManager?.isActive == true) {
            inputMethodManager?.hideSoftInputFromInputMethod(windowToken, 0)
        }
    }

    private fun suspendBlink() {
        mBlink?.cancel()
    }

    private fun resumeBlink() {
        mBlink?.uncancel()
        makeBlink()
    }

    private fun shouldBlink(): Boolean {
        return isFocused and (editable.length < count)
    }

    private fun makeBlink() {
        if (shouldBlink()) {
            mShowCursor = SystemClock.uptimeMillis()
            if (mBlink == null) mBlink = Blink(this)
            removeCallbacks(mBlink)
            postDelayed(mBlink, BLINK.toLong())
        } else {
            if (mBlink != null) removeCallbacks(mBlink)
        }
    }

    private fun invalidateCursorPath() {
        // Switch the cursor visibility and set it
        val newAlpha = if (cursorPaint.alpha == 0) 255 else 0
        cursorPaint.alpha = newAlpha
        cursorDrawable?.alpha = newAlpha

        invalidate()
    }

    private inner class Blink(private val codeView: CodeEditView) : Runnable {

        private var mCancelled: Boolean = false

        override fun run() {
            if (mCancelled) {
                return
            }
            codeView.removeCallbacks(this)

            if (shouldBlink()) {
                codeView.invalidateCursorPath()
                codeView.postDelayed(this, BLINK.toLong())
            }
        }

        internal fun isCancelled(): Boolean {
            return mCancelled
        }

        internal fun cancel() {
            if (!mCancelled) {
                codeView.removeCallbacks(this)
                mCancelled = true
            }
        }

        internal fun uncancel() {
            mCancelled = false
        }
    }


    interface OnCodeCompleteListener {
        fun onCodeComplete(code: String)
    }

}
