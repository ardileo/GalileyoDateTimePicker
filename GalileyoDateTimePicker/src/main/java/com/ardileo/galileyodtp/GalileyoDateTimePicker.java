package com.ardileo.galileyodtp;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ViewFlipper;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
/**
 * Created by Ardi Leo on 28/12/19
 * ardileyo@gmail.com
 */

public class GalileyoDateTimePicker implements View.OnClickListener {
    private final static int BTN_LEFT_ID = 12221;
    private final static int BTN_RIGHT_ID = 21112;
    private final static String STR_BACK = "Back";
    private final static String STR_SET = "Set";
    private final static String STR_CANCEL = "Cancel";
    private final static String STR_NEXT = "Next";
    private final static String STR_SEL_DATE = "Select Date";
    private final static String STR_SEL_TIME = "Pick Time";


    private final Activity activity;
    private final Dialog dialog;
    private View.OnClickListener btnRightToNext, btnLeftPrev;

    private boolean isCancelable = false;
    private boolean isWithTime = true;
    private boolean is24Hour = false;
    private boolean isOnlyTime = false;
    private boolean isOnlyDate = false;

    private Calendar calendar_date, calendar_date_min, calendar_date_max;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private OnSetListener onSetListener;
    DisplaySize displaySize;

    public GalileyoDateTimePicker(Activity activity) {
        this.activity = activity;
        this.activity.setTheme(android.R.style.Theme_Holo_Light_Dialog);
        this.displaySize = new DisplaySize(activity);

        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setWindowAnimations(android.R.style.Animation_Dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        dialog.create();
        calendar_date = Calendar.getInstance(Locale.getDefault());
    }

    private View getDateTimePickerLayout() {
        LinearLayout.LayoutParams lp_matchwrap = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        int color_bg = Color.parseColor("#F1F1F5");
        int color_fg = Color.parseColor("#ffffff");

        LinearLayout container = new LinearLayout(activity);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundColor(color_bg);

        final TextView tvTitle = new TextView(activity);
        tvTitle.setBackgroundColor(color_fg);
        tvTitle.setLayoutParams(lp_matchwrap);
        tvTitle.setTextAppearance(android.R.style.TextAppearance_Holo_Widget_ActionBar_Title);
        tvTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvTitle.setText(STR_SEL_DATE);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setPadding(0, dpToPixels(15), 0, dpToPixels(15));

        final ViewFlipper viewFlipper = new ViewFlipper(activity);
        datePicker = new DatePicker(activity);
        timePicker = new TimePicker(activity);

        datePicker.setCalendarViewShown(false);
        datePicker.setLayoutParams(lp_matchwrap);
        timePicker.setLayoutParams(lp_matchwrap);

        viewFlipper.addView(datePicker, 0);
        viewFlipper.addView(timePicker, 1);

        LinearLayout btnContainer = new LinearLayout(activity);
        btnContainer.setOrientation(LinearLayout.HORIZONTAL);
        btnContainer.setLayoutParams(lp_matchwrap);


        TypedValue outValue = new TypedValue();
        activity.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        int selectedItemFG = outValue.resourceId;

        final Button btnLeft = new Button(activity);
        final Button btnRight = new Button(activity);

        btnLeft.setId(BTN_LEFT_ID);
        btnLeft.setForeground(ContextCompat.getDrawable(activity, selectedItemFG));
        btnLeft.setBackgroundColor(color_fg);
        btnLeft.setText(STR_CANCEL);
        btnLeft.setLayoutParams(lp_matchwrap);
        btnLeft.setOnClickListener(this);
        ((LinearLayout.LayoutParams) btnLeft.getLayoutParams()).weight = 1;
        ((ViewGroup.MarginLayoutParams) btnLeft.getLayoutParams()).rightMargin = 2;

        btnRight.setId(BTN_RIGHT_ID);
        btnRight.setForeground(ContextCompat.getDrawable(activity, selectedItemFG));
        btnRight.setBackgroundColor(color_fg);
        btnRight.setText(STR_SET);
        btnRight.setLayoutParams(lp_matchwrap);
        btnRight.setOnClickListener(this);
        ((LinearLayout.LayoutParams) btnRight.getLayoutParams()).weight = 1;
        ((ViewGroup.MarginLayoutParams) btnRight.getLayoutParams()).leftMargin = 2;

        btnLeftPrev = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvTitle.setText(STR_SEL_DATE);
                viewFlipper.setDisplayedChild(0);
                btnLeft.setText(STR_CANCEL);
                btnLeft.setOnClickListener(GalileyoDateTimePicker.this);
                btnRight.setText(STR_NEXT);
                btnRight.setOnClickListener(btnRightToNext);
            }
        };

        btnRightToNext = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvTitle.setText(STR_SEL_TIME);
                viewFlipper.setDisplayedChild(1);
                btnRight.setOnClickListener(GalileyoDateTimePicker.this);
                btnRight.setText(STR_SET);
                btnLeft.setOnClickListener(btnLeftPrev);
                btnLeft.setText(STR_BACK);
            }
        };


        if (isOnlyTime) {
            tvTitle.setText(STR_SEL_TIME);
            viewFlipper.setDisplayedChild(1);
        } else {
            if (isWithTime) {
                btnRight.setText(STR_NEXT);
                btnRight.setOnClickListener(btnRightToNext);
            }
        }

        btnContainer.addView(btnLeft);
        btnContainer.addView(btnRight);

        container.addView(tvTitle);
        container.addView(viewFlipper);
        container.addView(btnContainer);
        container.setGravity(Gravity.CENTER_VERTICAL);

        CardView cardView = new CardView(activity);
        cardView.addView(container);
        cardView.setRadius(28f);

        // setter
        dialog.setCancelable(isCancelable);
        datePicker.updateDate(calendar_date.get(Calendar.YEAR), calendar_date.get(Calendar.MONTH), calendar_date.get(Calendar.DATE));
        timePicker.setIs24HourView(is24Hour);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                calendar_date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar_date.set(Calendar.MINUTE, minute);
            }
        });

        if (calendar_date_min != null) {
            datePicker.setMinDate(calendar_date_min.getTime().getTime());
        }

        if (calendar_date_max != null) {
            datePicker.setMinDate(calendar_date_max.getTime().getTime());
        }

        return cardView;
    }

    private int dpToPixels(int i) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, i, activity.getResources().getDisplayMetrics());
    }

    public GalileyoDateTimePicker setIs24Hour() {
        is24Hour = true;
        return this;
    }

    public GalileyoDateTimePicker onSet(OnSetListener onSetListener) {
        this.onSetListener = onSetListener;
        return this;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case BTN_LEFT_ID:
                dialog.dismiss();
                break;
            case BTN_RIGHT_ID:
                if (onSetListener != null) {
                    String ampm = (calendar_date.get(Calendar.AM_PM) == (Calendar.AM)) ? "AM" : "PM";
                    int hourin24Format = calendar_date.get(Calendar.HOUR_OF_DAY);
                    int hourIn12Format = hourin24Format == 0 ? 12 : (hourin24Format <= 12 ? hourin24Format : hourin24Format - 12);
                    onSetListener.onSet(calendar_date,
                            calendar_date.getTime(),
                            hourin24Format,
                            hourIn12Format,
                            calendar_date.get(Calendar.MINUTE),
                            calendar_date.get(Calendar.SECOND),
                            ampm);
                }
                dialog.dismiss();
                break;
        }
    }


    public void showTimePicker() {
        isOnlyDate = false;
        isOnlyTime = true;
        show();
    }

    public void showDatePicker() {
        isOnlyDate = true;
        isWithTime = false;
        show();
    }

    public GalileyoDateTimePicker setCurrentDate(Date toDate) {
        calendar_date = Calendar.getInstance();
        calendar_date.setTime(toDate);
        return this;
    }

    public GalileyoDateTimePicker setMinDate(Date toDate) {
        calendar_date_min = Calendar.getInstance();
        calendar_date_min.setTime(toDate);
        return this;
    }

    public GalileyoDateTimePicker setMaxDate(Date toDate) {
        calendar_date_max = Calendar.getInstance();
        calendar_date_max.setTime(toDate);
        return this;
    }

    public interface OnSetListener {
        void onSet(Calendar selCalendar, Date selDate, int hour24, int hour12, int minute, int second, String AM_PM);
    }

    public void show() {
        dialog.setContentView(getDateTimePickerLayout());
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private static class DisplaySize {
        int width, height;

        DisplaySize(Activity activity) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            width = size.x;
            height = size.y;
        }
    }

    private static Bitmap takeScreenShot(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);

        int statusBarHeight = frame.top;
        DisplaySize displaySize = new DisplaySize(activity);
        Bitmap bitmap = Bitmap.createBitmap(b1, 0, statusBarHeight, displaySize.width, displaySize.height - statusBarHeight);
        view.destroyDrawingCache();

        // This will blur the bitmapOriginal with a radius of 16 and save it in bitmapOriginal
        RenderScript renderScript = RenderScript.create(activity);
        final Allocation input = Allocation.createFromBitmap(renderScript, bitmap); // Use this constructor for best performance, because it uses USAGE_SHARED mode which reuses memory
        final Allocation output = Allocation.createTyped(renderScript, input.getType());
        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        script.setRadius(16f);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(bitmap);

        // Buat lebih gelap
        Canvas canvas = new Canvas(bitmap);
        canvas.drawARGB(100, 0, 0, 0);

        return bitmap;
    }
}
