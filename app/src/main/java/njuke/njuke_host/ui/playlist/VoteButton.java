package njuke.njuke_host.ui.playlist;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import njuke.njuke_host.R;

/**
 * A subclass of the Button class which keeps track of its toggle
 * state and changes the background drawable based on whether it's
 * clicked or not.
 */
public class VoteButton extends Button {
    /* Debug tag. */
    @SuppressWarnings("UnusedDeclaration")
    public static final String TAG = VoteButton.class.getSimpleName();

    /* Keeps track whether the button is clicked or not. */
    private boolean isClicked;
    /* Resource ID for the regular background. */
    private int backgroundRes;
    /* Resource ID for the clicked background. */
    private int clickedBackgroundRes;
    /* Text color for the regular background. */
    private int textColor;
    /* Text color for the clicked background. */
    private int clickedTextColor;
    /* Statically allocated array with the background attribute ID. */
    private static final int[] backgroundAttrId = new int[] { android.R.attr.background };

    public VoteButton(Context context) {
        super(context);
    }

    public VoteButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp(context, attrs);
    }

    public VoteButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setUp(context, attrs);
    }

    private void setUp(Context context, AttributeSet attrs) {
        // Need to have one TypedArray for each attribute array.
        TypedArray customAttrs = context.obtainStyledAttributes(attrs, R.styleable.VoteButton);
        TypedArray backgroundAttr = context.obtainStyledAttributes(attrs, backgroundAttrId);

        try {
            backgroundRes = backgroundAttr.getResourceId(0, 0);
            clickedBackgroundRes = customAttrs.getResourceId(R.styleable.VoteButton_clickedBackground, 0);
        } finally {
            customAttrs.recycle();
            backgroundAttr.recycle();
        }

        textColor = getResources().getColor(R.color.text_color_primary_dark);
        clickedTextColor = getResources().getColor(R.color.text_color_primary_light);
    }

    @Override
    public boolean performClick() {
        isClicked = !isClicked;
        updateUI();
        return super.performClick();
    }

    /**
     * Sets the state for the button.
     *
     * @param isClicked The new state to set.
     */
    public void setState(boolean isClicked) {
        this.isClicked = isClicked;
        updateUI();
    }

    /**
     * Updates the UI as needed depending on the state of the button.
     */
    private void updateUI() {
        // If no resource was specified for the clicked state, bail.
        if (clickedBackgroundRes == 0) {
            return;
        }

        setBackgroundResource(isClicked ? clickedBackgroundRes : backgroundRes);
        setTextColor(isClicked ? clickedTextColor : textColor);
        setTypeface(null, isClicked ? Typeface.BOLD : Typeface.NORMAL);
    }
}
