package com.example.firebasesocialmediaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ViewPostsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ListView postslistview;
    private ArrayList<String> usernames;
    private ArrayAdapter adapter;
    private FirebaseAuth firebaseAuth;
    private ImageView sentPostImageView;
    private TextView txtDescription;
    private ArrayList<DataSnapshot> dataSnapshots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_posts);

        firebaseAuth = FirebaseAuth.getInstance();

        postslistview = findViewById(R.id.postsListView);
        usernames = new ArrayList<>();
        adapter = new ArrayAdapter(ViewPostsActivity.this,android.R.layout.simple_list_item_1,usernames);
        postslistview.setAdapter(adapter);


        sentPostImageView = findViewById(R.id.sentPostImageView);
        txtDescription = findViewById(R.id.txtDescription);
        dataSnapshots = new ArrayList<>();
        postslistview.setOnItemClickListener(this);
        postslistview.setOnItemLongClickListener(this);

        FirebaseDatabase.getInstance().getReference()
                .child("my_users")
                .child(firebaseAuth.getCurrentUser().getUid())
                .child("received_posts")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        dataSnapshots.add(dataSnapshot);
                        String fromWhomUsername = (String) dataSnapshot.child("fromWhom").getValue();
                        usernames.add(fromWhomUsername);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        int i = 0;
                        for(DataSnapshot snapshot : dataSnapshots){
                            if (snapshot.getKey().equals(dataSnapshot.getKey())){
                                dataSnapshots.remove(i);
                                usernames.remove(i);
                                i++;
                            }
                        }
                        adapter.notifyDataSetChanged();
                        sentPostImageView.setImageResource(R.drawable.woman);
                        txtDescription.setText("");

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        DataSnapshot myDataSnapshot = dataSnapshots.get(position);
        String downloadLink = (String) myDataSnapshot.child("imageLink").getValue();

        Picasso.get().load(downloadLink).into(sentPostImageView);
        txtDescription.setText((String)myDataSnapshot.child("des").getValue());


    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete this Entry?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        FirebaseStorage.getInstance().getReference()
                                .child("my_images")
                                .child((String) dataSnapshots.get(position).child("imageIdentifier").getValue())
                                .delete();

                        FirebaseDatabase.getInstance().getReference()
                                .child("my_users")
                                .child(firebaseAuth.getCurrentUser().getUid())
                                .child("received_posts")
                                .child(dataSnapshots.get(position).getKey())
                                .removeValue();


                    }
                });

            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.show();
        return false;
    }
}
