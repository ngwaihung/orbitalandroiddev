package com.example.android.firebaseauthdemo;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TransactionHistoryFragment extends Fragment {

    public static TransactionHistoryFragment newInstance() {
        TransactionHistoryFragment fragment = new TransactionHistoryFragment();
        return fragment;
    }

    DatabaseReference databaseProducts;
    ListView listViewBuyer;
    ListView listViewCourier;
    List<Product> productList;
    List<Product> productList2;
    String userEmail;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseUsers;
    View buttonView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_transaction_history, container, false);

        databaseProducts = FirebaseDatabase.getInstance().getReference("products");
        productList = new ArrayList<>();
        productList2 = new ArrayList<>();

        Bundle extras = getActivity().getIntent().getExtras();
        if(extras != null){
            userEmail = extras.getString("email");
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        listViewBuyer = (ListView) getView().findViewById(R.id.listViewTransactBuyer);
        listViewCourier = (ListView) getView().findViewById(R.id.listViewTransactCourier);
    }

    @Override
    public void onStart() {
        super.onStart();
        databaseProducts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                productList.clear();

                for(DataSnapshot productSnapshot : dataSnapshot.getChildren()){
                    Product product = productSnapshot.getValue(Product.class);
                    String buyerEmail = product.getProductBuyer();
                    if(product.getStatus().equals("Completed") && buyerEmail.equals(userEmail)){
                        productList.add(product);
                    }
                }

                ProductListTransactHistory adapter = new ProductListTransactHistory(getActivity(), productList);
                listViewBuyer.setAdapter(adapter);

                productList2.clear();

                for(DataSnapshot productSnapshot : dataSnapshot.getChildren()){
                    Product product = productSnapshot.getValue(Product.class);
                    String courierEmail = product.getProductCourier();
                    if(product.getStatus().equals("Completed") && courierEmail.equals(userEmail)){
                        productList2.add(product);
                    }
                }

                ProductListTransactHistory adapter2 = new ProductListTransactHistory(getActivity(), productList2);
                listViewCourier.setAdapter(adapter2);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
