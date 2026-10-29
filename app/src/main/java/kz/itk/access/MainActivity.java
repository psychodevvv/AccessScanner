package kz.itk.access;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCK_MILLIS = 30_000;

    private Storage storage;
    private EditText keyInput;
    private Button loginButton;
    private TextView logText;
    private BiometricPrompt biometricPrompt;
    private int attemptsLeft = MAX_ATTEMPTS;
    private Role pendingRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storage = new Storage(this);

        if (!storage.isRegistered()) {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        keyInput = findViewById(R.id.keyInput);
        loginButton = findViewById(R.id.loginButton);
        logText = findViewById(R.id.logText);

        biometricPrompt = createBiometricPrompt();
        loginButton.setOnClickListener(v -> onLoginClicked());
    }

    private void onLoginClicked() {
        String key = keyInput.getText().toString();
        keyInput.setText("");
        logText.setText("");

        String error = Validator.checkKey(key);
        if (error != null) {
            log("[Валидация] " + error);
            registerFailure();
            return;
        }
        log("[Валидация] формат ключа верный");

        Role role = storage.findRoleByKey(key);
        if (role == null) {
            log("[Верификация] такого ключа нет");
            registerFailure();
            return;
        }
        log("[Верификация] ключ принят, роль: " + role.getTitle());
        attemptsLeft = MAX_ATTEMPTS;

        if (role.needsBiometrics()) {
            startBiometrics(role);
        } else {
            openProfile(role);
        }
    }

    private void registerFailure() {
        attemptsLeft--;
        if (attemptsLeft > 0) {
            log("Осталось попыток: " + attemptsLeft);
            return;
        }

        log("Слишком много ошибок, вход заблокирован на 30 секунд");
        loginButton.setEnabled(false);
        new CountDownTimer(LOCK_MILLIS, 1000) {
            @Override
            public void onTick(long millisLeft) {
                loginButton.setText("Блокировка: " + (millisLeft / 1000 + 1) + " с");
            }

            @Override
            public void onFinish() {
                attemptsLeft = MAX_ATTEMPTS;
                loginButton.setEnabled(true);
                loginButton.setText("Войти");
            }
        }.start();
    }

    private void startBiometrics(Role role) {
        int status = BiometricManager.from(this).canAuthenticate(BIOMETRIC_WEAK);
        if (status != BiometricManager.BIOMETRIC_SUCCESS) {
            log("[Биометрия] на телефоне не настроен отпечаток или лицо — вход запрещён");
            return;
        }

        pendingRole = role;
        log("[Биометрия] приложите палец или посмотрите в камеру");

        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Face ID / Touch ID")
                .setSubtitle("Подтвердите, что вы владелец")
                .setNegativeButtonText("Отмена")
                .setAllowedAuthenticators(BIOMETRIC_WEAK)
                .build();
        biometricPrompt.authenticate(info);
    }

    private BiometricPrompt createBiometricPrompt() {
        return new BiometricPrompt(this, ContextCompat.getMainExecutor(this),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        log("[Биометрия] личность подтверждена");
                        if (pendingRole != null) {
                            openProfile(pendingRole);
                            pendingRole = null;
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        log("[Биометрия] не распознано, попробуйте ещё раз");
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        log("[Биометрия] отказ: " + errString);
                        pendingRole = null;
                    }
                });
    }

    private void openProfile(Role role) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.EXTRA_ROLE, role.name());
        startActivity(intent);
    }

    private void log(String line) {
        logText.append(line + "\n");
    }
}
