package hu.mobilalkfejl.util;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import hu.mobilalkfejl.R;
import hu.mobilalkfejl.model.Stock;
import hu.mobilalkfejl.model.Store;
import hu.mobilalkfejl.model.User;

public class StoreListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  implements Filterable {
    private static final String LOG_TAG = "StoreListAdapter";
    private ArrayList<Store> mStoreData;
    private final ArrayList<Store> mStoreDataAll;
    private final Context context;
    private int lastPos = -1;

    public StoreListAdapter(Context context, ArrayList<Store> storeData) {
        this.mStoreData = storeData;
        this.mStoreDataAll = storeData;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_store, parent, false));
    }



    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Store currentStore = mStoreData.get(position);

        if (holder instanceof ViewHolder) {
            ((ViewHolder) holder).bindTo(currentStore);
        }

        if (holder.getAdapterPosition() > lastPos) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_up);
            holder.itemView.startAnimation(animation);
            lastPos = holder.getAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return mStoreData.size();
    }

    @Override
    public Filter getFilter() {
        return storeFilter;
    }

    private final Filter storeFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<Store> filteredList = new ArrayList<>();
            FilterResults results = new FilterResults();

            if (charSequence == null || charSequence.length() == 0) {
                results.count = mStoreDataAll.size();
                results.values = mStoreDataAll;
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for (Store store : mStoreDataAll) {
                    if (store.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(store);
                    }
                }

                results.count = filteredList.size();
                results.values = filteredList;

            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults results) {
            mStoreData = (ArrayList<Store>) results.values;
            notifyDataSetChanged();
        }
    };


    class ViewHolder extends RecyclerView.ViewHolder{
        private static final String LOG_TAG = "StoreListAdapter.ViewHolder";
        private final TextView storeName;
        private final TextView shippingCost;
        private final TextView storeAddress;
        private final ImageView logo;
        private final RatingBar thrustMeter;

        private UserManager userManager = new UserManager();

        private Store storeItem;

        private FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();
        private CollectionReference mStocks = mFireStore.collection("/Stock");

        private CollectionReference mStores = mFireStore.collection("/Stores");

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            storeName = itemView.findViewById(R.id.store_name);
            shippingCost = itemView.findViewById(R.id.shipping_cost);
            storeAddress = itemView.findViewById(R.id.store_address);
            logo = itemView.findViewById(R.id.store_logo);
            thrustMeter = itemView.findViewById(R.id.store_rating);
        }

        public void deleteStore(String storeId) {
            mStocks.whereEqualTo("storeId", storeId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().isEmpty()) {
                        mStores.document(storeId).delete().addOnSuccessListener(aVoid -> {
                            Log.d(LOG_TAG, "Bolt sikeresen törölve!");
                            Toast.makeText(context, "A bolt sikeresen törölve!", Toast.LENGTH_SHORT).show();
                            userManager.getCurrentUser(new UserManager.OnUserCheckListener() {
                                @Override
                                public void onComplete(User user) {
                                    user.getOwnedStores().remove(storeId);
                                    mFireStore.collection("Users").document(userManager.getUserId()).set(user);
                                }
                            });

                        }).addOnFailureListener(e -> {
                            Log.w(LOG_TAG, "Hiba a bolt törlése közben", e);
                            Toast.makeText(context, "Hiba a bolt törlése közben", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Log.d(LOG_TAG, "A bolt törléséhez törölje az összes készletet!");
                        Toast.makeText(context, "A bolt törléséhez törölje az összes készletet!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.w(LOG_TAG, "Hiba a Stock példányok lekérdezése közben", task.getException());
                    Toast.makeText(context, "Hiba a Stock példányok lekérdezése közben", Toast.LENGTH_SHORT).show();
                }
            });
        }




        public void bindTo(Store currentStore) {
            this.storeItem = currentStore;

            storeName.setText(currentStore.getName());
            shippingCost.setText("Szállítási díj: " + currentStore.getShippingCost() + " Ft");
            storeAddress.setText(currentStore.getAddress());
            thrustMeter.setRating(currentStore.getRating());

            itemView.findViewById(R.id.store_delete_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(LOG_TAG, "onClick: Törlés!");
                    deleteStore(currentStore._getId());
                }
            });

            Glide.with(context)
                    .load(currentStore.getLogo())
                    .apply(new RequestOptions().placeholder(R.drawable.placeholder).error(R.drawable.imagenotfound))
                    .into(logo);

            userManager.getCurrentUser(new UserManager.OnUserCheckListener() {
                @Override
                public void onComplete(User user) {
                    if (user.getOwnedStores().contains(currentStore._getId())) {
                        itemView.findViewById(R.id.store_manage).setVisibility(View.VISIBLE);
                    } else {
                        itemView.findViewById(R.id.store_manage).setVisibility(View.GONE);
                    }
                }
            });
        }


    }
}




