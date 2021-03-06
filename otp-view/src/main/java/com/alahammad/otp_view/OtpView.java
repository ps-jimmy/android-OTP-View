package com.alahammad.otp_view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class OtpView extends LinearLayout {
    private EditText mCurrentlyFocusedEditText;

    private OTPListener onOtpFinished;

    private List<EditText> otpViews = new ArrayList<>();

    public OtpView(Context context) {
        super(context);
        init(null);
    }

    public OtpView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public OtpView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray styles = getContext().obtainStyledAttributes(attrs, R.styleable.OtpView);
        LayoutInflater mInflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.otpview_layout, this);

        int otpCount = styles.getInt(R.styleable.OtpView_otp_number, 5);
        for (int i = 0; i < otpCount; i++) {
            EditText editText = createEditText(i);
            otpViews.add(editText);
            addView(editText);
        }
        styleEditTexts(styles);
        styles.recycle();
    }


    private EditText createEditText(int i) {
        EditText editText = new EditText(getContext());
        editText.setHeight(getResources().getDimensionPixelSize(R.dimen.otp_size));
        editText.setWidth(getResources().getDimensionPixelSize(R.dimen.otp_size));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 16, 0);
        lp.gravity = Gravity.CENTER;
        editText.setId(i);

        editText.setLayoutParams(lp);
        editText.setGravity(Gravity.CENTER);
        editText.setMaxLines(1);
        setMaxLength(editText);
        return editText;
    }


    private void setMaxLength(EditText editText) {
        int maxLength = 1;
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        editText.setFilters(fArray);
    }


    /**
     * Get an instance of the present otp
     */
    private String makeOTP() {
        StringBuilder stringBuilder = new StringBuilder();
        for (EditText otpEditText : otpViews) {
            stringBuilder.append(otpEditText.getText().toString());
        }
        return stringBuilder.toString();
    }

    /**
     * Checks if all four fields have been filled
     *
     * @return length of OTP
     */
    public boolean hasValidOTP() {
        return makeOTP().length() == 4;
    }

    /**
     * Returns the present otp entered by the user
     *
     * @return OTP
     */
    public String getOTP() {
        return makeOTP();
    }

    /**
     * Used to set the OTP. More of cosmetic value than functional value
     *
     * @param otp Send the four digit otp
     */
    public void setOTP(String otp) {
        if (otp.length() != otpViews.size()) {
            Log.e("OTPView", "Invalid otp param");
            return;
        }
        if (otpViews.get(0).getInputType() == InputType.TYPE_CLASS_NUMBER
                && !otp.matches("[0-9]+")) {
            Log.e("OTPView", "OTP doesn't match INPUT TYPE");
            return;
        }
        for (int i = 0; i < otpViews.size(); i++) {
            otpViews.get(i).setText(String.valueOf(otp.charAt(i)));
        }
    }

    private void styleEditTexts(TypedArray styles) {
        int textColor = styles.getColor(R.styleable.OtpView_android_textColor, Color.BLACK);
        int backgroundColor =
                styles.getColor(R.styleable.OtpView_text_background_color, Color.TRANSPARENT);
        if (styles.getColor(R.styleable.OtpView_text_background_color, Color.TRANSPARENT)
                != Color.TRANSPARENT) {
            for (EditText otpEditText : otpViews) {
                otpEditText.setBackgroundColor(backgroundColor);
            }
        } else {
            for (EditText otpEditText : otpViews) {
                otpEditText.getBackground().mutate().setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);
            }
        }
        for (int i = 0; i < otpViews.size(); i++) {

            otpViews.get(i).setTextColor(textColor);
            EditText next;
            if (i==0){
                next = otpViews.get(i+1);
            }else if (i<otpViews.size()-1){
                next = otpViews.get(i + 1);
            }else {
                next = otpViews.get(otpViews.size()-1);
            }
            otpViews.get(i).setNextFocusRightId(next.getId());

            EditText left;
            if (i == 0) {
                left = otpViews.get(0);
            } else if (i < otpViews.size() - 1) {
                left = otpViews.get(i + 1);
            } else {
                left = otpViews.get(i);
            }
            otpViews.get(i).setNextFocusLeftId(left.getId());
        }
        setEditTextInputStyle(styles);
    }

    private void setEditTextInputStyle(TypedArray styles) {
        int inputType =
                styles.getInt(R.styleable.OtpView_android_inputType, EditorInfo.TYPE_TEXT_VARIATION_NORMAL);

        for (EditText otpEditText : otpViews) {
            otpEditText.setInputType(inputType);
        }
        String text = styles.getString(R.styleable.OtpView_otp);
        if (!TextUtils.isEmpty(text) && text.length() == otpViews.size() - 1) {
            for (int i = 0; i < otpViews.size(); i++) {
                otpViews.get(i).setText(String.valueOf(text.charAt(i)));
            }

        }
        setFocusListener();
        setOnTextChangeListener();
    }

    private void setFocusListener() {
        View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mCurrentlyFocusedEditText = (EditText) v;
                mCurrentlyFocusedEditText.setSelection(mCurrentlyFocusedEditText.getText().length());
            }
        };
        for (EditText otpEditText : otpViews) {
            otpEditText.setOnFocusChangeListener(onFocusChangeListener);
        }
    }

    public void disableKeypad() {
        OnTouchListener touchListener = new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.onTouchEvent(event);
                InputMethodManager imm =
                        (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return true;
            }
        };
        for (EditText otpEdittext : otpViews) {
            otpEdittext.setOnTouchListener(touchListener);
        }
    }

    public void enableKeypad() {
        OnTouchListener touchListener = new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        };
        for (EditText otpEdittext : otpViews) {
            otpEdittext.setOnTouchListener(touchListener);
        }
    }

    public void setOnOtpFinished(OTPListener onOtpFinished) {
        this.onOtpFinished = onOtpFinished;
    }

    public EditText getCurrentFoucusedEditText() {
        return mCurrentlyFocusedEditText;
    }

    private void setOnTextChangeListener() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mCurrentlyFocusedEditText.getText().length() >= 1
                        && mCurrentlyFocusedEditText != otpViews.get(otpViews.size() - 1)) {
                    mCurrentlyFocusedEditText.focusSearch(View.FOCUS_RIGHT).requestFocus();
                } else if (mCurrentlyFocusedEditText.getText().length() >= 1
                        && mCurrentlyFocusedEditText == otpViews.get(otpViews.size() - 1)) {
                    InputMethodManager imm =
                            (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(getWindowToken(), 0);
                        if (onOtpFinished != null)
                            onOtpFinished.otpFinished(makeOTP());
                    }
                } else {
                    String currentValue = mCurrentlyFocusedEditText.getText().toString();
                    if (currentValue.length() <= 0 && mCurrentlyFocusedEditText.getSelectionStart() <= 0) {
                        mCurrentlyFocusedEditText.focusSearch(View.FOCUS_LEFT).requestFocus();
                    }
                }
            }
        };
        for (EditText otpEditext : otpViews) {
            otpEditext.addTextChangedListener(textWatcher);
        }
    }

    public void simulateDeletePress() {
        mCurrentlyFocusedEditText.setText("");
    }
}
