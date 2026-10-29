package kz.itk.access;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private Storage storage;
    private Profile scannedProfile;
    private TextView scanResult;
    private TextView saveErrors;
    private EditText ownerKey;
    private EditText someoneKey;
    private EditText govKey;
    private Button saveButton;

    private final ActivityResultLauncher<ScanOptions> scanLauncher =
            registerForActivityResult(new ScanContract(), this::onScanResult);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        storage = new Storage(this);

        scanResult = findViewById(R.id.scanResult);
        saveErrors = findViewById(R.id.saveErrors);
        ownerKey = findViewById(R.id.ownerKey);
        someoneKey = findViewById(R.id.someoneKey);
        govKey = findViewById(R.id.govKey);
        saveButton = findViewById(R.id.saveButton);
        Button scanButton = findViewById(R.id.scanButton);

        saveButton.setEnabled(false);
        scanButton.setOnClickListener(v -> startScan());
        saveButton.setOnClickListener(v -> save());
    }

    private void startScan() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Наведите камеру на QR-код");
        options.setBeepEnabled(true);
        options.setOrientationLocked(false);
        options.addExtra(Intents.Scan.CHARACTER_SET, "UTF-8");
        scanLauncher.launch(options);
    }

    private void onScanResult(ScanIntentResult result) {
        if (result.getContents() == null) {
            scanResult.setText("Сканирование отменено");
            return;
        }

        List<String> errors = new ArrayList<>();
        Profile profile = Validator.parseScan(result.getContents(), errors);
        if (profile == null) {
            scannedProfile = null;
            saveButton.setEnabled(false);
            scanResult.setText("[Валидация] ошибка:\n" + String.join("\n", errors));
            return;
        }

        scannedProfile = profile;
        saveButton.setEnabled(true);
        scanResult.setText("[Валидация] документ распознан\n"
                + "Фамилия: " + profile.surname + "\n"
                + "Имя: " + profile.name + "\n"
                + "Отчество: " + profile.patronymic + "\n"
                + "ИИН: " + profile.iin);
    }

    private void save() {
        Map<Role, String> keys = new EnumMap<>(Role.class);
        keys.put(Role.OWNER, ownerKey.getText().toString());
        keys.put(Role.SOMEONE, someoneKey.getText().toString());
        keys.put(Role.GOV, govKey.getText().toString());

        List<String> errors = new ArrayList<>();
        for (Map.Entry<Role, String> entry : keys.entrySet()) {
            String error = Validator.checkKey(entry.getValue());
            if (error != null) {
                errors.add(entry.getKey().getTitle() + ": " + error);
            }
        }
        if (new HashSet<>(keys.values()).size() != keys.size()) {
            errors.add("У всех ролей ключи должны быть разными");
        }
        if (!errors.isEmpty()) {
            saveErrors.setText(String.join("\n", errors));
            return;
        }

        storage.register(scannedProfile, keys);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
