package hu.mobilalkfejl.util;

import android.app.Activity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import hu.mobilalkfejl.model.User;

public class UserManager {
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public UserManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }
    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }
    public void isUserSeller(OnSellerCheckListener listener) {
        FirebaseUser mUser = auth.getCurrentUser();
        if(mUser != null) {
            db.collection("Users").document(mUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                boolean seller = Boolean.TRUE.equals(document.getBoolean("seller"));
                                listener.onComplete(seller);
                            }
                        }
                    });
        }
    }
    public void checkLogin(String LOG_TAG, Activity activity) {
        if (isUserLoggedIn()) {
            Log.d(LOG_TAG, "Bejelentkezett felhasználó!");
        } else {
            Log.d(LOG_TAG, "Kijelentkezett felhasználó!");
            activity.finish();
        }
    }

    public String getUserId() {
        FirebaseUser mUser = auth.getCurrentUser();
        if (mUser != null) {
            return mUser.getUid();
        } else {
            return null;
        }
    }


    public interface OnSellerCheckListener {
        void onComplete(boolean isSeller);
    }

    public void getCurrentUser(OnUserCheckListener listener) {
        FirebaseUser mUser = auth.getCurrentUser();
        if(mUser != null) {
            db.collection("Users").document(mUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                User user = document.toObject(User.class);
                                listener.onComplete(user);
                            }
                        }
                    });
        }
    }
    public interface OnUserCheckListener {
        void onComplete(User user);
    }

}

