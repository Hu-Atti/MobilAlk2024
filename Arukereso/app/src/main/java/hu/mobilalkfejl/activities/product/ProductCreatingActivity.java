package hu.mobilalkfejl.activities.product;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import hu.mobilalkfejl.R;
import hu.mobilalkfejl.model.Product;
import hu.mobilalkfejl.model.Store;
import hu.mobilalkfejl.util.UserManager;

public class ProductCreatingActivity extends AppCompatActivity {
    private static final String LOG_TAG = "ProductCreatingActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText productNameEditText;
    private Spinner productCategorySpinner;
    private ImageView productImageView;
    private EditText productDescriptionEditText;
    private EditText productRatingEditText;
    private EditText productPriceEditText;
    private Uri productImageUri;
    private FirebaseFirestore mFireStore;
    private CollectionReference mProducts;
    private StorageReference mStorageRef;
    UserManager userManager = new UserManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_creating);

        if (userManager.isUserLoggedIn()) {
            Log.d(LOG_TAG, "Bejelentkezett felhasználó!");
            userManager.isUserSeller(isSeller -> {
                if (isSeller) {
                    Log.d(LOG_TAG, "A felhasználó eladó!");
                } else {
                    Log.d(LOG_TAG, "A felhasználó nem eladó!");
                    Toast.makeText(this, "Csak eladók adhatnak hozzá terméket!", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        } else {
            Log.d(LOG_TAG, "Kijelentkezett felhasználó!");
            finish();
        }

        productNameEditText = findViewById(R.id.product_name_editText);
        productCategorySpinner = findViewById(R.id.product_category_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        productCategorySpinner.setAdapter(adapter);
        productImageView = findViewById(R.id.product_imageView);
        productDescriptionEditText = findViewById(R.id.product_description_editText);
        productRatingEditText = findViewById(R.id.product_rating_editText);


        mFireStore = FirebaseFirestore.getInstance();
        mProducts = mFireStore.collection("/Products");
        mStorageRef = FirebaseStorage.getInstance().getReference("images");

        Button mSaveButton = findViewById(R.id.product_create_button);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Mentés gomb megnyomva!");
                saveProduct();
            }
        });

        Button mUploadButton = findViewById(R.id.product_image_button);
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
            productImageUri = data.getData();
            Picasso.get().load(productImageUri).into(productImageView);
        }
    }

    private void saveProduct() {
        String name = productNameEditText.getText().toString();
        String category = productCategorySpinner.getSelectedItem().toString();
        String description = productDescriptionEditText.getText().toString();
        float rating = Float.parseFloat(productRatingEditText.getText().toString());

        if (productImageUri != null) {
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(productImageUri));

            fileReference.putFile(productImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            Product product = new Product(name, category, description, rating, uri.toString());
                            mProducts.add(product).addOnSuccessListener(documentReference -> {
                                product.setId(documentReference.getId());
                                Log.d(LOG_TAG, "Termék mentve!");
                                Toast.makeText(ProductCreatingActivity.this, "Termék mentve", Toast.LENGTH_SHORT).show();
                                finish();
                            }).addOnFailureListener(e -> {
                                Log.w(LOG_TAG, "Hiba a termék mentésekor: ", e);
                                Toast.makeText(ProductCreatingActivity.this, "Hiba a termék mentése közben", Toast.LENGTH_SHORT).show();
                            });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProductCreatingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
