package hu.mobilalkfejl.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import hu.mobilalkfejl.R;
import hu.mobilalkfejl.model.Product;
import hu.mobilalkfejl.model.Stock;
import hu.mobilalkfejl.model.Store;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.ViewHolder> {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Stock> stockList;

    public StockAdapter(List<Stock> stockList) {
        this.stockList = stockList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_basic_store, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Stock stock = stockList.get(position);
        db.collection("Products").document(stock.getProductId()).get().addOnSuccessListener(documentSnapshot -> {
            Product product = documentSnapshot.toObject(Product.class);
            holder.productPrice.setText(String.valueOf(stock.getPrice()) + " Ft");
        });

        db.collection("Stores").document(stock.getStoreId()).get().addOnSuccessListener(documentSnapshot -> {
            Store store = documentSnapshot.toObject(Store.class);
            if (store != null) {
                holder.storeName.setText(store.getName());
                holder.storeShippingCost.setText("Szállítás: " + String.valueOf(store.getShippingCost()) + " Ft");
                holder.storeRating.setRating(store.getRating());
                Glide.with(holder.storeLogo.getContext())
                        .load(store.getLogo())
                        .apply(new RequestOptions().placeholder(R.drawable.placeholder).error(R.drawable.imagenotfound))
                        .into(holder.storeLogo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView storeLogo;
        TextView storeName;
        RatingBar storeRating;
        TextView productPrice;
        TextView storeShippingCost;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            storeLogo = itemView.findViewById(R.id.store_logo);
            storeName = itemView.findViewById(R.id.store_name);
            storeRating = itemView.findViewById(R.id.store_rating);
            productPrice = itemView.findViewById(R.id.product_price);
            storeShippingCost = itemView.findViewById(R.id.store_shipping_cost);
        }
    }
}
