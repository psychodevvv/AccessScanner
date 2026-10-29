package kz.itk.access;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    public static final String EXTRA_ROLE = "role";
    private static final String NO_ACCESS = "*** нет доступа ***";

    private Storage storage;
    private TextView messagesText;
    private EditText messageInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String roleName = getIntent().getStringExtra(EXTRA_ROLE);
        if (roleName == null) {
            finish();
            return;
        }
        Role role = Role.valueOf(roleName);

        setContentView(R.layout.activity_profile);
        storage = new Storage(this);

        TextView roleText = findViewById(R.id.roleText);
        TextView dataText = findViewById(R.id.dataText);
        messagesText = findViewById(R.id.messagesText);
        messageInput = findViewById(R.id.messageInput);
        Button sendButton = findViewById(R.id.sendButton);
        Button logoutButton = findViewById(R.id.logoutButton);

        roleText.setText("Вы вошли как: " + role.getTitle());

        StringBuilder data = new StringBuilder();
        for (Field field : Field.values()) {
            if (field == Field.MESSAGES) {
                continue;
            }
            String value = role.canSee(field) ? storage.getField(field) : NO_ACCESS;
            data.append(field.getTitle()).append(": ").append(value).append("\n");
        }
        dataText.setText(data.toString().trim());

        if (role.canSee(Field.MESSAGES)) {
            showMessages();
            sendButton.setOnClickListener(v -> sendMessage());
        } else {
            messagesText.setText(Field.MESSAGES.getTitle() + ": " + NO_ACCESS);
            messageInput.setVisibility(View.GONE);
            sendButton.setVisibility(View.GONE);
        }

        logoutButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) {
            messageInput.setError("Пустое сообщение не отправляется");
            return;
        }
        storage.addMessage(text);
        messageInput.setText("");
        showMessages();
    }

    private void showMessages() {
        List<String> messages = storage.getMessages();
        StringBuilder text = new StringBuilder(Field.MESSAGES.getTitle())
                .append(": ").append(messages.size()).append(" шт.");
        for (int i = 0; i < messages.size(); i++) {
            text.append("\n").append(i + 1).append(". ").append(messages.get(i));
        }
        messagesText.setText(text.toString());
    }
}
