package com.developer.projects.demo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final int INIT_TIP_PERCENT = 15;

    private EditText baseAmount;
    private SeekBar tipSeekBar;
    private TextView tipPercent;
    private TextView tipAmount;
    private TextView totalAmount;

    private void computeTipAndTotal() {
        double base = Double.parseDouble(baseAmount.getText().toString());
        double pct = tipSeekBar.getProgress();
        double tip = base * pct / 100;
        double total = base + tip;

        tipAmount.setText(String.valueOf(tip));
        totalAmount.setText(String.valueOf(total));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        baseAmount = findViewById(R.id.tvBaseAmount);
        tipSeekBar = findViewById(R.id.skTipPercent);
        tipPercent = findViewById(R.id.tvTipPercentLabel);
        tipAmount = findViewById(R.id.tvTipAmount);
        totalAmount = findViewById(R.id.tvTotalAmount);

        tipSeekBar.setProgress(INIT_TIP_PERCENT);
        tipPercent.setText(String.valueOf(INIT_TIP_PERCENT));

        tipSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tipPercent.setText(String.valueOf(progress));
                computeTipAndTotal();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        baseAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                computeTipAndTotal();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}