package com.example.android.firebaseauthdemo;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AcceptedCourierFragment extends Fragment {

    public static AcceptedCourierFragment newInstance() {
        AcceptedCourierFragment fragment = new AcceptedCourierFragment();
        return fragment;
    }

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    DatabaseReference databaseUsers;
    DatabaseReference databaseProductsAccepted;
    DatabaseReference databaseProductsAll;
    ListView listViewProductsAccepted;
    List<Product> productListAccepted;
    ListView listViewProductsAll;
    List<Product> productListAll;
    ProductList adapterAll;
    String userEmail;
    String listFilter;
    String userCountry;
    Button btnAccepted;
    Button btnSuggested;
    Button btnAll;
    private Button suggestedOrders;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_courier_accepted, container, false);
        //To remove once filter tabs are finalized
        Button suggestedOrders = (Button) rootView.findViewById(R.id.suggestedButton);
        suggestedOrders.setOnClickListener(suggestedListener);

        databaseProductsAccepted = FirebaseDatabase.getInstance().getReference("products");
        databaseProductsAll = FirebaseDatabase.getInstance().getReference("products");
        productListAccepted = new ArrayList<>();
        productListAll = new ArrayList<>();
        listFilter = "suggested";

        //Filter Buttons
        btnAccepted = (Button) rootView.findViewById(R.id.btnAccepted);
        btnSuggested = (Button) rootView.findViewById(R.id.btnSuggested);
        btnAll = (Button) rootView.findViewById(R.id.btnAll);
        btnAccepted.setOnClickListener(btnAcceptedListener);
        btnSuggested.setOnClickListener(btnSuggestedListener);
        btnAll.setOnClickListener(btnAllListener);
        btnAccepted.setTypeface(Typeface.DEFAULT_BOLD);
        btnAccepted.setTextSize(16);

        //User Info
        firebaseAuth = FirebaseAuth.getInstance();
        databaseUsers = FirebaseDatabase.getInstance().getReference("users");
        FirebaseUser user = firebaseAuth.getCurrentUser();
        databaseUsers.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userCountry = dataSnapshot.child("buyerCountry").getValue(String.class);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        //Grabs email string from previous activity
        Bundle extras = getActivity().getIntent().getExtras();
        if(extras != null){
            userEmail = extras.getString("email");
        }
        return rootView;
    }

    //Accepted Filter Button
    private View.OnClickListener btnAcceptedListener = new View.OnClickListener() {
        public void onClick(View v) {
            listViewProductsAccepted.setVisibility(View.VISIBLE);
            listViewProductsAll.setVisibility(View.INVISIBLE);
            clearButtonStyle();
            btnAccepted.setTypeface(Typeface.DEFAULT_BOLD);
            btnAccepted.setTextSize(16);
        }
    };

    //Suggested Filter Button (Under Construction)
    private View.OnClickListener btnSuggestedListener = new View.OnClickListener() {
        public void onClick(View v) {
            listFilter = "suggested";
            listViewProductsAccepted.setVisibility(View.INVISIBLE);
            listViewProductsAll.setVisibility(View.VISIBLE);
            onStart();
            adapterAll.notifyDataSetChanged();
            clearButtonStyle();
            btnSuggested.setTypeface(Typeface.DEFAULT_BOLD);
            btnSuggested.setTextSize(16);
        }
    };

    //Display All Button
    private View.OnClickListener btnAllListener = new View.OnClickListener() {
        public void onClick(View v) {
            listFilter = "all";
            listViewProductsAccepted.setVisibility(View.INVISIBLE);
            listViewProductsAll.setVisibility(View.VISIBLE);
            onStart();
            adapterAll.notifyDataSetChanged();
            clearButtonStyle();
            btnAll.setTypeface(Typeface.DEFAULT_BOLD);
            btnAll.setTextSize(16);
        }
    };

    //Reset Button Styles
    private void clearButtonStyle(){
        Button btnAccepted = (Button) getView().findViewById(R.id.btnAccepted);
        btnAccepted.setTypeface(Typeface.SANS_SERIF);
        btnAccepted.setTextSize(14);
        Button btnSuggested = (Button) getView().findViewById(R.id.btnSuggested);
        btnSuggested.setTypeface(Typeface.SANS_SERIF);
        btnSuggested.setTextSize(14);
        Button btnAll = (Button) getView().findViewById(R.id.btnAll);
        btnAll.setTypeface(Typeface.SANS_SERIF);
        btnAll.setTextSize(14);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        listViewProductsAccepted = (ListView) getView().findViewById(R.id.listViewProductsAccepted);
        listViewProductsAll = (ListView) getView().findViewById(R.id.listViewProductsAll);

        listViewProductsAccepted.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Product product = productListAccepted.get(i);
                showMenuDialog(product.getProductId(), product.getProductBuyer(), product.getProductCourier(), product.getProductName(), product.getProductType(), product.getProductCoords(), product.getLength(), product.getWidth(), product.getHeight(), product.getWeight(), product.getPrice(), product.getDate(), product.getImgurl(), product.getCountry(), product.getCourierComplete(), product.getBuyerComplete(), product.getTransit(), product.getBuyerPaid(), product.getStatus());
                return true;
            }
        });

        listViewProductsAll.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Product product = productListAll.get(i);
                String productID = product.getProductId();
                DatabaseReference dR = FirebaseDatabase.getInstance().getReference("products").child(productID);
                dR.child("productCourier").setValue(userEmail);
                Toast.makeText(getActivity().getApplicationContext(), "Order accepted!", Toast.LENGTH_LONG).show();
                return true;
            }
        });
    }

    //Menu Dialog for Accepted Tab
    private void showMenuDialog(final String productId, final String productBuyer, final String productCourier, final String productName, final String productType, final String productCoords, final String length, final String width, final String height, final String weight, final String price, final String date, final String url, final String country, final Boolean courierAccept, final Boolean buyerAccept, final Boolean transit, final Boolean buyerPaid, final String productStatus) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.courier_accepted_menu, null);
        dialogBuilder.setView(dialogView);
        final Button cancelOrder = (Button) dialogView.findViewById(R.id.cancelOrderButton);
        final Button inTransit = (Button) dialogView.findViewById(R.id.transitButton);
        final Button completeTransact = (Button) dialogView.findViewById(R.id.courierCompleteTransact);

        final AlertDialog b = dialogBuilder.create();
        b.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        b.show();

        if(!productStatus.equals("Matched")){
            cancelOrder.setEnabled(false);
        }

        if(transit == true){
            inTransit.setEnabled(false);
        }

        cancelOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCancel(productId, productBuyer, productCourier, productName, productType, productCoords, length, width, height, weight, price, date, url, country, courierAccept, buyerAccept, transit, buyerPaid);
                b.dismiss();
            }
        });

        inTransit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTransit(productId, productBuyer, productCourier, productName, productType, productCoords, length, width, height, weight, price, date, url, country, courierAccept, buyerAccept, transit, buyerPaid);
                b.dismiss();
            }
        });

        completeTransact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateComplete(productId, productBuyer, productCourier, productName, productType, productCoords, length, width, height, weight, price, date, url, country, courierAccept, buyerAccept, transit, buyerPaid);
                b.dismiss();
            }
        });
    }

    private boolean updateCancel(String productId, String productBuyer, String productCourier, String productName, String productType, String productCoords, String length, String width, String height, String weight, String price, String date, String url, String country, Boolean courierAccept, Boolean buyerAccept, Boolean transit, Boolean buyerPaid) {
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("products").child(productId);
        Product product = new Product(productId, productBuyer, "NONE", productName, productType, productCoords, length, width, height, weight, price, date, url, country, courierAccept, buyerAccept, transit, buyerPaid);
        dR.setValue(product);
        Toast.makeText(getActivity().getApplicationContext(), "Order rejected!", Toast.LENGTH_LONG).show();
        return true;
    }

    private boolean updateTransit(String productId, String productBuyer, String productCourier, String productName, String productType, String productCoords, String length, String width, String height, String weight, String price, String date, String url, String country, Boolean courierAccept, Boolean buyerAccept, Boolean transit, Boolean buyerPaid) {
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("products").child(productId);
        Product product = new Product(productId, productBuyer, productCourier, productName, productType, productCoords, length, width, height, weight, price, date, url, country, courierAccept, buyerAccept, true, buyerPaid);
        dR.setValue(product);
        Toast.makeText(getActivity().getApplicationContext(), "Order in transit!", Toast.LENGTH_LONG).show();
        return true;
    }
    
    private boolean updateComplete(String productId, String productBuyer, String productCourier, String productName, String productType, String productCoords, String length, String width, String height, String weight, String price, String date, String url, String country, Boolean courierAccept, Boolean buyerAccept, Boolean transit, Boolean buyerPaid) {
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("products").child(productId);
        Product product = new Product(productId, productBuyer, productCourier, productName, productType, productCoords, length, width, height, weight, price, date, url, country, true, buyerAccept, transit, buyerPaid);
        dR.setValue(product);
        Toast.makeText(getActivity().getApplicationContext(), "Transaction completed on courier's side!", Toast.LENGTH_LONG).show();
        return true;
    }

    //Can remove once filter tabs are finalized
    private View.OnClickListener suggestedListener = new View.OnClickListener() {
        public void onClick(View v) {
            // Create new fragment and transaction
            Fragment newFrag = new SuggestedCourierFragment();
            FragmentTransaction trans = getFragmentManager().beginTransaction();
            trans.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left);

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack
            getActivity().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            trans.replace(R.id.frame_layout, newFrag);
            trans.addToBackStack(null);

            // Commit the transaction
            trans.commit();
        }
    };

    @Override
    public void onStart() {
        super.onStart();

        //Accepted Tab
        databaseProductsAccepted.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                productListAccepted.clear();

                for(DataSnapshot productSnapshot : dataSnapshot.getChildren()){
                    Product product = productSnapshot.getValue(Product.class);
                    // Only include items that are accepted by courier
                    String courierID = product.getProductCourier();
                    if (courierID.equals(userEmail)) {
                        productListAccepted.add(product);
                    }
                }

                ProductListAccepted adapter = new ProductListAccepted(getActivity(), productListAccepted);
                listViewProductsAccepted.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Suggested & Display All Tab
        databaseProductsAll.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                productListAll.clear();

                for(DataSnapshot productSnapshot : dataSnapshot.getChildren()){
                    Product product = productSnapshot.getValue(Product.class);

                    String courierID = product.getProductCourier();
                    String buyerID = product.getProductBuyer();

                    // Only include items that are not assigned to any courier yet and not requested by user
                    if (courierID.equals("NONE") && !buyerID.equals(userEmail)) {
                        switch (listFilter) {
                            //Only add requests in same country as user
                            case "suggested":
                                if(userCountry != null) {
                                    if (userCountry.equals(product.getCountry())) {
                                        productListAll.add(product);
                                    }
                                }
                                break;
                            //List all requests
                            case "all":
                                productListAll.add(product);
                                break;
                        }
                    }
                }
                adapterAll = new ProductList(getActivity(), productListAll);
                listViewProductsAll.setAdapter(adapterAll);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
