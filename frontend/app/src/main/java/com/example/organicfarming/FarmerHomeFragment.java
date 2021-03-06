package com.example.organicfarming;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FarmerHomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FarmerHomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FarmerHomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FarmerHomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FarmerHomeFragment newInstance(String param1, String param2) {
        FarmerHomeFragment fragment = new FarmerHomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    ImageView btnAddCrop;
    NavController navController;
    RecyclerView rcvCrops;
    FarmerCropAdapter cropAdapter;
    ArrayList<FarmerCrop> arrayList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnAddCrop=view.findViewById(R.id.btnAddCrop);
        navController= Navigation.findNavController(getActivity(),R.id.farmer_host_fragment);
        rcvCrops=view.findViewById(R.id.rcvFarmerHome);

        arrayList=new ArrayList<>();

        fetchFarmerCrops();

        cropAdapter=new FarmerCropAdapter(getActivity(),arrayList);

        rcvCrops.setHasFixedSize(true);
        rcvCrops.setLayoutManager(new LinearLayoutManager(getActivity()));
        rcvCrops.setAdapter(cropAdapter);

        btnAddCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.farmer_nav_add_crop);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_farmer_home, container, false);
    }

    private void fetchFarmerCrops(){
        String uid=getActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE).getString("UID","NO");
        ProgressDialog progressDialog=new ProgressDialog(getActivity());
        progressDialog.setMessage("Getting Crops");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

        RequestQueue queue= Volley.newRequestQueue(getActivity());
        StringRequest request=new StringRequest(Request.Method.POST, "https://farmerbuyer.herokuapp.com/getCrops", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d("Farmer",response);
                updateArrayList(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(),error.getMessage(),Toast.LENGTH_SHORT).show();
                Log.e("Error",error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                final HashMap<String, String> postParams = new HashMap<String, String>();
                postParams.put("uid",uid);
                return postParams;
            }
        };
        queue.add(request);
    }

    private void updateArrayList(String response){
        JSONArray array;
        try {
            array=new JSONArray(response);
            for(int i=0;i<array.length();i++){
                JSONObject object=array.getJSONObject(i);
                arrayList.add(new FarmerCrop(object.getString("name"),object.getString("image"),object.getInt("views")));
            }
            cropAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}