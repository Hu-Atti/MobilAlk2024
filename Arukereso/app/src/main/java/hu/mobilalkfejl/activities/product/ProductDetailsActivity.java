package hu.mobilalkfejl.activities.product;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.mobilalkfejl.R;
import hu.mobilalkfejl.model.Product;
import hu.mobilalkfejl.model.Stock;
import hu.mobilalkfejl.model.Store;
import hu.mobilalkfejl.model.User;
import hu.mobilalkfejl.util.StockAdapter;
import hu.mobilalkfejl.util.UserManager;

public class ProductDetailsActivity extends AppCompatActivity {
    private static final String LOG_TAG = "ProductDetailsActivity";
    private FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();
    private List<Stock> stockList = new ArrayList<>();
    Product product;
    private StockAdapter stockAdapter;
    Map<String, String> storeMap = new HashMap<>();
    List<String> storeNames = new ArrayList<>();
    Spinner spinner;
    Button addButton;
    String productId;
    DocumentReference productRef;
    UserManager userManager;
    TextView productPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        userManager = new UserManager();

        Toolbar toolbar = findViewById(R.id.product_details_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Részletek");

        if (userManager.isUserLoggedIn()) {
            Log.d(LOG_TAG, "Bejelentkezett felhasználó!");
            userManager.isUserSeller(isSeller -> {
                if (isSeller) {
                    Log.d(LOG_TAG, "A felhasználó eladó!");
                } else {
                    Log.d(LOG_TAG, "A felhasználó nem eladó!");
                    LinearLayout addProduct = findViewById(R.id.add_product);
                    addProduct.setVisibility(View.GONE);
                }
            });
        } else {
            Log.d(LOG_TAG, "Kijelentkezett felhasználó!");
            finish();
        }

        productId = getIntent().getStringExtra("PRODUCT_ID");

        productRef = mFireStore.collection("Products").document(productId);
        productRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(LOG_TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists() && !isDestroyed()) {
                    product = snapshot.toObject(Product.class);

                    TextView productName = findViewById(R.id.product_name);
                    TextView productCategory = findViewById(R.id.product_category);
                    productPrice = findViewById(R.id.product_price);
                    ImageView productImage = findViewById(R.id.product_image);
                    RatingBar productRating = findViewById(R.id.product_rating);
                    TextView productDescription = findViewById(R.id.product_description);

                    productName.setText(product.getName());
                    productCategory.setText(product.getCategory());
                    if (product.getLowestPrice() != Integer.MAX_VALUE) {
                        productPrice.setText(String.valueOf(product.getLowestPrice()) + " Ft");
                    }
                    Glide.with(ProductDetailsActivity.this)
                            .load(product.getImage())
                            .apply(new RequestOptions().placeholder(R.drawable.placeholder).error(R.drawable.imagenotfound))
                            .into(productImage);
                    productRating.setRating(product.getRating());
                    productDescription.setText(product.getDescription());

                    querySpinnerData();
                } else {
                    Log.d(LOG_TAG, "Current data: null");
                }
            }
        });

        RecyclerView recyclerView = findViewById(R.id.stores_recycler_view);
        stockAdapter = new StockAdapter(stockList);
        recyclerView.setAdapter(stockAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        queryStockData();
        spinner = findViewById(R.id.store_spinner);
        addButton = findViewById(R.id.add_button);

        addButton.setOnClickListener(v -> {
            String selectedStoreName = (String) spinner.getSelectedItem();
            EditText priceEditText = findViewById(R.id.product_price_editText);
            if (selectedStoreName.equals("Válasszon boltot...")) {
                Toast.makeText(this, "Válasszon egy boltot!", Toast.LENGTH_SHORT).show();
            } else if (priceEditText.getText().toString().isEmpty()) {
                Toast.makeText(this, "Kérjük, adja meg az árat!", Toast.LENGTH_SHORT).show();
            } else {
                String storeId = storeMap.get(selectedStoreName);
                int price = Integer.parseInt(priceEditText.getText().toString());
                addProductToStore(price, storeId);
            }
        });
    }
    private void addProductToStore(int price, String storeId) {
        product.addStoreId(storeId);
        Stock newStock = new Stock(productId, storeId, price);

        mFireStore.collection("Stock")
                .whereEqualTo("productId", productId)
                .whereEqualTo("storeId", storeId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // If a Stock object exists, update the price
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (price == 0) {
                                    deleteProductFromStore(document.getId());
                                } else {
                                    mFireStore.collection("Stock").document(document.getId()).update("price", price);
                                }
                            }
                        } else if (price != 0) {
                            mFireStore.collection("Stock").add(newStock);
                        }
                    } else {
                        Log.d(LOG_TAG, "Hiba lépett fel: ", task.getException());
                    }
                });

        mFireStore.collection("Products").document(productId).set(product)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Termék és készlet frissítve!", Toast.LENGTH_SHORT).show();
                    productRef.get().addOnSuccessListener(documentSnapshot -> {
                        product = documentSnapshot.toObject(Product.class);
                        checkLowestPrice(productId);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba a termék frissítésekor!", Toast.LENGTH_SHORT).show();
                });
    }
    private void deleteProductFromStore(String documentId) {
        mFireStore.collection("Stock").document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(LOG_TAG, "Termék törölve a raktárból!");
                    Toast.makeText(this, "Termék törölve a raktárból!", Toast.LENGTH_SHORT).show();
                    checkLowestPrice(productId);
                })
                .addOnFailureListener(e -> {
                    Log.w(LOG_TAG, "Hiba a termék törlésekor!", e);
                    Toast.makeText(this, "Hiba a termék törlésekor!", Toast.LENGTH_SHORT).show();
                });
    }
    private void querySpinnerData() {
        userManager.getCurrentUser(new UserManager.OnUserCheckListener() {
            @Override
            public void onComplete(User user) {
                storeNames.clear();
                storeNames.add("Válasszon boltot...");
                for (String storeId : user.getOwnedStores()) {
                    mFireStore.collection("Stores").document(storeId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                Store store = documentSnapshot.toObject(Store.class);
                                if (store.getName() != null ) {
                                    storeNames.add(store.getName());
                                    storeMap.put(store.getName(), documentSnapshot.getId());
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(ProductDetailsActivity.this, android.R.layout.simple_spinner_item, storeNames){
                                    @Override
                                    public boolean isEnabled(int position){
                                        if(position == 0) {
                                            return false;
                                        }
                                        else {
                                            return true;
                                        }
                                    }
                                };
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setAdapter(adapter);
                            });
                }
            }
        });
    }



    private void queryStockData() {
        Log.d("LOG_TAG", "Raktárak olvasása az adatbázisból");
        stockList.clear();
        mFireStore.collection("Stock")
                .whereEqualTo("productId", productId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("LOG_TAG", "Listen failed.", e);
                            return;
                        }
                        stockList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Stock stock = document.toObject(Stock.class);
                            stockList.add(stock);
                        }
                        if (stockList.isEmpty()) {
                            Log.d("LOG_TAG", "Nincs az adatbázisban raktár!");
                        }
                        stockAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void checkLowestPrice(String productId) {
        mFireStore.collection("Stock")
                .whereEqualTo("productId", productId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int lowestPrice = Integer.MAX_VALUE;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Stock stock = document.toObject(Stock.class);
                            if (stock.getPrice() < lowestPrice) {
                                lowestPrice = stock.getPrice();
                            }
                        }
                        if (product.getLowestPrice() == Integer.MAX_VALUE) {
                            productPrice.setText("Termék nem elérhető");
                        }
                        product.setLowestPrice(lowestPrice);
                        mFireStore.collection("Products").document(productId).set(product);
                    } else {
                        Log.d(LOG_TAG, "Hiba lépett fel: ", task.getException());
                    }
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_product, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.edit) {
            Log.d(LOG_TAG, "Szerkeztés nyitása");
            if (userManager.isUserLoggedIn()) {
                userManager.isUserSeller(isSeller -> {
                    if (isSeller) {
                        Intent intent = new Intent(this, ProductEditingActivity.class);
                        intent.putExtra("PRODUCT_ID", productId);
                        this.startActivity(intent);
                    } else {
                        Log.d(LOG_TAG, "Nem eladó");
                        Toast.makeText(this, "Csak eladók szerkeszthetnek terméket!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return true;

        } else if (item.getItemId() == R.id.delete) {
            Log.d(LOG_TAG, "Törlés");
            if (userManager.isUserLoggedIn()) {
                userManager.isUserSeller(isSeller -> {
                    if (isSeller) {
                        mFireStore.collection("Stock")
                                .whereEqualTo("productId", productId)
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        if (task.getResult().isEmpty()) {
                                            mFireStore.collection("Products").document(productId)
                                                    .delete()
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d(LOG_TAG, "Termék törölve az adatbázisból!");
                                                        Toast.makeText(this, "Termék törölve az adatbázisból!", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.w(LOG_TAG, "Hiba a termék törlésekor!", e);
                                                        Toast.makeText(this, "Hiba a termék törlésekor!", Toast.LENGTH_SHORT).show();
                                                    });
                                        } else {
                                            Toast.makeText(this, "Kizárólag nem elérhető termékeket lehet törölni", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Log.d(LOG_TAG, "Hiba lépett fel: ", task.getException());
                                    }
                                });
                    } else {
                        Log.d(LOG_TAG, "Nem eladó");
                        Toast.makeText(this, "Csak eladók törölhetnek terméket!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return true;
        }

        else {
            return super.onOptionsItemSelected(item);
        }
    }
}
