package net.mashnoor.trainticketapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.Arrays;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {


    RecyclerView rvPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rvPosts = findViewById(R.id.rvPosts);
        rvPosts.setLayoutManager(new LinearLayoutManager(this));
        loadPosts();
        Dexter.withActivity(this).withPermission(Manifest.permission.CALL_PHONE).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {

            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

            }
        }).check();
    }

    private void loadPosts() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Loading. Please wait...");
        final AsyncHttpClient client = new AsyncHttpClient();
        client.get(AppUrl.GET_ALL_POSTS_URL, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                dialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Gson g = new Gson();
                final Post[] posts = g.fromJson(response, Post[].class);
                if(posts.length == 0)
                {
                    showToast("Search result empty!");
                }

                PostsAdapter adapter = new PostsAdapter(Arrays.asList(posts));
                adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                    @Override
                    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                        BootstrapButton b = (BootstrapButton) view;
                        if (b.getText().toString().equals("Delete")) {
                            showToast("Delete press");
                            final ProgressDialog dialog1 = new ProgressDialog(MainActivity.this);
                            dialog1.setMessage("Deleting. Please wait...");
                            AsyncHttpClient clnt = new AsyncHttpClient();
                            clnt.get(AppUrl.getPostDeleteUrl(posts[position].getId()), new AsyncHttpResponseHandler() {
                                @Override
                                public void onStart() {
                                    super.onStart();
                                    dialog1.show();

                                }

                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    showToast("Deleted Successfully!");
                                    dialog1.dismiss();
                                    Intent intent = getIntent();
                                    finish();
                                    startActivity(intent);

                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                    showToast("Something went wrong!");
                                    dialog1.dismiss();

                                }
                            });
                        }
                        else
                        {
                            Intent intent = new Intent(Intent.ACTION_CALL);

                            intent.setData(Uri.parse("tel:" + posts[position].getPostedBy().getNumber()));
                            startActivity(intent);
                        }
                    }
                });

                rvPosts.setAdapter(adapter);
                dialog.dismiss();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                dialog.dismiss();
                //Toast.makeText(MainActivity.class, "Something went wrong", Toast.LENGTH_LONG).show();
                showToast("Something went wrong!");

            }
        });

    }

    private void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    public void goPost(View v) {
        startActivity(new Intent(this, PostTicket.class));
    }

    public void goMyPosts(View v) {
        startActivity(new Intent(this, MyPosts.class));
    }

    public void goSearch(View v) {
        startActivity(new Intent(this, SearchActivity.class));
    }

    public void goRefresh(View v)
    {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }


}
