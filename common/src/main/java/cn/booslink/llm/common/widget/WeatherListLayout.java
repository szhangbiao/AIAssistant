package cn.booslink.llm.common.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Locale;

import cn.booslink.llm.common.R;
import cn.booslink.llm.common.di.CommonEntryPoint;
import cn.booslink.llm.common.image.ImageLoader;
import cn.booslink.llm.common.model.WeatherUI;
import dagger.hilt.android.EntryPointAccessors;

public class WeatherListLayout extends ConstraintLayout {

    private TextView tvDate;
    private TextView tvLocation;
    private TextView tvWeatherTitle;
    private ImageView ivWeatherIcon;
    private TextView tvWeatherDesc;
    private TextView tvTempHigh;
    private TextView tvTempLow;

    private TextView tvDate1;
    private ImageView ivIcon1;
    private TextView tvTemp1;
    private TextView tvDate2;
    private ImageView ivIcon2;
    private TextView tvTemp2;
    private TextView tvDate3;
    private ImageView ivIcon3;
    private TextView tvTemp3;
    private TextView tvDate4;
    private ImageView ivIcon4;
    private TextView tvTemp4;


    public WeatherListLayout(@NonNull Context context) {
        this(context, null);
    }

    public WeatherListLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeatherListLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateLayout(context);
        initWidgets();
    }

    private void inflateLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_weather_list, this, true);
    }

    private void initWidgets() {
        tvDate = findViewById(R.id.tv_date);
        tvLocation = findViewById(R.id.tv_location);
        tvWeatherTitle = findViewById(R.id.tv_weather_title);
        ivWeatherIcon = findViewById(R.id.iv_weather_icon);
        tvWeatherDesc = findViewById(R.id.tv_weather_desc);
        tvTempHigh = findViewById(R.id.tv_temp_high);
        tvTempLow = findViewById(R.id.tv_temp_low);

        tvDate1 = findViewById(R.id.tv_date_1);
        ivIcon1 = findViewById(R.id.iv_icon_1);
        tvTemp1 = findViewById(R.id.tv_temp_1);
        tvDate2 = findViewById(R.id.tv_date_2);
        ivIcon2 = findViewById(R.id.iv_icon_2);
        tvTemp2 = findViewById(R.id.tv_temp_2);
        tvDate3 = findViewById(R.id.tv_date_3);
        ivIcon3 = findViewById(R.id.iv_icon_3);
        tvTemp3 = findViewById(R.id.tv_temp_3);
        tvDate4 = findViewById(R.id.tv_date_4);
        ivIcon4 = findViewById(R.id.iv_icon_4);
        tvTemp4 = findViewById(R.id.tv_temp_4);
    }

    public void updateWeatherUI(WeatherUI weatherData) {
        CommonEntryPoint hiltEntryPoint = EntryPointAccessors.fromApplication(getContext().getApplicationContext(), CommonEntryPoint.class);
        ImageLoader imageLoader = hiltEntryPoint.imageLoader();
        if (weatherData == null) return;
        if (weatherData.getCurrent() != null) {
            tvDate.setText(weatherData.getCurrent().getDateForVoice());
            tvLocation.setText(weatherData.getCurrent().getCity());
            tvWeatherTitle.setText(String.format(Locale.getDefault(), "%s%s天气%s，%s", weatherData.getCurrent().getCity(), weatherData.getCurrent().getDateForVoice(), weatherData.getCurrent().getWeather(), weatherData.getCurrent().getWeatherDescription()));
            tvWeatherDesc.setText(weatherData.getCurrent().getWeather());
            tvTempHigh.setText(String.format(Locale.getDefault(), "最高%s", weatherData.getCurrent().getTempHigh()));
            tvTempLow.setText(String.format(Locale.getDefault(), "最低%s", weatherData.getCurrent().getTempLow()));
            if (!TextUtils.isEmpty(weatherData.getCurrent().getImg())) {
                imageLoader.loadImage(ivWeatherIcon, weatherData.getCurrent().getImg());
            }
        }
        if (weatherData.getDay1() != null) {
            tvDate1.setText(weatherData.getDay1().getDateForVoice());
            tvTemp1.setText(String.format(Locale.getDefault(), "%s/%s", weatherData.getDay1().getTempLow(), weatherData.getDay1().getTempHigh()));
            if (!TextUtils.isEmpty(weatherData.getDay1().getImg())) {
                imageLoader.loadImage(ivIcon1, weatherData.getDay1().getImg());
            }
        }
        if (weatherData.getDay2() != null) {
            tvDate2.setText(weatherData.getDay2().getDateForVoice());
            tvTemp2.setText(String.format(Locale.getDefault(), "%s/%s", weatherData.getDay2().getTempLow(), weatherData.getDay2().getTempHigh()));
            if (!TextUtils.isEmpty(weatherData.getDay2().getImg())) {
                imageLoader.loadImage(ivIcon2, weatherData.getDay2().getImg());
            }
        }
        if (weatherData.getDay3() != null) {
            tvDate3.setText(weatherData.getDay3().getDateForVoice());
            tvTemp3.setText(String.format(Locale.getDefault(), "%s/%s", weatherData.getDay3().getTempLow(), weatherData.getDay3().getTempHigh()));
            if (!TextUtils.isEmpty(weatherData.getDay3().getImg())) {
                imageLoader.loadImage(ivIcon3, weatherData.getDay3().getImg());
            }
        }
        if (weatherData.getDay4() != null) {
            tvDate4.setText(weatherData.getDay4().getDateForVoice());
            tvTemp4.setText(String.format(Locale.getDefault(), "%s/%s", weatherData.getDay4().getTempLow(), weatherData.getDay4().getTempHigh()));
            if (!TextUtils.isEmpty(weatherData.getDay4().getImg())) {
                imageLoader.loadImage(ivIcon4, weatherData.getDay4().getImg());
            }
        }
    }
}
