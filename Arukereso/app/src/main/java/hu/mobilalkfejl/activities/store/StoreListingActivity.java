package hu.mobilalkfejl.activities.store;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import hu.mobilalkfejl.R;
import hu.mobilalkfejl.activities.store.StoreCreatingActivity;
import hu.mobilalkfejl.model.Store;
import hu.mobilalkfejl.util.SpacesItemDecoration;
import hu.mobilalkfejl.util.StoreListAdapter;
import hu.mobilalkfejl.util.UserManager;

public class StoreListingActivity extends AppCompatActivity {
    private static final String LOG_TAG = "StoreListingActivity";
    RecyclerView mRecyclerView;
    ArrayList<Store> mStoresList;

    StoreListAdapter mAdapter;

    private int gridColumns = 1;

    private FirebaseFirestore mFireStore;
    private CollectionReference mStores;
    private CollectionReference mStocks;
    UserManager userManager = new UserManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_listing);

        Toolbar toolbar = findViewById(R.id.storeToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Boltok");

        userManager.checkLogin(LOG_TAG,this);

        mFireStore = FirebaseFirestore.getInstance();
        mStores = mFireStore.collection("/Stores");

        mStocks = mFireStore.collection("/Stocks");

        mRecyclerView = findViewById(R.id.storeRecyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridColumns));
        mStoresList = new ArrayList<>();
        mAdapter = new StoreListAdapter(this, mStoresList);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        mRecyclerView.setAdapter(mAdapter);

        queryData();
    }

    private void queryData() {
        Log.d(LOG_TAG, "Boltok olvasása az adatbázisból");
        mStoresList.clear();
        mStores.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(LOG_TAG, "Listen failed.", e);
                    return;
                }
                mStoresList.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Store store = document.toObject(Store.class);
                    store.setId(document.getId());
                    mStoresList.add(store);
                }
                if (mStoresList.isEmpty()) {
                    Log.d(LOG_TAG, "Nincs az adatbázisban bolt!");
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.storemenu, menu);
        MenuItem menuItem = menu.findItem(R.id.store_search_bar);
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
        if (item.getItemId() == R.id.openStore) {
            Log.d(LOG_TAG, "Bolt nyitása");
            if (userManager.isUserLoggedIn()) {
                userManager.isUserSeller(isSeller -> {
                    if (isSeller) {
                        Intent intent = new Intent(this, StoreCreatingActivity.class);
                        this.startActivity(intent);
                    } else {
                        Log.d(LOG_TAG, "Nem eladó");
                        Toast.makeText(this, "Csak eladók adhatnak hozzá boltot!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return true;

        }else {
            return super.onOptionsItemSelected(item);
        }
    }


}