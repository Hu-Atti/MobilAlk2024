package hu.mobilalkfejl.activities.store;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import hu.mobilalkfejl.R;
import hu.mobilalkfejl.model.Store;
import hu.mobilalkfejl.util.UserManager;

public class StoreCreatingActivity extends AppCompatActivity {
    private static final String LOG_TAG = "StoreCreatingActivity";
    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText storeNameEditText;
    private EditText storeAddressEditText;
    private ImageView storeLogoImageView;
    private EditText storeShippingCostEditText;
    private EditText storeRatingEditText;
    private Uri storeLogoUri;
    private FirebaseFirestore mFireStore;
    private CollectionReference mStores;
    private StorageReference mStorageRef;
    UserManager userManager = new UserManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_creating);

        if (userManager.isUserLoggedIn()) {
            Log.d(LOG_TAG, "Bejelentkezett felhasználó!");
            userManager.isUserSeller(isSeller -> {
                if (isSeller) {
                    Log.d(LOG_TAG, "A felhasználó eladó!");
                } else {
                    Log.d(LOG_TAG, "A felhasználó nem eladó!");
                    Toast.makeText(this, "Csak eladók adhatnak hozzá boltot!", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        } else {
            Log.d(LOG_TAG, "Kijelentkezett felhasználó!");
            finish();
        }

        storeNameEditText = findViewById(R.id.storeNameEditText);
        storeAddressEditText = findViewById(R.id.storeAddressEditText);
        storeLogoImageView = findViewById(R.id.storeLogoImageView);
        storeShippingCostEditText = findViewById(R.id.storeShippingCostEditText);
        storeRatingEditText = findViewById(R.id.storeRatingEditText);

        mFireStore = FirebaseFirestore.getInstance();
        mStores = mFireStore.collection("/Stores");
        mStorageRef = FirebaseStorage.getInstance().getReference("logos");

        Button mSaveButton = findViewById(R.id.storeSaveButton);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Mentés gomb megnyomva!");
                saveStore();
            }
        });

        Button mUploadButton = findViewById(R.id.storeLogoButton);
        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
    }

    private void openFileChooser() {
        Log.d(LOG_TAG, "Kép kiválasztás gomb megnyomva!");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            Log.d(LOG_TAG, "Kép kiválasztva!");
            storeLogoUri = data.getData();
            Picasso.get().load(storeLogoUri).into(storeLogoImageView);
        }
    }

    private void saveStore() {
        String name = storeNameEditText.getText().toString();
        String address = storeAddressEditText.getText().toString();
        int shippingCost = Integer.parseInt(storeShippingCostEditText.getText().toString());
        float rating = Float.parseFloat(storeRatingEditText.getText().toString());

        if (storeLogoUri != null) {
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(storeLogoUri));

            fileReference.putFile(storeLogoUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            Store store = new Store(name, address, uri.toString(), shippingCost, rating);
                            mStores.add(store).addOnSuccessListener(documentReference -> {
                                store.setId(documentReference.getId());
                                FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                                if(mUser != null) {
                                    DocumentReference userRef = FirebaseFirestore.getInstance().collection("Users").document(mUser.getUid());
                                    userRef.update("ownedStores", FieldValue.arrayUnion(store._getId()));
                                    Log.d(LOG_TAG, "Bolt hozzárendelve a felhasználóhoz!");
                                }
                                Log.d(LOG_TAG, "Bolt mentve!");
                                Toast.makeText(StoreCreatingActivity.this, "Bolt mentve", Toast.LENGTH_SHORT).show();
                                finish();
                            }).addOnFailureListener(e -> {
                                Log.w(LOG_TAG, "Hiba a bolt mentésekor: ", e);
                                Toast.makeText(StoreCreatingActivity.this, "Hiba a bolt mentése közben", Toast.LENGTH_SHORT).show();
                            });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(StoreCreatingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Nincs kép kiválasztva", Toast.LENGTH_SHORT).show();
        }
    }

    public void cancel(View view) {
        finish();
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}


