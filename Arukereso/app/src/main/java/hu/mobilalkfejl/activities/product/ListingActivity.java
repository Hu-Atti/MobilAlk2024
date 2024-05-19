package hu.mobilalkfejl.activities.product;

import static androidx.core.app.PendingIntentCompat.getActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import hu.mobilalkfejl.R;
import hu.mobilalkfejl.activities.product.ProductCreatingActivity;
import hu.mobilalkfejl.activities.profile.MainActivity;
import hu.mobilalkfejl.activities.store.StoreListingActivity;
import hu.mobilalkfejl.model.Product;
import hu.mobilalkfejl.model.User;
import hu.mobilalkfejl.util.ProductItemAdapter;
import hu.mobilalkfejl.util.SpacesItemDecoration;
import hu.mobilalkfejl.util.UserManager;


public class ListingActivity extends AppCompatActivity {
    private static final String LOG_TAG = "ListingActivity";
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();
    RecyclerView mRecyclerView;
    ArrayList<Product> mProductList;
    ProductItemAdapter mAdapter;
    private int gridColumns;
    private FirebaseFirestore mFireStore;
    private CollectionReference mProducts;
    UserManager userManager = new UserManager();

    private int checkedItemCategory = 0;
    private int checkedItemSort = 0;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Termékek");

        userManager.checkLogin(LOG_TAG,this);

        mFireStore = FirebaseFirestore.getInstance();
        mProducts = mFireStore.collection("/Products");

        int spacingInPixels;
        mRecyclerView = findViewById(R.id.productRecyclerView);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridColumns = 1;
            spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
        } else {
            gridColumns = 3;
            spacingInPixels = 0;
        }
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridColumns));
        mProductList = new ArrayList<>();
        mAdapter = new ProductItemAdapter(this, mProductList);

        mRecyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        mRecyclerView.setAdapter(mAdapter);

        auth = FirebaseAuth.getInstance();

        queryData();
    }

    private void queryData() {
        Log.d(LOG_TAG, "Termékek olvasása az adatbázisból");
        mProductList.clear();
        mProducts.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(LOG_TAG, "Listen failed.", e);
                    return;
                }
                mProductList.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Product product = document.toObject(Product.class);
                    product.setId(document.getId());
                    mProductList.add(product);
                }
                if (mProductList.isEmpty()) {
                    Log.d(LOG_TAG, "Nincs az adatbázisban termék!");
                }
                mAdapter.sort(0);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String search) {
                Log.d(LOG_TAG, "Keresés: " + search);
                mAdapter.getFilter().filter(search);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            Log.d(LOG_TAG, "Kijelentkezés");
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        } else if (item.getItemId() == R.id.category) {
            Log.d(LOG_TAG, "Kategória választó");
            showCategoryDialog();
            return true;
        } else if (item.getItemId() == R.id.delete_profile) {
            Log.d(LOG_TAG, "Profil törlése");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Profil törlése");
            builder.setMessage("Biztosan törölni szeretné a profilját?");
            builder.setPositiveButton("Igen", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    userManager.getCurrentUser(new UserManager.OnUserCheckListener() {
                        @Override
                        public void onComplete(User user) {
                            if (!user.getOwnedStores().isEmpty()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ListingActivity.this);
                                builder.setTitle("Figyelmeztetés");
                                builder.setMessage("A profil tulajdonosa boltoknak. A törléséhez ezeket elősszőr törölni szükséges!");
                                builder.setPositiveButton("OK", null);
                                builder.show();
                            } else {
                                mFireStore.collection("Users").document(auth.getCurrentUser().getUid()).delete();
                                auth.getCurrentUser().delete();
                                finish();
                            }
                        }
                    });
                }
            });
            builder.setNegativeButton("Mégse", null);
            builder.show();
            return true;
        }else if (item.getItemId() == R.id.sort) {
            Log.d(LOG_TAG, "Rendezés választó");
            showSortDialog();
            return true;
        } else if (item.getItemId() == R.id.create_product) {
            Log.d(LOG_TAG, "Termék hozzáadása");
            if (userManager.isUserLoggedIn()) {
                userManager.isUserSeller(isSeller -> {
                    if (isSeller) {
                        Intent intent = new Intent(this, ProductCreatingActivity.class);
                        this.startActivity(intent);
                    } else {
                        Log.d(LOG_TAG, "Nem eladó");
                        Toast.makeText(this, "Csak eladók adhatnak hozzá terméket!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return true;

        } else if (item.getItemId() == R.id.stores) {
            Log.d(LOG_TAG, "Boltok kezelése");
            Intent intent = new Intent(this, StoreListingActivity.class);
            this.startActivity(intent);
            return true;
        }else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Válasszon kategóriát");

        final String[] categories = getResources().getStringArray(R.array.category_array);
        final String[] categoriesWithAll = new String[categories.length + 1];
        categoriesWithAll[0] = "Minden termék";
        System.arraycopy(categories, 0, categoriesWithAll, 1, categories.length);

        builder.setSingleChoiceItems(categoriesWithAll, checkedItemCategory, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkedItemCategory = which;
                if (which == 0) {
                    filterByCategory(null);
                } else {
                    filterByCategory(categoriesWithAll[which]);
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Mégse", null);
        builder.show();
    }

    private void showSortDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Válasszon rendezési módot");

        final String[] sortOptions = {"A-Z", "Z-A", "Ár szerint növekvő", "Ár szerint csökkenő"};

        builder.setSingleChoiceItems(sortOptions, checkedItemSort, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkedItemSort = which;
                mAdapter.sort(which);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Mégse", null);
        builder.show();
    }

    private void filterByCategory(String category) {
        mAdapter.setFilterCategory(category);
    }

    private void updateLowestPrice(String productId) {
        mFireStore.collection("Stock").whereEqualTo("productId", productId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int lowestPrice = Integer.MAX_VALUE;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            int price = document.getLong("price").intValue();
                            if (price < lowestPrice) {
                                lowestPrice = price;
                            }
                        }
                        mFireStore.collection("Products").document(productId)
                                .update("lowestPrice", lowestPrice);
                    }
                });
    }
}