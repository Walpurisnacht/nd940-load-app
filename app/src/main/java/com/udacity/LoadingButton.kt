package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.renderscript.Sampler.Value
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.addListener
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private val arcMargin = 16F

    // Define custom view's custom attributes
    private var buttonBackgroundColor = 0
    private var buttonProgressColor = 0
    private var buttonTextColor = 0
    private var progress = 0F

    private val valueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
        // Hard-coded animation duration to 2 seconds
        // TODO sync animation with download real progress
        duration = 2000
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener { valueAnimator ->
            progress = valueAnimator.animatedFraction
            invalidate()
        }
        addListener(
            onStart = {
                progress = 0F
                buttonState = ButtonState.Loading
            },
            onEnd = {
                buttonState = ButtonState.Completed
            }
        )
    }!!

    var buttonState: ButtonState
            by Delegates.observable(ButtonState.Completed) { _, _, newState ->
                when (newState) {
                    ButtonState.Completed -> {
                        // Allow button to be clickable after 2 secs
                        // Note: real purpose is only clickable when done downloading
                        isClickable = true
                        isFocusable = true

                        valueAnimator.repeatCount = 0
                    }
                    ButtonState.Clicked -> {
                        // Disable button click until download complete
                        isClickable = false
                        isFocusable = false

                        // Restart downloading animation
                        valueAnimator.cancel()
                        valueAnimator.repeatCount = ValueAnimator.INFINITE
                        valueAnimator.start()
                    }
                    ButtonState.Loading -> {
                        // Do nothing besides default behavior which invalidates the view to draw new
                        // progress status image
                    }
                }
                invalidate()
            }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    init {
        // Init button default state
        isClickable = true
        isFocusable = true
        buttonState = ButtonState.Completed

        // Set custom attributes of button
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonBackgroundColor = getColor(R.styleable.LoadingButton_buttonBackgroundColor, 0)
            buttonProgressColor = getColor(R.styleable.LoadingButton_buttonProgressColor, 0)
            buttonTextColor = getColor(R.styleable.LoadingButton_buttonTextColor, 0)
        }
    }

    override fun performClick(): Boolean {

        // Handle button state when onClick
        if (ButtonState.Completed == buttonState) {
            buttonState = ButtonState.Clicked
        }
        super.performClick()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        when (buttonState) {
            ButtonState.Completed -> onDrawButtonInitialState(canvas)
            ButtonState.Loading -> onDrawDownloadingButtonState(canvas)
            else -> return
        }
    }

    private fun onDrawButtonInitialState(canvas: Canvas) {
        //===========  Button shape draw START  ===========//
        paint.color = buttonBackgroundColor

        // Draw button border
        canvas.drawRect(0F, 0F, widthSize.toFloat(), heightSize.toFloat(), paint)
        //===========  Button shape draw END  ===========//

        //===========  Button label draw START  ===========//
        paint.color = buttonTextColor

        // Calculate center coordinate of both x,y axis to put text in center
        // Horizontal center of the button
        val coordX = (widthSize / 2.0).toFloat()
        // Vertical center of the button, leaving some space for text height
        val coordY = ((heightSize.toFloat() - paint.descent() - paint.ascent()) / 2.0).toFloat()

        // Draw button label
        canvas.drawText(resources.getString(R.string.label_download), coordX, coordY, paint)
        //===========  Button label draw END  ===========//
    }

    private fun onDrawDownloadingButtonState(canvas: Canvas) {
        //===========  Button shape draw START  ===========//
        // Calculate completed part width base on progress
        val progressWidth = widthSize * progress

        // Overlay completed part base on progress
        paint.color = buttonProgressColor
        canvas.drawRect(0F, 0F, progressWidth, heightSize.toFloat(), paint)

        // Fill the rest with default background color
        paint.color = buttonBackgroundColor
        canvas.drawRect(progressWidth, 0F, widthSize.toFloat(), heightSize.toFloat(), paint)
        //===========  Button shape draw END  ===========//

        //===========  Button label draw START  ===========//
        paint.color = buttonTextColor

        // Calculate center coordinate of both x,y axis to put text in center
        // Horizontal center of the button
        val coordX = widthSize.toFloat() / 2.0F
        // Vertical center of the button, leaving some space for text height
        val coordY = (heightSize.toFloat() - paint.descent() - paint.ascent()) / 2F

        // Draw button label
        canvas.drawText(resources.getString(R.string.button_loading), coordX, coordY, paint)
        //===========  Button label draw END  ===========//

        //===========  Button progress circle draw START  ===========//
        // Circle at right most of button
        val arcLeft = widthSize.toFloat() - heightSize.toFloat() + arcMargin
        val arcRight = widthSize.toFloat() - arcMargin
        val arcTop = arcMargin
        val arcBottom = heightSize.toFloat() - arcMargin

        paint.color = resources.getColor(R.color.colorAccent, null)
        canvas.drawArc(
            arcLeft, arcTop, arcRight, arcBottom, 0.0F,
            progress * 360, true, paint
        )
        //===========  Button progress circle draw END  ===========//
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        // Set view measured dimensions to use later
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}
