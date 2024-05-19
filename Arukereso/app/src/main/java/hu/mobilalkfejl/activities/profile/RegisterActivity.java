package hu.mobilalkfejl.activities.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


import hu.mobilalkfejl.R;
import hu.mobilalkfejl.model.User;

public class RegisterActivity extends AppCompatActivity {
    private static final String LOG_TAG = RegisterActivity.class.getName();
    private static final String PREF_KEY = RegisterActivity.class.getPackage().toString();
    private FirebaseAuth auth;
    private SharedPreferences preferences;
    private FirebaseFirestore db;
    Button registerButton;
    EditText usernameEditText;
    EditText emailEditText;
    EditText phoneEditText;
    EditText passwordEditText;
    EditText confirmPasswordEditText;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        registerButton = findViewById(R.id.registerButton);
        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        spinner = findViewById(R.id.isSellerSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.roles_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
    }

    public void register(View view) {
        String username = usernameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();
        boolean seller = spinner.getSelectedItem().toString().equals("Eladó");

        if (!password.equals(confirmPassword)) {
            Log.e(LOG_TAG, "A két jelszó nem egyezik!");
            Toast.makeText(this, "A két jelszó nem egyezik!", Toast.LENGTH_LONG).show();
            return;
        }

        if (email.isEmpty() || password.isEmpty()) {
            Log.e(LOG_TAG, "Az email vagy a jelszó üres!");
            Toast.makeText(this, "Az email vagy a jelszó üres!", Toast.LENGTH_LONG).show();
        } else {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "Sikeres regisztráció!");
                    Toast.makeText(RegisterActivity.this, "Sikeres regisztráció!", Toast.LENGTH_LONG).show();
                    User user = new User(username, email, password, phone, seller);

                    db.collection("Users").document(auth.getCurrentUser().getUid()).set(user)
                            .addOnSuccessListener(aVoid -> Log.d(LOG_TAG, "Felhasználó létrehozva!"))
                            .addOnFailureListener(e -> Log.e(LOG_TAG, "Hiba a felhasználó létrehozása közben: ", e));
                    finish();
                    login();
                } else {
                    Log.d(LOG_TAG, "Sikertelen regisztráció!");
                    Toast.makeText(RegisterActivity.this, "Sikertelen regisztráció: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }


    public void cancel(View view) {
        finish();
    }

    private void login() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("username", usernameEditText.getText().toString());
        editor.putString("email", emailEditText.getText().toString());
        editor.putString("phone", phoneEditText.getText().toString());
        editor.putString("password", passwordEditText.getText().toString());
        editor.putString("confirmpassword", confirmPasswordEditText.getText().toString());
        editor.apply();
        Log.i(LOG_TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG, "onRestart");
    }



}